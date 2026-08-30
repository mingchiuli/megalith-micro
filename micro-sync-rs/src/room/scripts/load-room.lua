local snapshot = redis.call('HMGET', KEYS[1], 'through', 'update')
local through = snapshot[1] or '0-0'
local update = snapshot[2] or ''
local events = redis.call('XRANGE', KEYS[2], '(' .. through, '+')
local awareness = redis.call('HVALS', KEYS[3])
return {through, update, events, awareness}
