local ttl =  redis.call('ttl', KEYS[1])
if (ttl == -2) then return 0 end
redis.call('hincrby', KEYS[1], ARGV[1], 1)
redis.call('expire', KEYS[1], ttl)
return ttl