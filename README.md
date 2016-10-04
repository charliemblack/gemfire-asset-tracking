## Geode Geo Spatial Example

This project demonstrates the ability to add third party indexing on top of Apache Geode.  Third party indexing extends
the capabilities of Geode to quickly retrieve data that can't be looked up through standard key-value techniques or
through Geode's Object Query Language (OQL).

The indexing technique that is used in this project is a Quad Tree.  A Quad Tree is a tree data structure which has four
children - quad means four.  How this index applies to geo spatial, when divide a 2 dimensional space in four equal
partitions.  Each node in the quad tree is then responsible for holding the data that is contained in the individual
Quad.

Basic like this:

```
 0 | 1
 --+--
 2 | 3
 ```

One of the design choices the developer has is how the data maintained in the index.   The developer can push down data
down to the smallest quad defined by the tree or they can rebalanced as data is inserted.

Here is another image that shows how a quad tree can sub divide a map:

 ![Example of a mecator map](/images/ExampleQuadTree.png)

An item to note in the above map that the quads do not have to be square they just need to be uniform.

From a performance perspective a Quad Tree is a O(log n) data structure like all other Tree data structures.

### Some Technical Information

In this example we will be using some features of Geode:

1. **PartitionListener** – The partition listener is make sure the index is HA.   When there is a failure detected the
PartitionListener will create the index on the primary node(s).
2. **CacheWriter** – As data is being inserted and removed from the system we have to update the index.
3. **Functions** - The indexes are maintained on the servers so we want to query the servers for the data that matches
the search.

In this project I have also made heavy use of Spring eco system.

1. **Spring Data Geode** - Spring Data Geode is part of the Spring Data project.   I use this project configure Geode
and use dependency injection to wire up the project.   I used spring xml DSL to configure the project because I wanted
all the config in one place - just personal preference to help someone with learning.
2. **Spring Boot** - A great way to package up your java application in a single jar.   It make management of java
application a breeze.

The Quad Tree Implementation details in this project

1. Geometries that wrap the poles (-90/90) and the -180/180 lines are not handled correctly.   This was done to simplify
the code.   If this is a concern then we can just split the data in the index and insert multiple geometries.
2. To make remove fast there is an extra data structure to facilitate a quick remove by key.   This fast remove feature
would make this implementation removal O(1) and add the overhead of O(1) for insert since it uses a Map data structure.
    * Note this project uses the eclipse collections project to manage the space concerns.
    * https://www.eclipse.org/collections/
3. Utilizes a Reentrant Read Write Lock to allow thread safe read and write operations.

### Project Break Down

```
.
├── geode-geospatial-api          - The interfaces for the project.
├── geode-geospatial-lib          - The implementation of the Quad Tree and API
└── demo                          - Anything that is releated to the demo
    ├── geode-geospatial-demo-lib - Common classes and Domain Models
    ├── geode-geospatial-grid     - The Geode Grid - this is where to look for maintaining the index.
    ├── geode-geospatial-sim      - The simulation project that moves data around the roads.
    └── geode-geospatial-web      - The Web Front End that uses Open Layers for the Map.

```

### How to run

Open three terminal windows and run the following commands in the terminal windows:

1. ./gradlew :demo:geode-geospatial-grid:bootRun
2. ./gradlew :demo:geode-geospatial-sim:bootRun
3. ./gradlew :demo:geode-geospatial-web:bootRun

Then open your browser: http://localhost:8080

Then Use **Ctrl+Drag** (**Command+Drag** on Mac) to draw boxes over the state of California USA.  Until you get an idea
for how fast or slow your browser is make smaller boxes.   I did no limiting of results to make the code simpler to
read.

The sample data set used in the demo was derived from: http://www.dot.ca.gov/hq/tsip/gis/datalibrary/Metadata/Trknet.html

I converted the Truck Network Shapefile to KMZ file so I could use it with some Java tools that can read KML files.

Using the KMZ file I created a little simulation that has tracks moving at 50-70 MPH through the trucking lanes.

I tried to make the project as simple as I could - AND maintain scability if needed.  If there is something missing
please let me know.

### How to scale the grid:

Geode's typical deployment architecture there is a concept called a locator.   A locator has the responsibility of
allowing clients and servers to discover each other to simplify dynamic scaling.   In the demo code we have the server
start the locator process this is only done to simplify development.   In a production it is better to separate concerns
and run locators in their own process space.

