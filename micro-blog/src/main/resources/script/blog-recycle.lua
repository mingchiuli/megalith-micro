if redis.call('set', KEYS[2], '1', 'NX', 'EX', ARGV[2]) then
  redis.call('rpush', KEYS[1], ARGV[1])
  redis.call('expire', KEYS[1], ARGV[2])
  return 1
end
return 0
