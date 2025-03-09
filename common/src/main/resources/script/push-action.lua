local key = KEYS[1]

-- 检查键是否存在
if redis.call('exists', key) == 0 then return -2 end

-- 检查权限
local status = redis.call('hget', key, 'status')
local uid = redis.call('hget', key, 'userId')
local userId = ARGV[8]

if tonumber(status) ~= 0 and tonumber(uid) ~= tonumber(userId) then return nil end

-- 检查版本
local v = redis.call('hget', key, 'version')
local version = ARGV[3] and tonumber(ARGV[3]) or nil

if not version then return -2 end
if tonumber(v) + 1 < version then return -1 end
if tonumber(v) + 1 > version then return -2 end

-- UTF-8字符串处理函数
local function getUTF8CharLen(str, index)
    local byte = string.byte(str, index)
    if not byte then
        return 0
    elseif byte <= 127 then
        return 1
    elseif byte >= 192 and byte <= 223 then
        return 2
    elseif byte >= 224 and byte <= 239 then
        return 3
    elseif byte >= 240 and byte <= 247 then
        return 4
    else
        return 1 -- 处理无效字节
    end
end

-- 获取UTF-8字符串的字符数
local function getUTF8Length(str)
    local len = 0
    local i = 1
    while i <= #str do
        local charLen = getUTF8CharLen(str, i)
        if charLen == 0 then break end
        i = i + charLen
        len = len + 1
    end
    return len
end

-- 获取UTF-8字符串中指定字符索引对应的字节索引
local function getUTF8ByteIndex(str, charIndex)
    local curIndex = 0
    local i = 1
    while i <= #str do
        curIndex = curIndex + 1
        if curIndex >= charIndex then return i end
        local charLen = getUTF8CharLen(str, i)
        if charLen == 0 then break end
        i = i + charLen
    end
    return i
end

-- UTF-8子字符串提取
local function subStringUTF8(str, startIndex, endIndex)
    if not str then return "" end

    if startIndex < 0 then
        startIndex = getUTF8Length(str) + startIndex + 1
    end

    if endIndex and endIndex < 0 then
        endIndex = getUTF8Length(str) + endIndex + 1
    end

    local startByte = getUTF8ByteIndex(str, startIndex)

    if not endIndex then
        return string.sub(str, startByte)
    else
        local endByte = getUTF8ByteIndex(str, endIndex + 1) - 1
        return string.sub(str, startByte, endByte)
    end
end

-- 初始化参数
local contentChange = ARGV[1]
local operateTypeCode = ARGV[2] and tonumber(ARGV[2])
local indexStart = ARGV[4] and tonumber(ARGV[4])
local indexEnd = ARGV[5] and tonumber(ARGV[5])
local field = ARGV[6]
local paraNo = ARGV[7] and tonumber(ARGV[7])

-- 获取指定字段的内容
local function getFieldContent(fieldName)
    return redis.call('hget', key, fieldName) or ''
end

-- 更新指定字段并同时更新版本号
local function updateField(fieldName, newContent)
    return redis.call('hset', key, fieldName, newContent, 'version', version)
end

-- 如果是段落相关操作，确定正确的字段名
local function getParaField()
    return 'para::' .. paraNo
end

-- 处理更新状态的情况
if operateTypeCode == -1 then
    return updateField('status', contentChange)
end

-- 根据操作类型执行不同的逻辑
local fieldName = (operateTypeCode >= 6 and operateTypeCode <= 13) and getParaField() or field
local content = getFieldContent(fieldName)

-- 增、删、改操作处理
if operateTypeCode == 0 or operateTypeCode == 6 then
    -- 追加内容
    return updateField(fieldName, content .. contentChange)
elseif operateTypeCode == 1 or operateTypeCode == 7 then
    -- 删除尾部
    return updateField(fieldName, subStringUTF8(content, 1, indexStart))
elseif operateTypeCode == 2 or operateTypeCode == 8 then
    -- 头部添加内容
    return updateField(fieldName, contentChange .. content)
elseif operateTypeCode == 3 or operateTypeCode == 9 then
    -- 删除头部
    return updateField(fieldName, subStringUTF8(content, indexStart + 1))
elseif operateTypeCode == 4 or operateTypeCode == 10 then
    -- 替换部分内容
    return updateField(fieldName,
        subStringUTF8(content, 1, indexStart) .. contentChange .. subStringUTF8(content, indexEnd + 1))
elseif operateTypeCode == 5 or operateTypeCode == 11 then
    -- 清空内容
    return updateField(fieldName, '')
elseif operateTypeCode == 12 then
    -- 处理段落分割：删除前一段落末尾的换行符并创建新段落
    local prevParaField = 'para::' .. (paraNo - 1)
    local prevContent = getFieldContent(prevParaField)
    return redis.call('hset', key,
        prevParaField, string.sub(prevContent, 1, #prevContent - 1),
        fieldName, '',
        'version', version)
elseif operateTypeCode == 13 then
    -- 合并段落：将当前段落与前一段落合并
    local prevParaField = 'para::' .. (paraNo - 1)
    local prevContent = getFieldContent(prevParaField)
    redis.call('hdel', key, fieldName)
    return updateField(prevParaField, prevContent .. '\n')
elseif operateTypeCode == 14 then
    -- 更新敏感内容列表
    return updateField('sensitiveContentList', contentChange)
end

return -3 -- 未知操作类型
