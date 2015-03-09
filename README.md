# storm-dynamic

Motivation:

  Provide a package that allows a user to create a storm topology from a configuration file.

To build:

  mvn package

Example:

  $ storm jar target/storm-dynamic-builder-0.1.0-SNAPSHOT-jar-with-dependencies.jar com.ncleung.storm.topology.dynamic.DynamicTopologyBuilder com.ncleung.storm.topology.dynamic.unmarshaller.GsonTopologyUnmarshaller src/main/resources/com/ncleung/storm/topology/dynamic/WordCountTopology.json

Notes:

  depends on storm version 0.9.3 (modify storm.version property in pom.xml to change the dependency)
  storm-starter is pulled in so that WordCountTopology.json can be executed from the jar-with-dependencies
  Cyclic topologies not yet supported
  The unmarshaller class (and corresponding configuration file implementation) is meant to be configurable
   - You can write your own unmarshaller as long as it reifies your configuration into com.ncleung.storm.topology.dynamic.StormTopologyRepresentation
   - You can remove the default unmarshaller implementation and gson dependency if you don't want it
  Arguments for bolt / spout creation or method invocation can be specified as follows:
   - String: set "value", and do not set "argumentType" or "implementationType"
   - POJO creation / method invocation: each argument is treated as an argument for the constructor
   - List / Array: each argument is treated as an element of the list / array
