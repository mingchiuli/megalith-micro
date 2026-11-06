if redis.call('EXISTS', KEYS[1], KEYS[2]) > 0 then return 0 end
redis.call('HSET', KEYS[1], 'revision', 0, 'through', '0-0', 'update', ARGV[1])
redis.call('PEXPIRE', KEYS[1], ARGV[2])
return 1
