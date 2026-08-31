local revision = tonumber(redis.call('HGET', KEYS[1], 'revision') or '0')
if revision ~= tonumber(ARGV[1]) then return 0 end
local redis_time = redis.call('TIME')
local now_ms = tonumber(redis_time[1]) * 1000 + math.floor(tonumber(redis_time[2]) / 1000)
redis.call('ZREMRANGEBYSCORE', KEYS[4], '-inf', now_ms)
local active_leases = redis.call('ZCARD', KEYS[4])
local stream_ttl = redis.call('PTTL', KEYS[3])
if active_leases == 0 and stream_ttl == -2 then
  redis.call('DEL', KEYS[1])
  redis.call('DEL', KEYS[2])
  return 1
end
redis.call('HSET', KEYS[1], 'revision', revision + 1, 'through', ARGV[2], 'update', ARGV[3])
redis.call('DEL', KEYS[2])
if active_leases == 0 then
  local inactive_ttl = stream_ttl
  if inactive_ttl < 0 or inactive_ttl > tonumber(ARGV[4]) then inactive_ttl = tonumber(ARGV[4]) end
  redis.call('PEXPIRE', KEYS[1], inactive_ttl)
else
  redis.call('PEXPIRE', KEYS[1], ARGV[5])
end
local safety_ms = now_ms - tonumber(ARGV[6])
local through_ms = tonumber(string.match(ARGV[2], '^(%d+)-')) or 0
local trim_ms = math.min(safety_ms, through_ms)
if trim_ms > 0 then redis.call('XTRIM', KEYS[3], 'MINID', '~', trim_ms .. '-0') end
return 1
