redis.call('XADD', KEYS[1], '*', 'kind', ARGV[1], 'origin', ARGV[2], 'payload', ARGV[3])
redis.call('PEXPIRE', KEYS[1], ARGV[4])
redis.call('PEXPIRE', KEYS[2], ARGV[4])
redis.call('PEXPIRE', KEYS[3], ARGV[4])
redis.call('PEXPIRE', KEYS[4], ARGV[4])
if ARGV[1] == 'd' and redis.call('SET', KEYS[5], '1', 'NX', 'PX', ARGV[5]) then
  redis.call('XADD', KEYS[6], '*', 'room', ARGV[6])
end
return 1
