
local daySize = redis.call('pfcount', KEYS[1])
local weekSize = redis.call('pfcount', KEYS[2])
local monthSize = redis.call('pfcount', KEYS[3])
local yearSize = redis.call('pfcount', KEYS[4])
local resp = {}
resp[1] = daySize
resp[2] = weekSize
resp[3] = monthSize
resp[4] = yearSize
return resp