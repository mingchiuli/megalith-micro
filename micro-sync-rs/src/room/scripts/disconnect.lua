local score = redis.call('ZSCORE', KEYS[1], ARGV[1])
local redis_time = redis.call('TIME')
local now_ms = tonumber(redis_time[1]) * 1000 + math.floor(tonumber(redis_time[2]) / 1000)
if ARGV[2] == '1' and score and tonumber(score) > now_ms then
  return 0
end
local owned = redis.call('HKEYS', KEYS[3])
for _, field in ipairs(owned) do
  local client = redis.call('HGET', KEYS[3], field)
  local payload = redis.call('HGET', KEYS[4], field)
  if client and payload and redis.call('HGET', KEYS[6], client) == ARGV[1] then
    redis.call('HDEL', KEYS[5], client)
    redis.call('HDEL', KEYS[6], client)
    local current_clock = tonumber(redis.call('HGET', KEYS[7], client) or '-1')
    redis.call('HSET', KEYS[7], client, math.min(current_clock + 1, 4294967295))
    redis.call('XADD', KEYS[8], '*', 'kind', 'a', 'origin', ARGV[1], 'payload', payload)
  end
end
redis.call('DEL', KEYS[3])
redis.call('DEL', KEYS[4])
redis.call('ZREM', KEYS[1], ARGV[1])
redis.call('HDEL', KEYS[2], ARGV[1])
redis.call('ZREM', KEYS[9], ARGV[1])
redis.call('ZREMRANGEBYSCORE', KEYS[9], '-inf', now_ms)
local remaining = redis.call('ZCARD', KEYS[9])
if remaining == 0 then
  for i = 5, 10 do redis.call('PEXPIRE', KEYS[i], ARGV[3]) end
else
  for i = 5, 10 do redis.call('PEXPIRE', KEYS[i], ARGV[4]) end
end
return 1
