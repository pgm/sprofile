# SProfile - A toolkit for creating sampling profiles

## Background 

Sprofile (still looking for a better name) is a library for embedding a
sampling profiler in a JVM process and visualizing/exploring the
results. 

In the past I'd used YourKit which provided an excellent, rich ui, but I was
disappointed that it was not as flexible as I'd like.  It's been a while,
but I remember being disappointed that while I could programatically capture
profiles, I didn't see a way to parse those generated profiles to mine
information that wasn't readily availible in the UI.

More recently, I've used jvisualvm which now has a sampling profiler, which
I've found, while less sleek, is very adequate.  However, the software I was
building at the time were fair sized distributed Map/Reduce style jobs. 
This made profiling more challenging because the processes I wanted to
collect information from were distributed across multiple machines.

Around that time I came across the Dapper paper (http://research.google.com/pubs/pub36356.html)
which I thought was very interesting.  (I'm always a bit envous when I read about the
infrastructure that google has built and has at their disposal)  While
Dapper is really about collecting traces distributed across
processes/servers and therefore different then the sampling profilers that
I'd been using, it got me thinking.

Looking at the source to jvisualvm and thinking about Dapper (and some
element of the intellectual exploration) eventually me to creating sprofile. 

## Unique characteristics of Sprofile

(Maybe these traits exist elsewhere, but I hadn't encountered them)

* Samples are written as a sequential log.  The idea being that you could 
write multiple logs for different processes and then aggregate them together
in the UI to look at cross-server profiles.  Also, this means that you can parse 
an incomplete file and you should still get a valid profile up until a certain
point in time.
* There is the ability to save "context" information associated with a stack trace.  
This can be used to embed some contextual information that will help explain
why it is spending so much time in the given routine.  (For example, it
could be the filename that was passed into a parse call)   If no sample is
taken while still in the call, then the context information is not written
and only adds very minimal overhead.

Sprofile is intended to be run with a very low sampling frequency.  For these
large map/reduce jobs that took hours, even infrequent samples provide a
meaningful statistical sample.   Also, sampling infrequently, the overhead
would be low enought that it would be part of the normal logging of the
process and used in production.  When a performance question arose, we 
could mine the already generated logs.

## Future work

I've considered adding:

* log memory usage along with each sample.
* add request handle for aggregating across threads.  (This would allow
Dapper like cross-thread/process call trees to be constructed)
* Add thread state to timeline plot
* Add compression.  There's already some amount of frugality in the
profile format, but a generic compressor would probably help greatly because
there still is a lot of repetition.   There appear to be several fast 
compressors availible, so I may try to incorporate one to further reduce the
size of the sample log.
