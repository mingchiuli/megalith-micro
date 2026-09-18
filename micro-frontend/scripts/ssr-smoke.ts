import assert from 'node:assert/strict'
import { mkdtemp, rm } from 'node:fs/promises'
import { tmpdir } from 'node:os'
import path from 'node:path'
import { fileURLToPath } from 'node:url'

const root = path.resolve(fileURLToPath(new URL('..', import.meta.url)))
const binary = path.join(root, 'dist/bin/megalith-frontend')
const sourceMap = path.join(root, 'dist/bin/megalith-frontend.map')
const browserOnlyPackages = ['dompurify', 'happy-dom', 'isomorphic-dompurify', 'jsdom']

const adminMenu = {
  id: 1,
  title: '系统',
  name: 'system',
  icon: '',
  orderNum: 0,
  parentId: 0,
  status: 0,
  type: 0,
  url: '/sys',
  component: 'sys/SystemView',
  children: [
    {
      id: 2,
      title: '用户',
      name: 'system-users',
      icon: '',
      orderNum: 0,
      parentId: 1,
      status: 0,
      type: 1,
      url: '/sys/users',
      component: 'sys/UsersView',
      children: []
    }
  ]
}

/**
 * Element Plus renders these class names without shipping rules for them, so they are not
 * evidence that a component stylesheet is missing.
 */
const unstyledElementPlusClasses = new Set([
  'tab-pane',
  'scrollbar__view',
  'select__icon',
  'tooltip__trigger',
  'calendar__button-group'
])

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

const elementPlusClasses = (html: string): Set<string> => {
  const classes = new Set<string>()
  const body = html.slice(html.indexOf('<body'))
  for (const match of body.matchAll(/class="([^"]*)"/g)) {
    for (const name of match[1]!.split(/\s+/)) {
      if (!name.startsWith('el-')) continue
      const root = name.split('--')[0]!.replace(/^el-/, '')
      if (root && !unstyledElementPlusClasses.has(root)) classes.add(root)
    }
  }
  return classes
}

/**
 * The server HTML has to declare every stylesheet the rendered route needs, otherwise slow
 * connections paint the component markup unstyled before the client CSS arrives.
 */
const assertFirstPaintStyles = async (base: string, route: string, html: string): Promise<void> => {
  const styleHrefs = [...html.matchAll(/<link[^>]*rel="stylesheet"[^>]*href="([^"]+)"/g)].map(
    (match) => match[1]!
  )
  assert.ok(styleHrefs.length > 0, `${route} must declare stylesheets`)
  assert.equal(new Set(styleHrefs).size, styleHrefs.length, `${route} must not repeat stylesheets`)

  const styles: string[] = []
  for (const href of styleHrefs) {
    const stylesheet = await fetch(`${base}${href}`)
    assert.equal(stylesheet.status, 200, `stylesheet ${href} must be served`)
    styles.push(await stylesheet.text())
  }
  const styleText = styles.join('\n')
  for (const className of elementPlusClasses(html)) {
    assert.ok(
      styleText.includes(`.el-${className}`),
      `${route} must declare .el-${className} before the first paint`
    )
  }
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
              title: 'Standalone SSR blog list',
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
    if (url.pathname === '/public/blog/info/standalone-smoke') {
      return Response.json({
        msg: 'OK',
        data: {
          title: 'Standalone SSR blog',
          description: 'Standalone sanitizer smoke test',
          content: '**Rendered by the standalone server**',
          avatar: '',
          readCount: 1,
          nickname: 'SSR',
          created: '2026-08-21'
        }
      })
    }
    if (url.pathname === '/auth/menu/nav') {
      return Response.json({ msg: 'OK', data: adminMenu })
    }
    if (url.pathname === '/token/userinfo') {
      return Response.json({ msg: 'OK', data: { nickname: 'SSR', avatar: '', id: 1 } })
    }
    if (url.pathname.startsWith('/sys/')) {
      return Response.json({
        msg: 'OK',
        data: { content: [], totalElements: 0, pageSize: 5, pageNumber: 1, additional: [] }
      })
    }
    return Response.json({ msg: 'Not Found', data: null }, { status: 404 })
  }
})

