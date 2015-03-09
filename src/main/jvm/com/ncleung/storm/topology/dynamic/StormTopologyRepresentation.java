package com.ncleung.storm.topology.dynamic;

import java.util.List;
import java.util.Map;

public class StormTopologyRepresentation {

    String name;
    List<StormSpout> spouts;
    List<StormBolt> bolts;
    Map<String, Object> config;
    int numWorkers;

}
