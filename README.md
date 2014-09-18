Ehcache Search API Examples/Tests
=================================

This is a small demo project for testing/demonstrating the usage and performance of the [Ehcache Search API](http://ehcache.org/documentation/apis/search).

So far, there are only tests with the onHeap cache, without overflow/storage to disk. More info on how this works without a search index can be found on the [website](http://ehcache.org/documentation/apis/search#standalone-ehcache-without-bigmemory).

Run
---

To run the examples, just type

    mvn test

and have a look onto the console.
All examples are implemented as unit-tests.

To populate the cache, dummy 10.000 Person objects will be created (from a csv file) and each object will be put into the cache 10 times, so that there is a count of 1.000.000 objects in the cache (~380MB cache size).


License
----

MIT
