local clock = redis.call('TIME')
local now = tonumber(clock[1]) * 1000 + math.floor(tonumber(clock[2]) / 1000)
local previous = assert(tonumber(redis.call('ZSCORE', KEYS[1], ARGV[1]) or '0'))
if ARGV[3] == 'renew' and previous <= now then return {} end
local deadline = math.max(previous, now + tonumber(ARGV[2]) * 2)
redis.call('ZADD', KEYS[1], deadline, ARGV[1])
local maximum = redis.call('ZRANGE', KEYS[1], -1, -1, 'WITHSCORES')
redis.call('PEXPIREAT', KEYS[1], tonumber(maximum[2]) + 1000)
return {previous == 0 and 1 or 0, deadline, deadline - now}
