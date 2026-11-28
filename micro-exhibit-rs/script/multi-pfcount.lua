local resp = {}

for i = 1, #KEYS do
    local ipCount = redis.call('pfcount', KEYS[i])
    resp[i] = ipCount
end

return resp
