# GemFire Asset Tracking

This project demonstrates the power of GemFire Search, providing GemFire users with advanced indexing techniques, including the ability to index geospatial data.

The project consists of three parts:
1. **Base Library**: Contains all common classes.
2. **Simulator**: Uses a KML file of active truck lanes in California to randomly move vehicles around the state.
3. **Web Server**: Hosts a mapping application.

The focus of this project is on showcasing GemFire application development rather than creating a complex application. However, it can easily be extended to correlate tracking information with "beacon" information, providing a 360-degree view of the beacon.

This 360-degree profile enrichment is accomplished using "cache listeners," "async event listeners," or "cache writer." The key is to stage the data with the identifier for the beacon, allowing for high throughput with in-memory performance. GemFire enhances this by co-locating regions, ensuring beacons operate on the servers holding the data.

The choice of technique depends on the use case:

* **CacheListener** - Synchronous, triggered after the data has been persisted in the context of a "put".
* **CacheWriter** - Synchronous, triggered before the data has been persisted in the context of a "put".  A CacheWriter can also prohibit a data item from being actually stored.  This could be treated as a data validation.
* **AsyncCacheListener** - Asynchronous, triggered after the data has been stored and operates outside the context of a "put".

## How to Use

### Install GemFire and GemFire Search

Follow these steps to install GemFire and GemFire Search:

1. **Install GemFire**: Download and unarchive GemFire.
2. **Install GemFire Search**: Download and copy GemFire Search into the GemFire `extensions` directory.

For detailed instructions, refer to the documentation:
- [GemFire Docs](https://docs.vmware.com/en/VMware-GemFire/10.1/gf/getting_started-installation-install_intro.html)
- [GemFire Search Docs](https://docs.vmware.com/en/VMware-GemFire-Search/1.0/gemfire-search/search_integration.html#installing-2)

### Build the Projects

Before starting GemFire, build the libraries for our domain objects in the [asset tracker library](tracker-lib) by running the `gradlew bootJar` command:

```shell
gradlew bootJar
```

### Run GemFire

To simplify running GemFire, batch scripts are provided for local setup and configuration. For Windows, use the following command:

```shell
cd <project>\scripts
startGemFire.bat
```

This command starts GemFire with one locator and two servers, then deploys the necessary classes for GemFire Search to index and search the data. The deployed project is located in the [library](tracker-lib/src/main/java/demo/gemfire/asset/tracker/lib).

The script also creates the index and a region for the demo.

### Start the Simulator

The simulator injects the current location of assets we are tracking. It simulates movement around California using trucking lanes data. The [`development` setting](tracker-simulator/src/main/resources/config/application.yml) configures 100k assets moving around.

```shell
cd <project>
gradlew tracker-simulator:bootRun
```

### Start the Web Application

The web application uses OpenLayers for mapping capabilities and provides a REST interface to query beacons in a given area. The code for querying beacons can be found [here](tracker-web-app/src/main/java/demo/gemfire/asset/tracker/web/GeospatialWebServer.java).

```shell
cd <project>
gradlew tracker-web-app:bootRun
```

Open your browser and navigate to [http://localhost:8080](http://localhost:8080). Use **Ctrl+Drag** (or **Command+Drag** on Mac) to draw boxes over California to query the beacons. Start with smaller boxes to gauge your browser's performance, as no result limiting was implemented to keep the code simple.

![Example Query](/images/sample_query.png)

## Debugging Tips for Windows

To kill all Java processes, use the following command:

```shell
taskkill /f /im java.exe
```