if redis.call('hexists', KEYS[1], ARGV[1]) == 0 then return 0 end
redis.call('del', KEYS[1])
redis.call('publish', ARGV[2], ARGV[3])
return 1
