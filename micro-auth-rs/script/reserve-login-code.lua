if redis.call('EXISTS', KEYS[1]) == 1 then return 0 end
redis.call('HSET', KEYS[1], ARGV[1], ARGV[2], ARGV[3], '0', 'reservation', ARGV[5])
redis.call('EXPIRE', KEYS[1], ARGV[4])
return 1
