redis.call('ltrim', KEYS[1], ARGV[1], ARGV[2])
local total = redis.call('llen', KEYS[1])
local resp = redis.call('lrange', KEYS[1], ARGV[4], ARGV[3] + ARGV[4])
local len = #resp
resp[len + 1] = tostring(total)
return resp