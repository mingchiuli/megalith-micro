local ids = cjson.decode(ARGV[1])
local resMap = {}
for i = 1, #ids do
    local id = ids[i]
    local readCount = redis.call('zscore', KEYS[1], id)
    if readCount then
        table.insert(resMap, id)
        table.insert(resMap, readCount)
    else
        table.insert(resMap, id)
        table.insert(resMap, '0')
    end
end
return resMap
