local username = redis.call('GET', KEYS[1])
if not username or username ~= ARGV[1] then
  return 0
end
return redis.call('DEL', KEYS[1])
