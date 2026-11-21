-- Hash ownership interoperates with Redisson's RLock and write-lock checks.
if redis.call('exists', KEYS[1]) ~= 0 then return 0 end
redis.call('hset', KEYS[1], ARGV[1], 1)
if ARGV[3] == 'write' then redis.call('hset', KEYS[1], 'mode', 'write') end
redis.call('pexpire', KEYS[1], ARGV[2])
return 1
