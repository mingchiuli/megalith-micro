if redis.call('EXISTS', KEYS[1]) == 0 then return 'NOT_FOUND' end
if redis.call('PTTL', KEYS[1]) <= 0 then
  redis.call('DEL', KEYS[1])
  return 'EXPIRED'
end
local attempts = tonumber(redis.call('HGET', KEYS[1], ARGV[2]) or '0')
local maximum = tonumber(ARGV[4])
if attempts >= maximum then
  redis.call('DEL', KEYS[1])
  return 'ATTEMPTS_EXCEEDED'
end
local expected = redis.call('HGET', KEYS[1], ARGV[1])
local presented = ARGV[3]
if ARGV[5] == '1' and expected then
  expected = string.lower(expected)
  presented = string.lower(presented)
end
if expected == presented then
  redis.call('DEL', KEYS[1])
  return 'ACCEPTED'
end
attempts = redis.call('HINCRBY', KEYS[1], ARGV[2], 1)
if attempts >= maximum then
  redis.call('DEL', KEYS[1])
  return 'ATTEMPTS_EXCEEDED'
end
return 'MISMATCH'
