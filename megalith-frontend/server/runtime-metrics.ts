import { monitorEventLoopDelay } from 'node:perf_hooks'
import { metrics } from '@opentelemetry/api'
import { heapStats } from 'bun:jsc'

const secondsFromNanoseconds = (value: number): number =>
  Number.isFinite(value) ? value / 1_000_000_000 : 0

export const registerBunRuntimeMetrics = (serviceName: string, serviceVersion: string): void => {
  const meter = metrics.getMeter(`${serviceName}.bun-runtime`, serviceVersion)
  const cpuUser = meter.createObservableCounter('bun.runtime.cpu.user', { unit: 's' })
  const cpuSystem = meter.createObservableCounter('bun.runtime.cpu.system', { unit: 's' })
  const rss = meter.createObservableGauge('bun.runtime.memory.rss', { unit: 'By' })
  const uptime = meter.createObservableGauge('bun.runtime.uptime', { unit: 's' })
  const heapSize = meter.createObservableGauge('bun.runtime.jsc.heap.size', { unit: 'By' })
  const heapCapacity = meter.createObservableGauge('bun.runtime.jsc.heap.capacity', { unit: 'By' })
  const extraMemory = meter.createObservableGauge('bun.runtime.jsc.memory.extra', { unit: 'By' })
  const objectCount = meter.createObservableGauge('bun.runtime.jsc.object.count', {
    unit: '{object}'
  })
  const eventLoopMean = meter.createObservableGauge('bun.runtime.event_loop.delay.mean', {
    unit: 's'
  })
  const eventLoopMax = meter.createObservableGauge('bun.runtime.event_loop.delay.max', {
    unit: 's'
  })
  const eventLoopP50 = meter.createObservableGauge('bun.runtime.event_loop.delay.p50', {
    unit: 's'
  })
  const eventLoopP90 = meter.createObservableGauge('bun.runtime.event_loop.delay.p90', {
    unit: 's'
  })
  const eventLoopP99 = meter.createObservableGauge('bun.runtime.event_loop.delay.p99', {
    unit: 's'
  })
  const eventLoopDelay = monitorEventLoopDelay({ resolution: 20 })
  eventLoopDelay.enable()

  const instruments = [
    cpuUser,
    cpuSystem,
    rss,
    uptime,
    heapSize,
    heapCapacity,
    extraMemory,
    objectCount,
    eventLoopMean,
    eventLoopMax,
    eventLoopP50,
    eventLoopP90,
    eventLoopP99
  ]

  meter.addBatchObservableCallback((result) => {
    const cpu = process.cpuUsage()
    const heap = heapStats()
    result.observe(cpuUser, cpu.user / 1_000_000)
    result.observe(cpuSystem, cpu.system / 1_000_000)
    result.observe(rss, process.memoryUsage.rss())
    result.observe(uptime, process.uptime())
    result.observe(heapSize, heap.heapSize)
    result.observe(heapCapacity, heap.heapCapacity)
    result.observe(extraMemory, heap.extraMemorySize)
    result.observe(objectCount, heap.objectCount)
    result.observe(eventLoopMean, secondsFromNanoseconds(eventLoopDelay.mean))
    result.observe(eventLoopMax, secondsFromNanoseconds(eventLoopDelay.max))
    result.observe(eventLoopP50, secondsFromNanoseconds(eventLoopDelay.percentile(50)))
    result.observe(eventLoopP90, secondsFromNanoseconds(eventLoopDelay.percentile(90)))
    result.observe(eventLoopP99, secondsFromNanoseconds(eventLoopDelay.percentile(99)))
  }, instruments)
}
