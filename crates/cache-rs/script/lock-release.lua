if redis.call('hexists', KEYS[1], ARGV[1]) == 0 then return 0 end
redis.call('del', KEYS[1])
redis.call('publish', KEYS[2], 0)
return 1
