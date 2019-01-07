# Repast Parameters Wrapper

## Overview
This wrapper provides an implementation of the Repast Parameters class that silently syncs with a RESTful backend. It's specifically written to work with the API defined in the [amongmodel-backend repository](https://github.com/brunslo/repast-amongmodel-backend), however it can be configured to point to any endpoint programatically.

## Caveats
As of the time of this release the Repast suite has not released its source into a Maven-compatible repository. Therefore the required Repast libraries required for building against have been included in the `/libs` folder.
 
Because this wrapper is designed to integrate with the Repast modeling suite that resides within Eclipse, all the dependencies defined in `POM.xml` are packaged within the resulting JAR. This is done using [maven-shade-plugin](https://maven.apache.org/plugins/maven-shade-plugin/), and results in a JAR that can be added to any Repast project's `/libs` directory without any additional dependency configuration.

## Usage
Simply invoke Maven to build and package the wrapper into a JAR:
```
mvn clean package
``` 
The resulting JAR, `/target/parameterswrapper-<version>.jar`, can then be copied in the `/libs` directory of any Repast model. 