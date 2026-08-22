import { describe, expect, it, vi } from 'vitest'
import { useLatestRequest } from '@/composables/useLatestRequest'

const deferred = <T>() => {
  let resolve!: (value: T) => void
  let reject!: (reason?: unknown) => void
  const promise = new Promise<T>((resolvePromise, rejectPromise) => {
    resolve = resolvePromise
    reject = rejectPromise
  })
  return { promise, resolve, reject }
}

describe('useLatestRequest', () => {
  it('only applies the latest response', async () => {
    const loading = ref(false)
    const apply = vi.fn()
    const first = deferred<string>()
    const second = deferred<string>()
    const { runLatest } = useLatestRequest(loading)

    const firstRun = runLatest(() => first.promise, apply)
    const secondRun = runLatest(() => second.promise, apply)
    first.resolve('old')
    await firstRun
    expect(apply).not.toHaveBeenCalled()
    expect(loading.value).toBe(true)

    second.resolve('new')
    await secondRun
    expect(apply).toHaveBeenCalledOnce()
    expect(apply).toHaveBeenCalledWith('new')
    expect(loading.value).toBe(false)
  })

  it('releases loading when the latest request fails', async () => {
    const loading = ref(false)
    const request = deferred<string>()
    const { runLatest } = useLatestRequest(loading)
    const run = runLatest(() => request.promise, vi.fn())
    request.reject(new Error('failed'))

    await expect(run).rejects.toThrow('failed')
    expect(loading.value).toBe(false)
  })
})
