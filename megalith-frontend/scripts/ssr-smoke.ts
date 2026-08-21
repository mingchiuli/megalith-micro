import assert from 'node:assert/strict'
import path from 'node:path'
import { fileURLToPath } from 'node:url'

const root = path.resolve(fileURLToPath(new URL('..', import.meta.url)))
const binary = path.join(root, 'dist/bin/megalith-frontend')

const waitForHealth = async (url: string, timeoutMillis: number): Promise<void> => {
  const deadline = Date.now() + timeoutMillis
  while (Date.now() < deadline) {
    try {
      const response = await fetch(url)
      if (response.ok) return
    } catch {
      // The standalone process may still be initializing.
    }
    await Bun.sleep(50)
  }
  assert.fail(`Frontend did not become healthy at ${url}`)
}

const reservePort = async (): Promise<number> => {
  const reservation = Bun.serve({ port: 0, fetch: () => new Response() })
  const port = reservation.port
  await reservation.stop(true)
  if (port === undefined) throw new Error('Bun did not assign a reservation port')
  return port
}

const gateway = Bun.serve({
  hostname: '127.0.0.1',
  port: 0,
  fetch(request) {
    const url = new URL(request.url)
    if (url.pathname === '/public/blog/stat') {
      return Response.json({
        msg: 'OK',
        data: { dayVisit: 1, weekVisit: 2, monthVisit: 3, yearVisit: 4 }
      })
    }
    return Response.json({ msg: 'Not Found', data: null }, { status: 404 })
  }
})

const frontendPort = await reservePort()
const child = Bun.spawn([binary], {
  cwd: root,
  env: {
    ...process.env,
    NODE_ENV: 'production',
    PORT: String(frontendPort),
    SSR_API_BASE_URL: `http://127.0.0.1:${gateway.port}`,
    APP_ORIGIN: 'https://chiu.wiki',
    OTEL_SDK_DISABLED: 'true'
  },
  stdin: 'ignore',
  stdout: 'pipe',
  stderr: 'pipe'
})
const stdout = new Response(child.stdout).text()
const stderr = new Response(child.stderr).text()
const baseUrl = `http://127.0.0.1:${frontendPort}`

try {
  await waitForHealth(`${baseUrl}/actuator/health`, 10_000)

  const intro = await fetch(`${baseUrl}/`)
  assert.equal(intro.status, 200)
  assert.match(intro.headers.get('content-type') || '', /^text\/html/)
  assert.equal(intro.headers.get('cache-control'), 'private, no-store')
  assert.match(await intro.text(), /class="intro-notebook-icon"/)

  const login = await fetch(`${baseUrl}/login`)
  assert.equal(login.status, 200)
  assert.match(await login.text(), /<title>登录<\/title>/)

  const notFound = await fetch(`${baseUrl}/production-ssr-smoke-not-found`)
  assert.equal(notFound.status, 404)
  assert.match(await notFound.text(), /404 NOT FOUND/)

  const manifest = (await Bun.file(
    path.join(root, 'dist/client/.vite/public-assets.json')
  ).json()) as Record<string, string>
  const javascriptPath = Object.keys(manifest).find((file) => file.endsWith('.js'))
  assert.ok(javascriptPath)

  const asset = await fetch(`${baseUrl}${javascriptPath}`, {
    headers: { 'Accept-Encoding': 'gzip' }
  })
  assert.equal(asset.status, 200)
  assert.match(asset.headers.get('content-type') || '', /javascript/)
  assert.equal(asset.headers.get('cache-control'), 'public, max-age=31536000, immutable')
  assert.equal(asset.headers.get('content-encoding'), 'gzip')
  assert.ok((await asset.arrayBuffer()).byteLength > 0)

  const weightedCompression = await fetch(`${baseUrl}${javascriptPath}`, {
    headers: { 'Accept-Encoding': 'gzip;q=0.5' }
  })
  assert.equal(weightedCompression.headers.get('content-encoding'), 'gzip')
  await weightedCompression.arrayBuffer()

  const disabledCompression = await fetch(`${baseUrl}${javascriptPath}`, {
    headers: { 'Accept-Encoding': 'gzip;q=0' }
  })
  assert.equal(disabledCompression.headers.get('content-encoding'), null)
  await disabledCompression.arrayBuffer()

  const head = await fetch(`${baseUrl}${javascriptPath}`, { method: 'HEAD' })
  assert.equal(head.status, 200)
  assert.equal((await head.arrayBuffer()).byteLength, 0)

  child.kill('SIGTERM')
  assert.equal(await child.exited, 0)
  const output = `${await stdout}${await stderr}`
  assert.match(output, /"runtime\.standalone":true/)
  assert.match(output, /HTTP server stopped/)
  console.log('Standalone Bun SSR, embedded assets, compression, and shutdown verified')
} finally {
  if (child.exitCode === null) {
    child.kill('SIGKILL')
    await child.exited
  }
  await gateway.stop(true)
}
