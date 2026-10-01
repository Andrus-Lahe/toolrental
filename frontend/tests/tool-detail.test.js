import assert from 'node:assert/strict'
import { readFile } from 'node:fs/promises'
import { test } from 'node:test'
import { parse } from '@vue/compiler-sfc'
import axios from 'axios'
import ToolService from '../src/api-services/ToolService.js'
import UserService from '../src/api-services/UserService.js'
import NavigationService from '../src/navigation/NavigationService.js'

async function loadOptions(relativePath, dependencies = {}) {
  const source = await readFile(new URL(`../src/${relativePath}`, import.meta.url), 'utf8')
  const { descriptor, errors } = parse(source, { filename: relativePath })
  assert.deepEqual(errors, [])
  const script = descriptor.script.content
    .replace(/^import .* from .*\n/gm, '')
    .replace(/export default\s*\{/, 'return {')
  const dependencyNames = Object.keys(dependencies)
  return new Function(...dependencyNames, script)(...dependencyNames.map((name) => dependencies[name]))
}

const validTool = (overrides = {}) => ({
  toolId: 1,
  ownerId: 4,
  toolName: 'Akutrell',
  categoryName: 'Ehitustööd',
  categoryDescription: 'Akutrell koos akudega',
  imageData: 'c3Zn',
  status: 'A',
  ...overrides,
})

async function createView({ toolService = {}, userService = {}, sessionStatus = 'guest', routeId = '1' } = {}) {
  const currentSession = {
    status: sessionStatus,
    user: sessionStatus === 'authenticated' ? { userId: 10 } : null,
  }
  const tools = {
    sendGetToolDetailsRequest: async () => ({ status: 200, data: validTool() }),
    ...toolService,
  }
  const users = {
    sendGetUserDetailsRequest: async (userId) => ({
      status: 200,
      data: { userId, firstName: 'Marko', lastName: 'Tamm', email: 'email@Gmail.com', phone: '56565656' },
    }),
    ...userService,
  }
  const options = await loadOptions('views/ToolDetailView.vue', {
    ToolService: tools,
    UserService: users,
    AlertDanger: {},
    OwnerContactCard: {},
    ToolImage: {},
    session: currentSession,
    NavigationService,
  })
  const calls = []
  const context = {
    $route: { params: { toolId: routeId }, fullPath: `/tools/${routeId}` },
    $router: { push: (location) => { calls.push(location); return location } },
    openLoginModal: (...args) => { calls.push(['login', ...args]); return 'opened-login' },
    ...options.data(),
  }
  Object.assign(context, options.methods)
  for (const [name, getter] of Object.entries(options.computed)) {
    Object.defineProperty(context, name, { get: () => getter.call(context) })
  }
  return { context, tools, users, calls }
}

function deferred() {
  let resolve
  const promise = new Promise((resolvePromise) => { resolve = resolvePromise })
  return { promise, resolve }
}

test('tool and owner services request their expected API routes', async () => {
  const originalGet = axios.get
  const calls = []
  axios.get = (...args) => { calls.push(args); return Promise.resolve({ status: 200 }) }
  try {
    await ToolService.sendGetToolDetailsRequest(5)
    await UserService.sendGetUserDetailsRequest(9)
  } finally {
    axios.get = originalGet
  }
  assert.deepEqual(calls, [['/api/tools/5'], ['/api/users/9']])
})

test('guest sees the tool and never triggers the private owner request', async () => {
  let ownerCalls = 0
  const { context } = await createView({ userService: {
    sendGetUserDetailsRequest: () => { ownerCalls += 1 },
  } })
  await context.loadTool()
  assert.equal(context.tool.toolName, 'Akutrell')
  assert.equal(context.toolDescription, 'Akutrell koos akudega')
  assert.equal(context.owner, null)
  assert.equal(ownerCalls, 0)
})

test('logged-in session loads owner only after tool response', async () => {
  const calls = []
  const { context } = await createView({
    sessionStatus: 'authenticated',
    toolService: { sendGetToolDetailsRequest: async () => {
      calls.push('tool')
      return { status: 200, data: validTool() }
    } },
    userService: { sendGetUserDetailsRequest: async (userId) => {
      calls.push(['owner', userId])
      return { status: 200, data: { userId, firstName: 'Marko', lastName: 'Tamm', email: null, phone: null } }
    } },
  })
  await context.loadTool()
  await Promise.resolve()
  assert.deepEqual(calls, ['tool', ['owner', 4]])
  assert.equal(context.owner.firstName, 'Marko')
})

test('owner request waits while shared session is loading and starts when it authenticates', async () => {
  let ownerCalls = 0
  const { context } = await createView({
    sessionStatus: 'loading',
    userService: { sendGetUserDetailsRequest: async (userId) => {
      ownerCalls += 1
      return { status: 200, data: { userId, firstName: 'Mari', lastName: 'Mets', email: null, phone: null } }
    } },
  })
  await context.loadTool()
  assert.equal(ownerCalls, 0)
  context.session.user = { userId: 10 }
  context.session.status = 'authenticated'
  await context.loadOwnerIfReady()
  assert.equal(ownerCalls, 1)
  assert.equal(context.owner.firstName, 'Mari')
})

test('tool API errors show backend message or the network fallback and hide booking action', async () => {
  for (const error of [
    { response: { status: 404, data: { message: 'Tööriista ei leitud' } }, expected: 'Tööriista ei leitud' },
    { response: { status: 400, data: { message: 'toolId: vigane' } }, expected: 'toolId: vigane' },
    { response: { status: 500, data: { message: 'Tööriista laadimine ebaõnnestus.' } }, expected: 'Tööriista laadimine ebaõnnestus.' },
    { expected: 'Tööriista laadimine ebaõnnestus. Palun proovi hiljem uuesti.' },
  ]) {
    const { context } = await createView({ toolService: {
      sendGetToolDetailsRequest: async () => { throw error },
    } })
    await context.loadTool()
    assert.equal(context.tool, null)
    assert.equal(context.errorMessage, error.expected)
    assert.equal(context.isLoading, false)
  }
})

test('owner errors leave tool data visible and 401 clears shared login state', async () => {
  const failed = await createView({ sessionStatus: 'authenticated', userService: {
    sendGetUserDetailsRequest: async () => {
      throw { response: { status: 500, data: { message: 'Kontaktide päring ebaõnnestus' } } }
    },
  } })
  await failed.context.loadTool()
  await Promise.resolve()
  assert.equal(failed.context.tool.toolName, 'Akutrell')
  assert.equal(failed.context.owner, null)
  assert.equal(failed.context.ownerErrorMessage, 'Kontaktide päring ebaõnnestus')

  const unauthorized = await createView({ sessionStatus: 'authenticated', userService: {
    sendGetUserDetailsRequest: async () => { throw { response: { status: 401, data: '' } } },
  } })
  await unauthorized.context.loadTool()
  await Promise.resolve()
  assert.equal(unauthorized.context.tool.toolName, 'Akutrell')
  assert.equal(unauthorized.context.owner, null)
  assert.equal(unauthorized.context.session.status, 'guest')
  assert.equal(unauthorized.context.session.user, null)
})

test('status U displays unavailable state while keeping the tool loaded', async () => {
  const { context } = await createView({ toolService: {
    sendGetToolDetailsRequest: async () => ({ status: 200, data: validTool({ status: 'U' }) }),
  } })
  await context.loadTool()
  assert.equal(context.isToolUnavailable, true)
  assert.equal(context.tool.toolName, 'Akutrell')
})

test('route changes reload data and ignore a late response from the previous tool', async () => {
  const first = deferred()
  const second = deferred()
  const { context } = await createView({ toolService: {
    sendGetToolDetailsRequest: (id) => id === '1' ? first.promise : second.promise,
  } })
  const firstLoad = context.loadTool()
  context.$route.params.toolId = '2'
  const secondLoad = context.loadTool()
  second.resolve({ status: 200, data: validTool({ toolId: 2, ownerId: 8, toolName: 'Redel', status: 'U' }) })
  await secondLoad
  first.resolve({ status: 200, data: validTool({ toolId: 1, ownerId: 4, toolName: 'Akutrell' }) })
  await firstLoad
  assert.equal(context.tool.toolName, 'Redel')
  assert.equal(context.isToolUnavailable, true)
})

test('booking button navigates for a logged-in user and opens login for a guest', async () => {
  const loggedIn = await createView({ sessionStatus: 'authenticated' })
  await loggedIn.context.loadTool()
  assert.deepEqual(loggedIn.context.handleLendClick(), {
    name: 'bookingFormRoute',
    params: { toolId: '1' },
  })
  assert.deepEqual(loggedIn.calls, [{ name: 'bookingFormRoute', params: { toolId: '1' } }])

  const guest = await createView()
  await guest.context.loadTool()
  assert.equal(guest.context.handleLendClick(), 'opened-login')
  assert.deepEqual(guest.calls, [['login']])
})

test('detail route is registered and contact fields are conditional', async () => {
  const [routerSource, ownerCardSource] = await Promise.all([
    readFile(new URL('../src/router/index.js', import.meta.url), 'utf8'),
    readFile(new URL('../src/components/common/OwnerContactCard.vue', import.meta.url), 'utf8'),
  ])
  assert.match(routerSource, /path:\s*'\/tools\/:toolId'/)
  assert.match(routerSource, /name:\s*'toolDetailRoute'/)
  assert.match(ownerCardSource, /v-if="owner\.email"/)
  assert.match(ownerCardSource, /v-if="owner\.phone"/)
})

test('ToolImage builds an SVG data URL and falls back for empty image data', async () => {
  const options = await loadOptions('components/common/ToolImage.vue')
  const context = { imageData: '  c3Zn  ' }
  assert.equal(options.computed.imageSource.call(context), 'data:image/svg+xml;base64,c3Zn')
  context.imageData = null
  assert.equal(options.computed.imageSource.call(context), '')
})
