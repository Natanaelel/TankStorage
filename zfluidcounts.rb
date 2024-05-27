DROPLET = 1
NUGGET  = 1000 * DROPLET
INGOT   = 9 * NUGGET
BUCKET  = 9 * INGOT

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
(0..9).each{|n|
    puts f n * INGOT
}
(0..9).each{|n|
    puts f n * BUCKET
}

(0..6).each{|n|
    puts f 10 ** n * BUCKET
}
(5..7).each{|n|
    puts f (n*10+n) * BUCKET
}

(5..7).each{|n|
    puts f (n*100+n*10+n) * BUCKET
}
(5..7).each{|n|
    puts f (n*1000+n*100+n*10+n) * BUCKET
}
puts f 1011 * BUCKET 

n = 1234.5678
puts "%2g" % n