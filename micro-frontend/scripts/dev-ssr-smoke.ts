import assert from 'node:assert/strict'
import path from 'node:path'
import { fileURLToPath } from 'node:url'

const root = path.resolve(fileURLToPath(new URL('..', import.meta.url)))

const reservePort = async (): Promise<number> => {
  const reservation = Bun.serve({ port: 0, fetch: () => new Response() })
  const port = reservation.port
  await reservation.stop(true)
  if (port === undefined) throw new Error('Bun did not assign a reservation port')
  return port
}

const waitForReady = async (url: string, timeoutMillis: number): Promise<void> => {
  const deadline = Date.now() + timeoutMillis
  while (Date.now() < deadline) {
    try {
      const response = await fetch(url)
      if (response.ok) return
    } catch {
      // The development server may still be starting up.
    }
    await Bun.sleep(50)
  }
  assert.fail(`Development server did not become ready at ${url}`)
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
    if (url.pathname === '/public/blog/page/1') {
      return Response.json({
        msg: 'OK',
        data: {
          content: [
            {
              id: 1,
              title: 'Development SSR blog list',
              description: 'Prefetched list content hidden behind the skeleton',
              created: '2026-08-30',
              link: '/standalone-cover.webp',
              status: 0
            }
          ],
          totalElements: 1,
          pageSize: 5,
          pageNumber: 1
        }
      })
    }
    return Response.json({ msg: 'Not Found', data: null }, { status: 404 })
  }
})

const devPort = await reservePort()
const child = Bun.spawn(['bun', '--bun', 'vite', '--port', String(devPort), '--strictPort'], {
  cwd: root,
  env: {
    ...process.env,
    SSR_API_BASE_URL: `http://127.0.0.1:${gateway.port}`,
    OTEL_SDK_DISABLED: 'true',
    LOG_LEVEL: 'warn'
  },
  stdin: 'ignore',
  stdout: 'pipe',
  stderr: 'pipe'
})
const output = [new Response(child.stdout).text(), new Response(child.stderr).text()]
const baseUrl = `http://127.0.0.1:${devPort}`

try {
  await waitForReady(`${baseUrl}/actuator/health`, 30_000)

  const response = await fetch(`${baseUrl}/blogs`)
  assert.equal(response.status, 200)
  const html = await response.text()
  assert.match(html, /Development SSR blog list/)

  // Vite injects development styles through JavaScript, so the server HTML has to declare the
  // stylesheets itself, exactly like the production manifest does.
  const styleHrefs = [...html.matchAll(/<link[^>]*rel="stylesheet"[^>]*href="([^"]+)"/g)].map(
    (match) => match[1]!
  )
  assert.ok(styleHrefs.length > 0, 'development SSR must declare stylesheets')
  assert.equal(new Set(styleHrefs).size, styleHrefs.length, 'stylesheets must not repeat')

  const styles: string[] = []
  for (const href of styleHrefs) {
    const stylesheet = await fetch(`${baseUrl}${href}`, {
      headers: { Accept: 'text/css,*/*;q=0.1' }
    })
    assert.equal(stylesheet.status, 200, `stylesheet ${href} must be served`)
    assert.match(
      stylesheet.headers.get('content-type') || '',
      /text\/css/,
      `stylesheet ${href} must be served as text/css`
    )
    styles.push(await stylesheet.text())
  }
  const styleText = styles.join('\n')
  for (const selector of ['.el-pagination', '.el-button', '.el-skeleton']) {
    assert.ok(styleText.includes(selector), `${selector} must be declared before the first paint`)
  }

  // The theme script has to run before the first paint, not after hydration.
  assert.match(html, /megalith_theme/)

  const repeat = await fetch(`${baseUrl}/blogs`)
  assert.equal(repeat.status, 200)
  await repeat.arrayBuffer()
  assert.equal(child.exitCode, null, 'development server must survive rendering')

  child.kill('SIGTERM')
  await child.exited
  console.log('Development SSR stylesheet preloading verified')
} finally {
  if (child.exitCode === null) {
    child.kill('SIGKILL')
    await child.exited
  }
  await gateway.stop(true)
  await Promise.all(output)
}
