local key = KEYS[1]

local exist = redis.call('exists', key)

if exist == 0 then return -2 end

local status = redis.call('hget', key, 'status')
local uid = redis.call('hget', key, 'userId')
local userId = ARGV[8]

if tonumber(status) ~= 0 and tonumber(uid) ~= tonumber(userId) then return nil end

local v = redis.call('hget', key, 'version')
local version
if ARGV[3] then
    version = tonumber(ARGV[3])
end

if tonumber(v) + 1 < version then return -1 end
if tonumber(v) + 1 > version then return -2 end

local function subStringGetByteCount(str, index)
    local curByte = string.byte(str, index)
    local byteCount = 1
    if curByte == nil then
        byteCount = 0
    elseif curByte > 0 and curByte <= 127 then
        byteCount = 1
    elseif curByte >= 192 and curByte <= 223 then
        byteCount = 2
    elseif curByte >= 224 and curByte <= 239 then
        byteCount = 3
    elseif curByte >= 240 and curByte <= 247 then
        byteCount = 4
    end
    return byteCount
end

local function subStringGetTotalIndex(str)
    local curIndex = 0
    local i = 1
    local lastCount = 1
    repeat
        lastCount = subStringGetByteCount(str, i)
        i = i + lastCount
        curIndex = curIndex + 1
    until(lastCount == 0)
    return curIndex - 1
end

local function subStringGetTrueIndex(str, index)
    local curIndex = 0
    local i = 1
    local lastCount = 1
    repeat
        lastCount = subStringGetByteCount(str, i)
        i = i + lastCount
        curIndex = curIndex + 1
    until(curIndex >= index)
    return i - lastCount
end


local function subStringUTF8(str, startIndex, endIndex)
    if startIndex < 0 then
        startIndex = subStringGetTotalIndex(str) + startIndex + 1
    end

    if endIndex ~= nil and endIndex < 0 then
        endIndex = subStringGetTotalIndex(str) + endIndex + 1
    end

    if endIndex == nil then
        return string.sub(str, subStringGetTrueIndex(str, startIndex))
    else
        return string.sub(str, subStringGetTrueIndex(str, startIndex), subStringGetTrueIndex(str, endIndex + 1) - 1)
    end
end

local contentChange = ARGV[1]

local operateTypeCode
if ARGV[2] then
    operateTypeCode = tonumber(ARGV[2])
end


local indexStart
if ARGV[4] then
    indexStart = tonumber(ARGV[4])
end

local indexEnd
if ARGV[5] then
    indexEnd = tonumber(ARGV[5])
end

local field = ARGV[6]

local paraNo
if ARGV[7] then
    paraNo = tonumber(ARGV[7])
end

local allPairs = redis.call('hgetall', key)

local content = ''

if operateTypeCode == -1 then
    return redis.call('hset', key, 'status', contentChange, 'version', version)
end

if operateTypeCode == 0 then
    for i = 1, #allPairs, 2 do
        if (allPairs[i] == field) then
            content = allPairs[i + 1]
        end
    end
    return redis.call('hset', key, field, content .. contentChange, 'version', version)
end

if operateTypeCode == 1 then
    for i = 1, #allPairs, 2 do
        if (allPairs[i] == field) then
            content = allPairs[i + 1]
        end
    end
    return redis.call('hset', key, field, subStringUTF8(content, 1, indexStart), 'version', version)
end

if operateTypeCode == 2 then
    for i = 1, #allPairs, 2 do
        if (allPairs[i] == field) then
            content = allPairs[i + 1]
        end
    end
    return redis.call('hset', key, field, contentChange .. content, 'version', version)
end

if operateTypeCode == 3 then
    for i = 1, #allPairs, 2 do
        if (allPairs[i] == field) then
            content = allPairs[i + 1]
        end
    end
    return redis.call('hset', key, field, subStringUTF8(content, indexStart + 1, subStringGetTotalIndex(content)), 'version', version)
end

if operateTypeCode == 4 then
    for i = 1, #allPairs, 2 do
        if (allPairs[i] == field) then
            content = allPairs[i + 1]
        end
    end
    return redis.call('hset', key, field, subStringUTF8(content, 1, indexStart) .. contentChange .. subStringUTF8(content, indexEnd + 1, subStringGetTotalIndex(content)), 'version', version)
end

if operateTypeCode == 5 then
    for i = 1, #allPairs, 2 do
        if (allPairs[i] == field) then
            content = allPairs[i + 1]
        end
    end
    return redis.call('hset', key, field, '', 'version', version)
end
if operateTypeCode == 6 then
    for i = 1, #allPairs, 2 do
        if (allPairs[i] == ('para::' .. paraNo)) then
            content = allPairs[i + 1]
        end
    end
    return redis.call('hset', key, 'para::' .. paraNo, content .. contentChange, 'version', version)
end

if operateTypeCode == 7 then
    for i = 1, #allPairs, 2 do
        if (allPairs[i] == ('para::' .. paraNo)) then
            content = allPairs[i + 1]
        end
    end
    return redis.call('hset', key, 'para::' .. paraNo, subStringUTF8(content, 1, indexStart), 'version', version)
end

if operateTypeCode == 8 then
    for i = 1, #allPairs, 2 do
        if (allPairs[i] == ('para::' .. paraNo)) then
            content = allPairs[i + 1]
        end
    end
    return redis.call('hset', key, 'para::' .. paraNo, contentChange .. content, 'version', version)
end

if operateTypeCode == 9 then
    for i = 1, #allPairs, 2 do
        if (allPairs[i] == ('para::' .. paraNo)) then
            content = allPairs[i + 1]
        end
    end
    return redis.call('hset', key, 'para::' .. paraNo, subStringUTF8(content, indexStart + 1, subStringGetTotalIndex(content)), 'version', version)
end

if operateTypeCode == 10 then
    for i = 1, #allPairs, 2 do
        if (allPairs[i] == ('para::' .. paraNo)) then
            content = allPairs[i + 1]
        end
    end
    return redis.call('hset', key, 'para::' .. paraNo, subStringUTF8(content, 1, indexStart) .. contentChange .. subStringUTF8(content, indexEnd + 1, subStringGetTotalIndex(content)), 'version', version)
end

if operateTypeCode == 11 then
    for i = 1, #allPairs, 2 do
        if (allPairs[i] == ('para::' .. paraNo)) then
            content = allPairs[i + 1]
        end
    end
    return redis.call('hset', key, 'para::' .. paraNo, '', 'version', version)
end

if operateTypeCode == 12 then
    for i = 1, #allPairs, 2 do
        if (allPairs[i] == ('para::' .. (paraNo - 1))) then
            content = allPairs[i + 1]
        end
    end
    return redis.call('hset', key, 'para::' .. (paraNo - 1), string.sub(content, 1, string.len(content) - 1), 'para::' .. paraNo, '', 'version', version)
end

if operateTypeCode == 13 then
    for i = 1, #allPairs, 2 do
        if (allPairs[i] == ('para::' .. (paraNo - 1))) then
            content = allPairs[i + 1]
        end
    end
    redis.call('hdel', key, 'para::' .. paraNo)
    return redis.call('hset', key, 'para::' .. (paraNo - 1), content .. '\n', 'version', version)
end

if operateTypeCode == 14 then
    return redis.call('hset', key, 'sensitiveContentList', contentChange, 'version', version)
end