# GemFire Asset Tracking

This project demonstrates the power of GemFire Search which gives GemFire some very powerful indexing techniques including a giving GemFire users the ability to index thier Geospatial data.

This project has 3 projects in it a base library where all the common classes would live.  Then we have a simulator which uses a KML file of the active truck lanes for California to randomly move vehicles around California.    Then finally we have an web server hosting a mapping application.   

I have attempted to make this project about highlighting GemFire application development over making a super complex application.   But it would be very simple to add in correlaing the tracking information with "beacon" information with a 360 degree view of what that beacon is.

## How to use

### Install GemFire and GemFire Search

The GemFire instructions are pretty basic.   

1) **Install GemFire**: Download and un-archive GemFire
2) **Install GemFire Search**: Download and copy GemFire Search into the GemFire `extensions` directory

If you want more drawn out instructions they can be found in the documentation:

1) [GemFire Docs](https://docs.vmware.com/en/VMware-GemFire/10.1/gf/getting_started-installation-install_intro.html)
2) [GemFire Search Docs](https://docs.vmware.com/en/VMware-GemFire-Search/1.0/gemfire-search/search_integration.html#installing-2)

### Build the projects

Before we start up GemFire we need to build some libraries for our domain objects which are in the [asset tracker library](tracker-lib).   This would accomplished buy running the `gradlew bootJar` commmand.

```
gradlew bootJar
```

### Run GemFire

To make running GemFire simpler I have provided some batch scripts that will get a GemFire system running locally.  I am currently using windows for development so those scripts have the most effort put into them.

```
cd <project>\scripts
startGemFire.bat
```
Running that command starts GemFire up with 1 locator and 2 servers.   Then it deploys some of the classes we are going to need to use for GemFire Search to index and search our data.  The project that gets deployed is in the [library](tracker-lib/src/main/java/demo/gemfire/asset/tracker/lib).

Then the script creates the index and a region for the demo needs.

### Start the Simulator

The simulator injects the current location of asset that we are tracking. To provide a more realistic example I have the location object pretend to "drive" around California using trucking lanes data.  The [`development` setting](tracker-simulator/src/main/resources/config/application.yml) will have 100k assets moving around.

```shell
cd <project>
gradlew gemfire-asset-tracker-sim:bootRun
```

### Start the Web Application

The web application uses Openlayers to provide mapping capabilities where we can interact with the data.  We provide a rest interface for the web application where the app user attempts to query for beacons in a given area.   There you can look at [how simple it is for the application](tracker-web-app/src/main/java/demo/gemfire/asset/tracker/web/GeospatialWebServer.java) to get the beacons enclosed in the geo box.

```
cd <project>
gradlew gemfire-asset-tracker-web:bootRun
```

To use open your browser: http://localhost:8080

Then Use **Ctrl+Drag** (**Command+Drag** on Mac) to draw boxes over the state of California USA.  Until you get an idea for how fast or slow your browser is make smaller boxes.   I did no limiting of results to make the code simpler to read.

![Example Query](/images/sample_query.png)




