// @vitest-environment node
import { IncomingMessage, ServerResponse } from 'node:http'
import { Socket } from 'node:net'
import type { Connect, MinimalPluginContextWithoutEnvironment, ViteDevServer } from 'vite'
import { describe, expect, it, vi } from 'vitest'
import { megalithSsrDevPlugin } from '../../server/dev'

describe('development SSR middleware', () => {
  it('forwards rejected requests to Connect instead of dropping the promise', async () => {
    const use = vi.fn<(handler: Connect.NextHandleFunction) => void>()
    const vite = {
      watcher: { on: vi.fn() },
      middlewares: { use }
    } as unknown as ViteDevServer
    const configure = megalithSsrDevPlugin().configureServer
    if (typeof configure !== 'function') throw new Error('Missing development server hook')
    const install = await configure.call({} as MinimalPluginContextWithoutEnvironment, vite)
    if (typeof install !== 'function') throw new Error('Missing SSR middleware installer')
    install()
    const handler = use.mock.calls[0]?.[0]
    if (!handler) throw new Error('SSR middleware was not installed')

    const request = new IncomingMessage(new Socket())
    request.headers.host = '['
    request.url = '/'
    const response = new ServerResponse(request)
    const error = new Promise<unknown>((resolve) => handler(request, response, resolve))

    await expect(error).resolves.toBeInstanceOf(TypeError)
    expect(response.writableEnded).toBe(false)
  })
})
