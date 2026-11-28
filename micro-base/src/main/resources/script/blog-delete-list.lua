local removed = 0
for i = 1, #ARGV do
  removed = removed + redis.call('LREM', KEYS[1], 1, ARGV[i])
end
return removed
