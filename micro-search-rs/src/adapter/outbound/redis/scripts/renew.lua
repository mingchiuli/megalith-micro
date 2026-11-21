if redis.call('hexists', KEYS[1], ARGV[1]) == 0 then return 0 end
return redis.call('pexpire', KEYS[1], ARGV[2])