To scale we just need to start a couple of locators, the right number depends on the HA requirements of the deployment.
How to launch the data grid servers all we have to do is point them at the list of locator address by adding in a
property called `demo.locators` and specify the list of locators.

Example: add in `-Ddemo.locators=locator1.address[10331],locator2.address[10332]`

For this application we are using spring profiles.  So we need to change the  profile to something other than the
development profile. The development profile starts the locator in the process.

Add in the following spring option `-Dspring.profiles.active=prod`

### Example of starting a production Grid

Start the locators on multiple hosts:
```
On host A:
  gfsh>start locator --name=locator1 --dir=one --mcast-port=0 --locators=hosta[10334],hostb[10334] --port=10334
On Host B:
  gfsh>start locator --name=locator2 --dir=two --mcast-port=0 --locators=hosta[10334],hostb[10334] --port=10334
```

**NOTE**: The JVM for the locator does not need much heap space.   A common setting is 1G - please iterate what works
best for your application deployment profile.

Start the grid nodes on multiple hosts:

```
java -Dspring.profiles.active=prod -Ddemo.locators=hosta[10334],hostb[10334] -jar geode-geospatial-grid.jar
```

**NOTE:** The Grid servers are normally storing data so we normally set the heaps to be large enough to satisfy the
storage of the data, indexes and run any application code. One item to think about when scoping heap space is how big the object
pointers are.   If we maintain less then 32GB heaps we can use java compressed oops (`XX:+UseCompressedOops`).   If we
we need to use heap sizes larger then 32GB think about using more then 48-64GB to overcome the impact of the larger
object pointers.

**NOTE:** The locators strings are the same (``locators=hosta[10334],hostb[10334]``).

### Common JVM settings for production

When going into a production some of the common Java Parameters I like to use are:

* **-XX:+UseParNewGC** - Use multi-threaded young generation collection
* **-XX:+UseConcMarkSweepGC**  - The CMS GC is the recommend GC for server applications - G1 is getting better with
every release so it debatable for production uses.
* **-XX:CMSInitiatingOccupancyFraction=60** -  We want the CMS collector to start working before we run out of memory -
Definitely a something to watch with VSD or GC Logging
* **-XX:+UseCMSInitiatingOccupancyOnly** - There are a bunch of GC heuristics that go on in the JVM - lets turn that off
and only use the `CMSInitiatingOccupancyFraction`.
* **-XX:+CMSParallelRemarkEnabled** - Reduce the mark phase.
* **-XX:+ScavengeBeforeFullGC** - Run the young collector before the full GC.
* **-XX:+CMSScavengeBeforeRemark** - Run the young collector before the mark phase.

Just about every application is different with respect to how it uses the young generation space.   So it is common to
tune the young generation space.

* **-Xmn=2g** - Another place to look at when tuning the system this is something to review the GC logs and see how the
application is using memory and tune respectively.

When reviewing GC issues here is something to cut and past to help working with GC tuning:

* **export logtime=\`date +%Y%m%d%H%M%S\`** - Grab a date so we can have a history of GC logs based on application start time.
* **-Xloggc:gc${logtime}.log** - Name the log file with the current date.
* **-XX:+PrintGCDetails** - Turn on detailed GC logging.
* **-XX:+PrintGCDateStamps** - Turn on dates for GC detail logging.

Then use something GCViewer to see how the memory is doing:
    https://github.com/chewiebug/GCViewer

Sometimes it is good to review the total time the application stops.   That option can be turned on via:

* **-XX:+PrintGCApplicationStoppedTime** – this will actually report pause time for all safepoints (GC related or not).

Unfortunately output from this option lacks timestamps, but it is still useful to narrow down problem to safepoints.

If you are working with larger heaps here is an option to try out:

* **-XX:+UnlockDiagnosticVMOptions**
* **-XX:ParGCCardsPerStrideChunk=32768** - The default is 256 and could be to small for large heaps.

In one case ParGCCardsPerStrideChunk option reduced the duration of minor GC by more than 60% in tests with 32G and 96G
heaps:

 * 96G (5G young gen): minor pauses reduced from ~130ms to ~40ms while   the frequency remained the same ( once every
 ~45s)
 * 32G (2G young gen): ~35ms down to ~13ms (same frequency, once every 20s)

### Video walk through:

Insert you tube video.