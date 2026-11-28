local clock = redis.call('TIME')
local now = tonumber(clock[1]) * 1000 + math.floor(tonumber(clock[2]) / 1000)
local expired = redis.call('ZRANGEBYSCORE', KEYS[2], '-inf', now)
for _, group in ipairs(expired) do
    if redis.call('EXISTS', KEYS[1]) == 1 then redis.call('XGROUP', 'DESTROY', KEYS[1], group) end
    redis.call('ZREM', KEYS[2], group)
end
local created = redis.pcall('XGROUP', 'CREATE', KEYS[1], ARGV[1], '0', 'MKSTREAM')
if created.err and not string.find(created.err, 'BUSYGROUP', 1, true) then return created end
redis.call('ZADD', KEYS[2], now + 30000, ARGV[1])
-- Redis 8 retains stream entries until all registered groups have acknowledged them.
redis.call('XTRIM', KEYS[1], 'MAXLEN', '=', 0, 'ACKED')
return created.err and 0 or 1
