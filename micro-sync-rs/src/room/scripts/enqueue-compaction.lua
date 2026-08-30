if redis.call('SET', KEYS[1], '1', 'NX', 'PX', ARGV[1]) then
  redis.call('XADD', KEYS[2], '*', 'room', ARGV[2])
  return 1
end
return 0
