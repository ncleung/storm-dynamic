package com.ncleung.storm.topology.dynamic;

public interface StormTopologyUnmarshaller {
    StormTopologyRepresentation unmarshal(String stringRepresentation);
}
