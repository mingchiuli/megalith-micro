if redis.call('EXISTS', KEYS[1]) == 1 then redis.call('XGROUP', 'DESTROY', KEYS[1], ARGV[1]) end
return redis.call('ZREM', KEYS[2], ARGV[1])
