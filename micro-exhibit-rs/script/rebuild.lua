local operation = ARGV[1]
if operation == 'begin' then
    if redis.call('EXISTS', KEYS[2], KEYS[3]) == 2 then return 0 end
    if not redis.call('SET', KEYS[1], ARGV[2], 'NX', 'PX', 30000) then return 0 end
    redis.call('DEL', KEYS[3], KEYS[4])
    redis.call('SETBIT', KEYS[4], 0, 1)
    return 1
end
if redis.call('GET', KEYS[1]) ~= ARGV[2] then return 0 end
if operation == 'batch' then
    redis.call('PEXPIRE', KEYS[1], 30000)
    for i = 3,#ARGV do redis.call('SETBIT', KEYS[4], ARGV[i], 1) end
elseif operation == 'publish' then
    redis.call('RENAME', KEYS[4], KEYS[2])
    redis.call('SET', KEYS[3], ARGV[2])
    redis.call('DEL', KEYS[1])
elseif operation == 'abort' then redis.call('DEL', KEYS[1], KEYS[4])
end
return 1
