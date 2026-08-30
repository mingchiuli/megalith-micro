local redis_time = redis.call('TIME')
local now_ms = tonumber(redis_time[1]) * 1000 + math.floor(tonumber(redis_time[2]) / 1000)
local ids = redis.call('ZRANGEBYSCORE', KEYS[1], '-inf', now_ms, 'LIMIT', 0, ARGV[1])
local result = {}
for _, id in ipairs(ids) do
  local room = redis.call('HGET', KEYS[2], id)
  if room then
    table.insert(result, id)
    table.insert(result, room)
  else
    redis.call('ZREM', KEYS[1], id)
  end
end
return result
