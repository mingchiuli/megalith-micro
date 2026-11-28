local clock = redis.call('TIME')
local now = tonumber(clock[1]) * 1000 + math.floor(tonumber(clock[2]) / 1000)
local deadline = math.min(assert(tonumber(ARGV[3])), now + tonumber(ARGV[2]))
if deadline <= now then return 0 end
redis.call('SET', KEYS[1], ARGV[1], 'PXAT', deadline)
return 1
