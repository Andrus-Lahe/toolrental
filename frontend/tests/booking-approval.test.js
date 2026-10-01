import assert from 'node:assert/strict'
import { readFile } from 'node:fs/promises'
import { test } from 'node:test'
import { parse } from '@vue/compiler-sfc'
import axios from 'axios'
import BookingService from '../src/api-services/BookingService.js'
import {
  clearLoginReturnPath,
  getLoginReturnPath,
  hasLoginReturnPath,
  rememberLoginReturnPath,
} from '../src/auth/loginReturnPath.js'

async function loadOptions(relativePath, dependencies = {}) {
  const source = await readFile(new URL(`../src/${relativePath}`, import.meta.url), 'utf8')
  const { descriptor, errors } = parse(source, { filename: relativePath })
  assert.deepEqual(errors, [])
  const script = descriptor.script.content
    .replace(/^import .* from .*\n/gm, '')
    .replace(/export default\s*\{/, 'return {')
  const dependencyNames = Object.keys(dependencies)
  const createOptions = new Function(...dependencyNames, script)
  return createOptions(...dependencyNames.map((name) => dependencies[name]))
}

function deferred() {
  let resolve
  let reject
  const promise = new Promise((resolvePromise, rejectPromise) => {
    resolve = resolvePromise
    reject = rejectPromise
  })
  return { promise, resolve, reject }
}

const validBooking = (bookingId = 1, overrides = {}) => ({
  bookingId,
  toolId: 1,
  toolName: 'Akutrell',
  startDate: '2026-10-02',
  endDate: '2026-10-04',
  status: 'P',
  ownerMessage: null,
  isOwner: true,
  contactName: 'Liis Kask',
  contactEmail: 'liis.kask@example.com',
  contactPhone: '55501002',
  ...overrides,
})

async function createView(routeId = '1', bookingService = {}, navigationService = { navigateToMyTools: () => Promise.resolve() }) {
  const service = {
    sendGetBookingRequest: async (bookingId) => ({ status: 200, data: validBooking(bookingId) }),
    sendConfirmBookingRequest: async () => ({ status: 200, data: '' }),
    sendRejectBookingRequest: async () => ({ status: 200, data: '' }),
    ...bookingService,
  }
  const options = await loadOptions('views/BookingApprovalView.vue', {
    BookingService: service,
    AlertDanger: {},
    BookingContactCard: {},
    BookingDecisionForm: {},
    BookingDecisionModal: {},
    NavigationService: navigationService,
  })
  const context = {
    $route: { params: { bookingId: routeId } },
    $router: {},
    openLoginModal: () => {},
    ...options.data(),
  }
  Object.assign(context, options.methods)
  Object.defineProperties(context, {
    bookingId: { get: () => options.computed.bookingId.call(context) },
    canDecide: { get: () => options.computed.canDecide.call(context) },
  })
  return { context, options, service }
}

test('route booking IDs accept positive Java Integers only and invalid IDs do not call the API', async () => {
  let getCalls = 0
  const { context } = await createView('2147483648', {
    sendGetBookingRequest: () => { getCalls += 1 },
  })
  await context.loadBooking()
  assert.equal(context.bookingId, null)
  assert.equal(context.pageStatus, 'invalid-route')
  assert.equal(context.errorMessage, 'Vigane broneeringu ID.')
  assert.equal(getCalls, 0)
})

test('BookingService calls the expected GET and PATCH endpoints with only the decision body', async () => {
  const originalGet = axios.get
  const originalPatch = axios.patch
  const calls = []
  axios.get = (...args) => { calls.push(['get', ...args]); return Promise.resolve({ status: 200 }) }
  axios.patch = (...args) => { calls.push(['patch', ...args]); return Promise.resolve({ status: 200 }) }
  try {
    await BookingService.sendGetBookingRequest(5)
    await BookingService.sendConfirmBookingRequest(5, { ownerMessage: null })
    await BookingService.sendRejectBookingRequest(5, { ownerMessage: 'Põhjus' })
  } finally {
    axios.get = originalGet
    axios.patch = originalPatch
  }
  assert.deepEqual(calls, [
    ['get', '/api/bookings/5'],
    ['patch', '/api/bookings/5/confirm', { ownerMessage: null }],
    ['patch', '/api/bookings/5/reject', { ownerMessage: 'Põhjus' }],
  ])
})

test('loading uses GET data and formats dates without timezone conversion', async () => {
  let requestedId
  const { context } = await createView('1', {
    sendGetBookingRequest: async (bookingId) => {
      requestedId = bookingId
      return { status: 200, data: validBooking(bookingId) }
    },
  })
  await context.loadBooking()
  assert.equal(requestedId, 1)
  assert.equal(context.booking.toolName, 'Akutrell')
  assert.equal(context.formatDate(context.booking.startDate), '02.10.2026')
})

test('GET errors and unexpected response bodies show errors without stale booking data', async () => {
  const failures = [
    { status: 401, message: 'Palun logi sisse.' },
    { status: 403, message: 'Sul pole õigust seda broneeringut vaadata' },
    { status: 404, message: "Ei leidnud primary keyd 'bookingId' väärtusega: 1" },
    { status: 400, message: 'bookingId: peab olema Integer-tüüpi täisarv' },
    { status: 500, message: 'Broneeringu laadimine ebaõnnestus. Palun proovi hiljem uuesti.' },
  ]
  for (const failure of failures) {
    const { context } = await createView('1', {
      sendGetBookingRequest: async () => {
        throw { response: { status: failure.status, data: { message: failure.message } } }
      },
    })
    context.booking = validBooking()
    await context.loadBooking()
    assert.equal(context.booking, null)
    assert.equal(context.errorMessage, failure.message)
    assert.equal(context.pageStatus, failure.status === 401 ? 'unauthorized' : 'error')
  }

  const { context } = await createView('1', {
    sendGetBookingRequest: async () => ({ status: 200, data: null }),
  })
  await context.loadBooking()
  assert.equal(context.booking, null)
  assert.equal(context.errorMessage, 'Päring ebaõnnestus. Palun proovi uuesti.')
})

test('late GET responses cannot replace data for a newer route ID', async () => {
  const first = deferred()
  const second = deferred()
  const { context } = await createView('1', {
    sendGetBookingRequest: (id) => id === 1 ? first.promise : second.promise,
  })
  const firstLoad = context.loadBooking()
  context.$route.params.bookingId = '2'
  const secondLoad = context.loadBooking()
  second.resolve({ status: 200, data: validBooking(2, { toolName: 'Redel' }) })
  await secondLoad
  first.resolve({ status: 200, data: validBooking(1, { toolName: 'Akutrell' }) })
  await firstLoad
  assert.equal(context.booking.bookingId, 2)
  assert.equal(context.booking.toolName, 'Redel')
})

test('decision permission requires exactly owner true and pending status', async () => {
  const { context } = await createView()
  context.booking = validBooking(1, { isOwner: true, status: 'P' })
  assert.equal(context.canDecide, true)
  context.booking = validBooking(1, { isOwner: true, status: 'C' })
  assert.equal(context.canDecide, false)
  context.booking = validBooking(1, { isOwner: true, status: 'R' })
  assert.equal(context.canDecide, false)
  context.session = { user: { roleName: 'admin' } }
  context.booking = validBooking(1, { isOwner: false, status: 'P' })
  assert.equal(context.canDecide, false)
})

test('confirm PATCH sends only ownerMessage and an empty 200 body opens confirmation modal', async () => {
  let call
  const { context } = await createView('1', {
    sendConfirmBookingRequest: async (...args) => {
      call = args
      return { status: 200, data: '' }
    },
  })
  context.booking = validBooking()
  await context.handleConfirm({ ownerMessage: 'Palun helista.' })
  assert.deepEqual(call, [1, { ownerMessage: 'Palun helista.' }])
  assert.equal(context.booking.status, 'C')
  assert.equal(context.booking.ownerMessage, 'Palun helista.')
  assert.equal(context.isDecisionModalOpen, true)
  assert.equal(context.completedDecision, 'confirmed')
})

test('reject sends null for an empty message and suppresses duplicate in-flight decisions', async () => {
  const request = deferred()
  const calls = []
  const { context } = await createView('7', {
    sendRejectBookingRequest: (id, body) => {
      calls.push([id, body])
      return request.promise
    },
    sendConfirmBookingRequest: () => {
      calls.push('unexpected-confirm')
      return request.promise
    },
  })
  context.booking = validBooking(7)
  const firstDecision = context.handleReject({ ownerMessage: null })
  await context.handleConfirm({ ownerMessage: 'second click' })
  assert.deepEqual(calls, [[7, { ownerMessage: null }]])
  request.resolve({ status: 200, data: '' })
  await firstDecision
  assert.equal(context.booking.status, 'R')
  assert.equal(context.completedDecision, 'rejected')
})

test('400 keeps the booking and displays backend validation message without opening modal', async () => {
  const { context } = await createView('1', {
    sendConfirmBookingRequest: async () => {
      throw { response: { status: 400, data: { message: 'ownerMessage: Sõnum võib olla kuni 500 märki' } } }
    },
  })
  context.booking = validBooking()
  await context.handleConfirm({ ownerMessage: 'x'.repeat(500) })
  assert.equal(context.booking.status, 'P')
  assert.equal(context.errorMessage, 'ownerMessage: Sõnum võib olla kuni 500 märki')
  assert.equal(context.isDecisionModalOpen, false)
})

test('404 and BOOKING_NOT_OWNER hide the booking data', async () => {
  for (const error of [
    { response: { status: 404, data: { message: 'Broneeringut ei leitud' } } },
    { response: { status: 403, data: { errorCode: 'BOOKING_NOT_OWNER', message: 'Keelatud' } } },
  ]) {
    const { context } = await createView('1', {
      sendConfirmBookingRequest: async () => { throw error },
    })
    context.booking = validBooking()
    await context.handleConfirm({ ownerMessage: null })
    assert.equal(context.booking, null)
    assert.equal(context.pageStatus, 'error')
    assert.equal(context.isDecisionModalOpen, false)
  }
})

test('BOOKING_NOT_PENDING reloads fresh data, keeps the message, and blocks another decision', async () => {
  let getCalls = 0
  const { context } = await createView('1', {
    sendConfirmBookingRequest: async () => {
      throw { response: { status: 403, data: { errorCode: 'BOOKING_NOT_PENDING', message: 'Taotlus on juba otsustatud' } } }
    },
    sendGetBookingRequest: async (id) => {
      getCalls += 1
      return { status: 200, data: validBooking(id, { status: 'C' }) }
    },
  })
  context.booking = validBooking()
  await context.handleConfirm({ ownerMessage: null })
  assert.equal(getCalls, 1)
  assert.equal(context.booking.status, 'C')
  assert.equal(context.canDecide, false)
  assert.equal(context.errorMessage, 'Taotlus on juba otsustatud')
})

test('PATCH 500 refreshes booking state but does not show success modal', async () => {
  let getCalls = 0
  const { context } = await createView('1', {
    sendConfirmBookingRequest: async () => {
      throw { response: { status: 500, data: { message: 'Taotluse kinnitamine ebaõnnestus' } } }
    },
    sendGetBookingRequest: async (id) => {
      getCalls += 1
      return { status: 200, data: validBooking(id) }
    },
  })
  context.booking = validBooking()
  await context.handleConfirm({ ownerMessage: null })
  assert.equal(getCalls, 1)
  assert.equal(context.booking.status, 'P')
  assert.equal(context.isDecisionModalOpen, false)
  assert.equal(context.errorMessage, 'Taotluse kinnitamine ebaõnnestus')
})

test('view does not send a 501-character owner message and modal close navigates to My Tools', async () => {
  let patchCalls = 0
  let navigation
  const { context } = await createView('1', {
    sendConfirmBookingRequest: async () => {
      patchCalls += 1
      return { status: 200, data: '' }
    },
  }, {
    navigateToMyTools: (router) => { navigation = router },
  })
  context.booking = validBooking()
  await context.handleConfirm({ ownerMessage: 'x'.repeat(501) })
  assert.equal(patchCalls, 0)
  assert.equal(context.errorMessage, 'Sõnum võib olla kuni 500 märki')
  context.handleDecisionModalClosed()
  assert.equal(navigation, context.$router)
})

test('401 opens the shared login modal without resending the decision', async () => {
  let loginModalCalls = 0
  let patchCalls = 0
  const { context } = await createView('1', {
    sendConfirmBookingRequest: async () => {
      patchCalls += 1
      throw { response: { status: 401, data: '' } }
    },
  })
  context.openLoginModal = () => { loginModalCalls += 1 }
  context.booking = validBooking()
  await context.handleConfirm({ ownerMessage: null })
  assert.equal(loginModalCalls, 1)
  assert.equal(patchCalls, 1)
  assert.equal(context.errorMessage, 'Palun logi sisse.')
  assert.equal(context.isDecisionModalOpen, false)
})

test('decision form accepts 500 characters, maps empty input to null, and rejects 501', async () => {
  const form = await loadOptions('components/forms/BookingDecisionForm.vue')
  const emitted = []
  const context = { disabled: false, ownerMessage: 'x'.repeat(500), validationMessage: '', $emit: (...args) => emitted.push(args) }
  form.methods.submitDecision.call(context, 'confirm')
  assert.deepEqual(emitted, [['event-confirm', { ownerMessage: 'x'.repeat(500) }]])
  context.ownerMessage = 'x'.repeat(501)
  form.methods.submitDecision.call(context, 'reject')
  assert.equal(context.validationMessage, 'Sõnum võib olla kuni 500 märki')
  assert.equal(emitted.length, 1)
  context.ownerMessage = '  '
  context.validationMessage = ''
  form.methods.submitDecision.call(context, 'reject')
  assert.deepEqual(emitted[1], ['event-reject', { ownerMessage: '  ' }])
  context.ownerMessage = ''
  form.methods.submitDecision.call(context, 'confirm')
  assert.deepEqual(emitted[2], ['event-confirm', { ownerMessage: null }])
})

test('contact card omits missing contacts and URL-encodes Gmail recipient', async () => {
  const card = await loadOptions('components/common/BookingContactCard.vue')
  const context = { contactName: 'Liis Kask', contactEmail: 'liis+kask@example.com', contactPhone: null }
  Object.defineProperty(context, 'emailAddress', { get: () => card.computed.emailAddress.call(context) })
  assert.equal(card.computed.emailComposeUrl.call(context),
    'https://mail.google.com/mail/?view=cm&fs=1&to=liis%2Bkask%40example.com')
  context.contactEmail = null
  assert.equal(card.computed.emailAddress.call(context), '')
  assert.equal(card.computed.displayName.call({ contactName: null }), '—')
})

test('decision modal displays matching result and emits close event', async () => {
  const modal = await loadOptions('components/modals/BookingDecisionModal.vue')
  assert.equal(modal.computed.title.call({ decision: 'confirmed' }), 'Taotlus kinnitatud')
  assert.equal(modal.computed.title.call({ decision: 'rejected' }), 'Taotlus tagasi lükatud')
  const emitted = []
  modal.methods.closeModal.call({ $emit: (...args) => emitted.push(args) })
  assert.deepEqual(emitted, [['event-modal-closed']])
})

test('shared login flow remembers only booking return paths and consumes them after redirect', () => {
  const previousWindow = globalThis.window
  const sessionStorage = new Map()
  globalThis.window = {
    sessionStorage: {
      getItem: (key) => sessionStorage.get(key) ?? null,
      setItem: (key, value) => sessionStorage.set(key, value),
      removeItem: (key) => sessionStorage.delete(key),
    },
  }
  try {
    rememberLoginReturnPath('/admin')
    assert.equal(hasLoginReturnPath(), false)
    rememberLoginReturnPath('/bookings/42?source=email')
    assert.equal(hasLoginReturnPath(), true)
    assert.equal(getLoginReturnPath(), '/bookings/42?source=email')
    clearLoginReturnPath()
    assert.equal(hasLoginReturnPath(), false)
  } finally {
    if (previousWindow === undefined) delete globalThis.window
    else globalThis.window = previousWindow
  }
})

test('App reopens the requested booking after authentication and clears the saved path', async () => {
  const previousWindow = globalThis.window
  const sessionStorage = new Map()
  globalThis.window = {
    sessionStorage: {
      getItem: (key) => sessionStorage.get(key) ?? null,
      setItem: (key, value) => sessionStorage.set(key, value),
      removeItem: (key) => sessionStorage.delete(key),
    },
  }
  try {
    const session = { status: 'authenticated', user: null, error: '' }
    const app = await loadOptions('App.vue', {
      RouterView: {},
      AppHeader: {},
      GoogleLoginModal: {},
      loadSession: async () => {},
      session,
      clearLoginReturnPath,
      rememberLoginReturnPath,
      getLoginReturnPath,
    })
    let navigatedTo = null
    const context = {
      ...app.data(),
      session,
      $route: { fullPath: '/' },
      $router: { replace: (path) => { navigatedTo = path; return Promise.resolve() } },
    }
    Object.assign(context, app.methods)
    app.provide.call(context).openLoginModal('/bookings/81')
    assert.equal(context.isLoginModalOpen, true)
    context.restoreLoginReturnPath()
    await Promise.resolve()
    assert.equal(navigatedTo, '/bookings/81')
    assert.equal(getLoginReturnPath(), null)
  } finally {
    if (previousWindow === undefined) delete globalThis.window
    else globalThis.window = previousWindow
  }
})
