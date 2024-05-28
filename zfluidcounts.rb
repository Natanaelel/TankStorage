require "./formatters.rb"

DROPLET = 1
NUGGET  = 1000 * DROPLET
INGOT   = 9 * NUGGET
BUCKET  = 9 * INGOT
MILLIBUCKET = BUCKET / 1000
p MILLIBUCKET

def round3(count)
    digits = count.floor.to_s.size
    if digits >= 3
        count.floor
    else
        count % 1 == 0 ? count.floor : count.round(3-digits)
    end
end

def format(count)
    buckets = count.to_f / BUCKET
    count.to_s
    # return "#{buckets / 1000**3}B" if buckets > 1000**3
    return "#{round3 buckets / 1000**2}M" if buckets >= 1000**2
    return "#{round3 buckets / 1000**1}K" if buckets >= 1000**1
    return "#{round3 buckets / 1000**0}B" if buckets >= 1000**0
    return "#{round3 buckets / 1000**-1}mB" if buckets >= 1000**-1 
    return "0B" if buckets == 0
    "what??"
end

def f(count) = "#{(count.to_f/BUCKET).round(5).to_s.rjust(9," ")}-> #{format(count).rjust(8," ")}"
# (0..9).each{|n|
#     puts f n * INGOT
# }
# (0..9).each{|n|
#     puts f n * BUCKET
# }

# (0..6).each{|n|
#     puts f 10 ** n * BUCKET
# }
# (5..7).each{|n|
#     puts f (n*10+n) * BUCKET
# }

# (5..7).each{|n|
#     puts f (n*100+n*10+n) * BUCKET
# }
# (5..7).each{|n|
#     puts f (n*1000+n*100+n*10+n) * BUCKET
# }
# puts f 1011 * BUCKET 

# n = 1234.5678
# puts "%2g" % n





#    droplets |  ae2 |      mi    |    emi  |    jade   |   wthit  | tank? 
#           1 |   ~0 |     0 ¹⁄₈₁ |         |  0 ¹⁄₈₁mB |     0 mb |
#          80 |   ~0 |    0 ⁸⁰⁄₈₁ |         | 0 ⁸⁰⁄₈₁mB |     0 mB |
#          81 | .001 |          1 |     1 L |       1mB |     1 mB |
#         810 |  .01 |         10 |    10 L |      10mB |    10 mB |
#        8100 |   .1 |        100 |   100 L |     100mB |   100 mB |
#       18000 | .222 |  222 ¹⁸⁄₈₁ |   222 L | 222 ²⁄₉mB |   222 mB |
#       81000 |    1 |       1000 |  1000 L |        1B |    1K mB |
#       81081 |   ~1 |       1001 |  1001 L |        1B |    1K mB |
#       81810 | 1.01 |       1010 |  1010 L |        1B | 1.01K mB |
#       81891 | 1.01 |       1011 |  1011 L |        1B | 1.01K mB |
#      810000 |   10 |      10000 | 10000 L |       10B |   10K mB |
#      810081 |  ~10 |      10001 | 10001 L |       10B |   10K mB |
#      810810 |  ~10 |      10010 | 10010 L |       10B |   10K mB |
#      850500 | 10.5 |      10500 | 10500 L |       10B | 10.5K mB |
#     8100000 |  100 |     100000 |         |      100B |  100K mB |
#     8140500 | ~100 |     100500 |         |      100B |  101K mB |
#     8140500 | ~100 |     100500 |         |      100B |  101K mB |
#    81000000 | 1000 |    1000000 |         |       1kB |    1M mB |
#    81040500 | 1000 |    1000500 |         |       1kB |    1M mB |
#    81081000 | 1001 |    1001000 |         |       1kB |    1M mB |
#    81810000 | 1010 |    1010000 |         |    1.01kB | 1.01M mB |
#   810000000 |  10K |   10000000 |         |      10kB |   10M mB |
#   818100000 |  10K |   10100000 |         |    10.1kB | 10.1M mB |
#  8100000000 | 100K |  100000000 |         |     100kB |  100M mB |
# 81000000000 | 1.0M | 1000000000 |         |       1MB |    1G mB |