const frontendPort = await reservePort()
const runtimeRoot = await mkdtemp(path.join(tmpdir(), 'megalith-frontend-ssr-'))
const child = Bun.spawn([binary], {
  cwd: runtimeRoot,
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
  const { sources } = (await Bun.file(sourceMap).json()) as { sources: string[] }
  for (const packageName of browserOnlyPackages) {
    assert.ok(
      sources.every((source) => !source.includes(`/node_modules/${packageName}/`)),
      `${packageName} must not be included in the standalone server`
    )
  }

  await waitForHealth(`${baseUrl}/actuator/health`, 10_000)

  const intro = await fetch(`${baseUrl}/`)
  assert.equal(intro.status, 200)
  assert.match(intro.headers.get('content-type') || '', /^text\/html/)
  assert.equal(intro.headers.get('cache-control'), 'private, no-store')
  assert.match(await intro.text(), /class="intro-notebook-icon"/)

  const login = await fetch(`${baseUrl}/login`)
  assert.equal(login.status, 200)
  assert.match(await login.text(), /<title>登录<\/title>/)

  const blog = await fetch(`${baseUrl}/blog/standalone-smoke`)
  assert.equal(blog.status, 200)
  assert.match(await blog.text(), /<title>Standalone SSR blog<\/title>/)

  const blogs = await fetch(`${baseUrl}/blogs`)
  assert.equal(blogs.status, 200)
  const blogsHtml = await blogs.text()
  assert.match(blogsHtml, /Standalone SSR blog list/)
  assert.match(blogsHtml, /blogs-skeleton/)
  assert.match(blogsHtml, /style="display:\s*none;?"/)

  await assertFirstPaintStyles(baseUrl, '/blogs', blogsHtml)

  const routes = [
    { path: '/', status: 200 },
    { path: '/blogs', status: 200 },
    { path: '/blog/standalone-smoke', status: 200 },
    { path: '/login', status: 200 },
    { path: '/register/smoke-token', status: 200 },
    { path: '/production-ssr-smoke-not-found', status: 404 },
    { path: '/sys/users', status: 200, cookie: 'megalith_access_token=smoke' }
  ]
  for (const route of routes) {
    const response = await fetch(`${baseUrl}${route.path}`, {
      headers: route.cookie ? { Cookie: route.cookie } : undefined
    })
    assert.equal(response.status, route.status, `${route.path} must render with ${route.status}`)
    await assertFirstPaintStyles(baseUrl, route.path, await response.text())
  }

  const notFound = await fetch(`${baseUrl}/production-ssr-smoke-not-found`)
  assert.equal(notFound.status, 404)
  assert.match(await notFound.text(), /404 NOT FOUND/)

  // An unreachable gateway must not terminate the SSR process: the affected route renders
  // with its default state and every later request still gets a response.
  const unreachablePort = await reservePort()
  const degradedPort = await reservePort()
  const degradedChild = Bun.spawn([binary], {
    cwd: runtimeRoot,
    env: {
      ...process.env,
      NODE_ENV: 'production',
      PORT: String(degradedPort),
      SSR_API_BASE_URL: `http://127.0.0.1:${unreachablePort}`,
      APP_ORIGIN: 'https://chiu.wiki',
      OTEL_SDK_DISABLED: 'true'
    },
    stdin: 'ignore',
    stdout: 'pipe',
    stderr: 'pipe'
  })
  const degradedOutput = [
    new Response(degradedChild.stdout).text(),
    new Response(degradedChild.stderr).text()
  ]
  try {
    await waitForHealth(`http://127.0.0.1:${degradedPort}/actuator/health`, 10_000)
    for (const path of ['/', '/login', '/blogs']) {
      const response = await fetch(`http://127.0.0.1:${degradedPort}${path}`)
      assert.equal(response.status, 200, `${path} must render while the gateway is unreachable`)
      await response.arrayBuffer()
    }
    const health = await fetch(`http://127.0.0.1:${degradedPort}/actuator/health`)
    assert.equal(health.status, 200, 'SSR process must survive an unreachable gateway')
    await health.arrayBuffer()
  } finally {
    degradedChild.kill('SIGKILL')
    await degradedChild.exited
    await Promise.all(degradedOutput)
  }

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
  await rm(runtimeRoot, { recursive: true, force: true })
}
