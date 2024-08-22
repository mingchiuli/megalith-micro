local str =  redis.call('lindex', KEYS[1], ARGV[1])
redis.call('lrem', KEYS[1], '1', str)
return str