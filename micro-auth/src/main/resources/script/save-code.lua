redis.call('hmset', KEYS[1], ARGV[1], ARGV[2], ARGV[3], ARGV[4])
redis.call('expire', KEYS[1], ARGV[5])