redis.call('ltrim', KEYS[1], ARGV[1], ARGV[2])
redis.call('rpush', KEYS[1], ARGV[3])
redis.call('expire', KEYS[1], ARGV[4])
