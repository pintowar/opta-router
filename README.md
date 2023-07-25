# Opta Router Boot

![master status](https://github.com/pintowar/opta-router/actions/workflows/master.yml/badge.svg?branch=master)

![GitHub tag (latest)](https://img.shields.io/github/v/tag/pintowar/opta-router)
![GitHub license](https://img.shields.io/github/license/pintowar/opta-router)

Sample CVRP Application using Kotlin + Optaplanner/Timefold/Jsprit/Or-Tools/Jenetics + Graphhopper + Spring Boot + + Jooq + Websockets

## What is CVRP?

CVRP is a variation of VRP (Vehicle Routing Problem). VRP is a combinatorial optimization problem which asks "What is the optimal set of routes for a fleet of vehicles to traverse in order to deliver to a given set of customers?". 

On CVRP (Capacitated Vehicle Routing Problem), the vehicles have a limited carrying capacity of the goods that must be delivered and customers have a given demand.

Determining the optimal solution to VRP is NP-hard], so the size of problems that can be optimally solved using mathematical programming or combinatorial optimization may be limited. Therefore, commercial and open source solvers tend to use heuristics due to the size and frequency of real world VRPs they need to solve.

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
* optaplanner: solves constraint satisfaction problems with construction heuristics and metaheuristic algorithms;
* or-tools: a for solving linear programming, mixed integer programming, constraint programming, vehicle routing, and related optimization problems;
* timefold: is a fork of OptaPlanner.

### Screenshots

Portfolio List

![image](https://github.com/pintowar/opta-router/assets/354264/b88230bf-5f2e-40cf-8921-370bacd1a602)

Solver View

![image](https://github.com/pintowar/opta-router/assets/354264/3922c6d1-8e48-4af6-8805-44096312bb24)

### Building and running

TODO From here!!!

### Run in development mode

This sample application uses the belgium map to calculate distances between points. The map can be found on this [link](http://download.geofabrik.de/europe/belgium-latest.osm.pbf). Download it and point the Env `GRAPH_OSM_PATH` to it's path, in order to run the application.
The `GRAPH_OSM_LOCATION` Env must point to a temporary folder, in order to Graphhopper folder.

Both Envs can be customized on the `application.yml` file.

This project is divided in 2 sub modules:

#### Web Server App

To run the server module, run on opta-router-app folder:

    gradle bootRun

This command will run a Spring Boot application on port `8080`.

#### Web Client App

To run the client module, make sure you have node installed on your system, then run on client folder:

    npm run dev

Point your browser to `http://localhost:3000`. This module will run on port `3000`, but will proxy remote calls to port `8080`.

#### Both Modules

Gradle can run both modules together

    gradle bootRun -parallel

This task will run both modules on port `3000` and `8080`.

### Build assembled jar

To build a jar that contains both server and client modules assembled on the same app. Just run the task on the root folder:

    gradle assembleServerAndClient

This will generate a jar named `app.jar` on the `build` folder. To run the generated app, then run `GRAPH_OSM_PATH=<path_to_osm_map> GRAPH_OSM_LOCATION=<path_to_graphhopper_folder> java -jar app.jar`.

### Docker

A full packaged docker image can be found at Dockerhub. In order to run the sample, you have to download the belgim OSM map and add the envs config mentioned above.

To run a working sample of opta-router image, run the following commands:

```shell
mkdir -p /tmp/osm/gh-tmp
wget http://download.geofabrik.de/europe/belgium-latest.osm.pbf -P /tmp/osm

docker run --rm -e GRAPH_OSM_PATH=/tmp/osm/belgium-latest.osm.pbf -e GRAPH_OSM_LOCATION=/tmp/osm/gh-tmp -v /tmp/osm:/tmp/osm -p 8080:8080 pintowar/opta-router
```

Point your browser to `http://localhost:8080`.
