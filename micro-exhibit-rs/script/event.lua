-- Redis serializes both the revision check and the matching state transition.
if redis.call('EXISTS', KEYS[6]) == 1 then return 0 end
local revision = ARGV[1]
local previous = redis.call('GET', KEYS[1])
if previous and (#previous > #revision or (#previous == #revision and previous >= revision)) then return 1 end
local operation = ARGV[2]
if operation == '3' or (operation == '2' and ARGV[4] == '0') then redis.call('DEL', KEYS[4]) end
if operation == '3' then redis.call('ZREM', KEYS[5], ARGV[3]) end
if operation == '1' or operation == '3' then
    if redis.call('EXISTS', KEYS[2], KEYS[3]) == 2 then
        redis.call('SETBIT', KEYS[2], ARGV[3], operation == '1' and 1 or 0)
    else redis.call('DEL', KEYS[3]) end
end
redis.call('SET', KEYS[1], revision, 'EX', 2592000)
return 1
