if redis.call('EXISTS', KEYS[1], KEYS[2]) ~= 2 then return -1 end
return redis.call('GETBIT', KEYS[1], ARGV[1])
