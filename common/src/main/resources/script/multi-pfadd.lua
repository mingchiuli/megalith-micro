for i = 1, #KEYS do
    redis.call('pfadd', KEYS[i], ARGV[i])
end
