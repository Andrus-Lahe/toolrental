import assert from 'node:assert/strict'
import { readFile } from 'node:fs/promises'
import { test } from 'node:test'
import { parse } from '@vue/compiler-sfc'

async function createView(post) {
  const source = await readFile(new URL('../src/views/AiSearchView.vue', import.meta.url), 'utf8')
  const { descriptor, errors } = parse(source, { filename: 'AiSearchView.vue' })
  assert.deepEqual(errors, [])

  const script = descriptor.script.content
    .replace(/^import .* from .*\n/gm, '')
    .replace(/export default\s*\{/, 'return {')
  const options = new Function('axios', 'RouterLink', script)({ post }, {})
  const context = { ...options.data() }
  Object.assign(context, options.methods)
  return { context, options, template: descriptor.template.content }
}

const result = (overrides = {}) => ({
  toolId: 12,
  name: 'Akutrell',
  category: 'Ehitustööd',
  location: 'Kristiine',
  ...overrides,
})

test('renders tool names as detail links using each API toolId and displays location', async () => {
  const calls = []
  const { context, options, template } = await createView(async (...args) => {
    calls.push(args)
    return { data: { tools: [result()] } }
  })
  context.question = 'Millised tööriistad on saadaval Kristiines?'

  await context.ask()

  assert.deepEqual(calls, [['/api/ai/ask', { question: context.question }]])
  assert.deepEqual(context.tools, [result()])
  assert.equal(context.hasSearched, true)
  assert.equal(context.busy, false)
  assert.ok(options.components.RouterLink)
  assert.match(template, /name: 'toolDetailRoute', params: \{ toolId: tool\.toolId \}/)
  assert.match(template, /\{\{ tool\.name \}\}/)
  assert.match(template, /\{\{ tool\.category \}\}/)
  assert.match(template, /tool\.location/)
  assert.doesNotMatch(template, /tool\.district|answer/)
})

test('shows successful empty results separately from request errors', async () => {
  const { context } = await createView(async () => ({ data: { tools: [] } }))
  context.question = 'Puuduva vaste küsimus'

  await context.ask()

  assert.equal(context.hasSearched, true)
  assert.deepEqual(context.tools, [])
  assert.equal(context.error, '')
})

test('rejects malformed result payloads instead of creating unsafe links', async () => {
  const { context } = await createView(async () => ({
    data: { tools: [result({ toolId: '12' })] },
  }))
  context.question = 'Millised tööriistad on saadaval?'

  await context.ask()

  assert.equal(context.hasSearched, false)
  assert.deepEqual(context.tools, [])
  assert.equal(context.error, 'Päring ebaõnnestus. Palun proovi uuesti.')
})

test('shows dedicated messages for 400 and 422 and a generic message for 500/network errors', async () => {
  for (const [failure, expectedMessage, expectedLogin] of [
    [{ response: { status: 401 } }, 'Tööriistade küsimiseks logi esmalt sisse.', true],
    [{ response: { status: 400, data: { errorCode: 'INCORRECT_INPUT' } } },
      'Palun sisesta küsimus, mis ei ole tühi ja on kuni 500 märki pikk.', false],
    [{ response: { status: 422, data: { errorCode: 'AI_UNSUPPORTED_QUESTION' } } },
      'Saan aidata vaid tööriistade otsinguda.', false],
    [{ response: { status: 500 } }, 'Päring ebaõnnestus. Palun proovi uuesti.', false],
    [new Error('network failure'), 'Päring ebaõnnestus. Palun proovi uuesti.', false],
  ]) {
    const { context } = await createView(async () => { throw failure })
    context.question = 'Millised tööriistad on saadaval?'

    await context.ask()

    assert.equal(context.needsLogin, expectedLogin)
    assert.equal(context.error, expectedMessage)
    assert.equal(context.hasSearched, false)
    assert.equal(context.busy, false)
  }
})
