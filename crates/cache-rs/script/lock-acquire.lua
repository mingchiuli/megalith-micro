if redis.call('exists', KEYS[1]) ~= 0 then return 0 end
redis.call('hset', KEYS[1], ARGV[1], 1)
redis.call('pexpire', KEYS[1], ARGV[2])
return 1
