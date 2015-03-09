package com.ncleung.storm.topology.dynamic.unmarshaller;

import com.google.gson.Gson;

import com.ncleung.storm.topology.dynamic.StormTopologyRepresentation;
import com.ncleung.storm.topology.dynamic.StormTopologyUnmarshaller;

public class GsonTopologyUnmarshaller implements StormTopologyUnmarshaller {

    Gson gson = new Gson();

    @Override
    public StormTopologyRepresentation unmarshal(String stringRepresentation) {
        return gson.fromJson(stringRepresentation, StormTopologyRepresentation.class);
    }

}

