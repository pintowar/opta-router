# Opta Router

| Service       | Master                                                                                                                                                                      | Develop                                                                                                                                                                                             |
|---------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| CI Status     | ![master status](https://github.com/pintowar/opta-router/actions/workflows/master.yml/badge.svg?branch=master)                                                       | ![develop status](https://github.com/pintowar/opta-router/actions/workflows/develop.yml/badge.svg?branch=develop)                                                                               |
| Test Coverage | [![Sonar Coverage](https://sonarcloud.io/api/project_badges/measure?project=pintowar_opta-router&metric=coverage)](https://sonarcloud.io/dashboard?id=pintowar_opta-router) | [![Sonar Coverage](https://sonarcloud.io/api/project_badges/measure?project=pintowar_opta-router&metric=coverage&branch=develop)](https://sonarcloud.io/dashboard?id=pintowar_opta-router&branch=develop) |

![GitHub release (latest)](https://img.shields.io/github/v/release/pintowar/opta-router?logo=github)
![Docker release (latest)](https://img.shields.io/docker/v/pintowar/opta-router?sort=semver&logo=docker)
![GitHub license](https://img.shields.io/github/license/pintowar/opta-router)

Sample CVRP Application using Kotlin + Optaplanner/Timefold/Jsprit/Or-Tools/Jenetics + Graphhopper + Spring Boot + Apache Camel + Jooq + Hazelcast + RSocket + Websockets

## What is CVRP?

CVRP is a variation of VRP (Vehicle Routing Problem). VRP is a combinatorial optimization problem which asks "What is the optimal set of routes for a fleet of vehicles to traverse in order to deliver to a given set of customers?".

On CVRP (Capacitated Vehicle Routing Problem), the vehicles have a limited carrying capacity of the goods that must be delivered and customers have a given demand.

Determining the optimal solution to VRP is NP-hard, so the size of problems that can be optimally solved using mathematical programming or combinatorial optimization may be limited. Therefore, commercial and open source solvers tend to use heuristics due to the size and frequency of real world VRPs they need to solve.

## About the project

This is a playground project for educational purpose. I usually use this project to explore some JVM/Kotlin libs, new Gradle features/plugins, Constraint Solvers, Design Architectures on distributed systems and CI pipes (using github actions).

### Project Modules

The project uses a [hexagonal architecture](https://jmgarridopaz.github.io/content/articles.html) and was broken into the following modules:

* opta-router-core: domain modules with main business logic;
* opta-router-geo: adapter module for reading geo data using Graphhopper;
* opta-router-repo: adapter module for relational database persistence using Jooq;
* opta-router-solver: modules containing several solvers;
* opta-router-webcli: webclient app (SPA) using Vue3;
* opta-router-app: webserver application using Spring Boot.

The `opta-router-solver` module contains several submodules that uses different Open Source solvers (Constraint Programming and Meta Heuristics) implementations. They are:

* jenetics: a Genetic Algorithm library, written in modern-day Java;
* jsprit: lightweight, flexible toolkit for solving VRP, based on a single all-purpose meta-heuristic currently solving;
* optaplanner: solves constraint satisfaction problems with construction heuristics and meta-heuristic algorithms;
* or-tools: a for solving linear programming, mixed integer programming, constraint programming, vehicle routing, and related optimization problems;
* timefold: is a fork of OptaPlanner.

### Screenshots

Portfolio List

![image](https://github.com/pintowar/opta-router/assets/354264/6449a598-49d0-4faa-8cf4-4d9dbf37b75a)

Solver View

![image](https://github.com/pintowar/opta-router/assets/354264/12e9f924-cd4c-42fd-b9ad-404318bbc1f7)

Solver History

![image](https://github.com/pintowar/opta-router/assets/354264/ef81013b-122b-48d3-8493-a8d744f3249e)

### Building and running

The project has 3 build/run profiles:

* local (**default**): for development setup;
* single: single process with an embedded h2 database;
* dist: app being able to connect with an external postgres database. This profile is also meant to run for different modules (look into `docker-compose.yml` file to check how to proper setup this environment):
  * rest-app: client ui + main rest endpoints;
  * solver-app: backend with solver. It reads solver requests from a distributed queue;
  * geo-app: another backend serving data concerning to geo data.

This project contains a sample of pre-defined problems on belgium area. An OSM (Open Street Map) map is needed to make the road path calculation process possible. The `opta-router-geo` is responsible to extract this information from the map that can be found on this [link](http://download.geofabrik.de/europe/belgium-latest.osm.pbf).

Download it and point the ENV `GRAPH_OSM_PATH` to its system path. This way the `opta-router-geo` will parse and extract the relevant road data and persist it on another folder (defined by `GRAPH_OSM_LOCATION` path). This process can take some minutes, but will run only once (as long as the relevant data is already stored on the `GRAPH_OSM_LOCATION` path).

#### Development profile

In order to run the project in development mode, the `local` profile must be used. Since it is the default profile, the explicit definition is optional.

##### Web Server App

To run the web-server module, run on `opta-router-app` folder:

    gradle bootRun

This command will run a Spring Boot application on port `8080`.

##### Web Client App

To run the client module, make sure you have node installed on your system, then run on `opta-router-webcli` folder:

    npm run dev

Point your browser to `http://localhost:3000`. This module will run on port `3000`, but will proxy remote calls to port `8080`.

##### Both Modules

Gradle can run both modules together

    gradle bootRun -parallel

This task will run both modules on port `3000` and `8080`.

#### Single assembled jar

To build a jar that contains an embedded h2 database and both server and client modules assembled on the same app. Just run the task on the root folder:

    gradle -PenvironmentName=single assembleApp

This will generate a jar named `app.jar` on the `build` folder. To run the generated app, then run `GRAPH_OSM_PATH=<path_to_osm_map> GRAPH_OSM_LOCATION=<path_to_graphhopper_folder> java -jar app.jar`.

#### Docker Image with Single assembled jar

A full packaged docker image (with single profile) can be found at Dockerhub. In order to run the sample, you have to download the belgium OSM map and add the ENVS config mentioned above.

To run a working sample of opta-router image, run the following commands:

```shell
mkdir -p /tmp/osm/gh-tmp
wget http://download.geofabrik.de/europe/belgium-latest.osm.pbf -P /tmp/osm

docker run --rm -e GRAPH_OSM_PATH=/tmp/osm/belgium-latest.osm.pbf -e GRAPH_OSM_LOCATION=/tmp/osm/gh-tmp -v /tmp/osm:/tmp/osm -p 8080:8080 pintowar/opta-router:single-latest
```

Point your browser to `http://localhost:8080`.

#### Dist assembled jar

To build a jar that contains **only** server and client modules assembled on the same app with drivers to a postgres database. Just run the task on the root folder:

    gradle -PenvironmentName=dist assembleApp

One pre-requisite for building with this profile is a running instance of a postgres instance with a database "opta-router" already created. This is needed in order to `opta-router-repo` module be able to run the migrations and generate the base JOOQ classes. The initial postgres connection configuration can be redefined on `gradle-dist.properties` file.

This will generate a jar named `app.jar` on the `build` folder. To run the generated app, then run `GRAPH_OSM_PATH=<path_to_osm_map> GRAPH_OSM_LOCATION=<path_to_graphhopper_folder> java -jar app.jar`.

#### Docker Compose with Single dist jar

There is a `docker-compose.yml` file on the base directory of this project. It is configured with a postgres database and a docker images with a dist profile build.

On the base directory, you can run the following command:

```shell
mkdir -p /tmp/osm/gh-tmp
wget http://download.geofabrik.de/europe/belgium-latest.osm.pbf -P /tmp/osm

docker-compose up
```

Then point your browser to `http://localhost:8080`.
