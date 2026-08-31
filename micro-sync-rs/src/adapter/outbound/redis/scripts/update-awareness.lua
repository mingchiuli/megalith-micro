local previous_clock = tonumber(redis.call('HGET', KEYS[3], ARGV[2]) or '-1')
local incoming_clock = tonumber(ARGV[3])
if incoming_clock < previous_clock or
   (incoming_clock == previous_clock and
    (ARGV[4] ~= '1' or redis.call('HEXISTS', KEYS[1], ARGV[2]) == 0)) then
  return 0
end
redis.call('HSET', KEYS[3], ARGV[2], incoming_clock)
if ARGV[4] == '1' then
  redis.call('HDEL', KEYS[1], ARGV[2])
  redis.call('HDEL', KEYS[2], ARGV[2])
  redis.call('HDEL', KEYS[4], ARGV[2])
  redis.call('HDEL', KEYS[5], ARGV[2])
else
  redis.call('HSET', KEYS[1], ARGV[2], ARGV[5])
  redis.call('HSET', KEYS[2], ARGV[2], ARGV[1])
  redis.call('HSET', KEYS[4], ARGV[2], ARGV[2])
  redis.call('HSET', KEYS[5], ARGV[2], ARGV[6])
end
redis.call('XADD', KEYS[6], '*', 'kind', 'a', 'origin', ARGV[1], 'payload', ARGV[5])
redis.call('PEXPIRE', KEYS[1], ARGV[7])
redis.call('PEXPIRE', KEYS[2], ARGV[7])
redis.call('PEXPIRE', KEYS[3], ARGV[7])
redis.call('PEXPIRE', KEYS[4], ARGV[7])
redis.call('PEXPIRE', KEYS[5], ARGV[7])
redis.call('PEXPIRE', KEYS[6], ARGV[7])
return 1
