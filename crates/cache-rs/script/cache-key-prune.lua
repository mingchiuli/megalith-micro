local clock = redis.call('TIME')
local now = tonumber(clock[1]) * 1000 + math.floor(tonumber(clock[2]) / 1000)
local expired = redis.call('ZRANGEBYSCORE', KEYS[1], '-inf', now, 'LIMIT', 0, 256)
if #expired > 0 then redis.call('ZREM', KEYS[1], unpack(expired)) end
if ARGV[1] == 'list' then return redis.call('ZRANGEBYSCORE', KEYS[1], '(' .. now, '+inf') end
return expired
