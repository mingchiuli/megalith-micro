redis.call('zremrangebyscore', KEYS[1], '-inf', ARGV[1])
redis.call('zadd', KEYS[1], ARGV[2], ARGV[3])
redis.call('pexpire', KEYS[1], ARGV[4])
return redis.call('zcard', KEYS[1])
