package com.ncleung.storm.topology.dynamic;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.topology.*;
import org.apache.commons.io.FileUtils;

import java.io.File;

public class DynamicTopologyBuilder {

    private void createTopology(String stringRepresentation, StormTopologyUnmarshaller unmarshaller, boolean local) throws Exception {
        StormTopologyRepresentation representation = unmarshaller.unmarshal(stringRepresentation);
        TopologyBuilder builder = new TopologyBuilder();

        Config config = new Config();
        if (representation.config != null) {
            config.putAll(representation.config);
        }
        config.setNumWorkers(representation.numWorkers);

        for (StormSpout spout : representation.spouts) {
            if (spout.numExecutors == null)
                spout.numExecutors = spout.numTasks;

            SpoutDeclarer declarer;
            declarer = builder.setSpout(spout.name, spout.reify(), spout.numExecutors);
            declarer.setNumTasks(spout.numTasks);
            if (spout.maxPending != null)
                declarer.setMaxSpoutPending(spout.maxPending);
        }

        if (representation.bolts != null) {
            for (StormBolt bolt : representation.bolts) {
                if (bolt.numExecutors == null)
                    bolt.numExecutors = bolt.numTasks;

                BoltDeclarer declarer;
                Object boltObj = bolt.reify();
                if (IRichBolt.class.isAssignableFrom(boltObj.getClass())) {
                    IRichBolt richBolt = (IRichBolt)boltObj;
                    declarer = builder.setBolt(bolt.name, richBolt, bolt.numExecutors);
                } else if (IBasicBolt.class.isAssignableFrom(boltObj.getClass())) {
                    IBasicBolt basicBolt = (IBasicBolt)boltObj;
                    declarer = builder.setBolt(bolt.name, basicBolt, bolt.numExecutors);
                } else {
                    throw new RuntimeException("Class " + bolt.className + " implements neither IBasicBolt nor IRichBolt");
                }
                declarer.setNumTasks(bolt.numTasks);

                for (Dependency dependency : bolt.dependencies) {
                    ReflectionHelpers.invokeMethod(declarer, dependency.grouping, dependency.arguments);
                }
            }
        }

        if (local) {
            LocalCluster cluster = new LocalCluster();
            cluster.submitTopology(representation.name, config, builder.createTopology());
        } else {
            StormSubmitter.submitTopology(representation.name, config, builder.createTopology());
        }
    }

    public void launchLocalTopology(String representation, StormTopologyUnmarshaller unmarshaller) throws Exception {
        createTopology(representation, unmarshaller, true);
    }

    public void launchTopology(String representation, StormTopologyUnmarshaller unmarshaller) throws Exception {
        createTopology(representation, unmarshaller, false);
    }

    public static void main(String args[]) throws Exception {
        boolean isLocal = false;
        if (args.length < 2) {
            System.out.println("Usage: storm jar <your jar file> backtype.storm.topology.dynamic.DynamicTopologyBuilder <unmarshaller> <file> [local]");
            System.exit(1);
        }
        if (args.length > 2) {
            isLocal = true;
        }

        Class<?> unmarshallerClass = Class.forName(args[0]);
        String marshalled = FileUtils.readFileToString(new File(args[1]));
        StormTopologyUnmarshaller unmarshaller;
        Object obj = unmarshallerClass.newInstance();
        if (obj instanceof StormTopologyUnmarshaller) {
            unmarshaller = (StormTopologyUnmarshaller)obj;
        } else {
            throw new RuntimeException("Class " + args[0] + " must implement backtype.storm.topology.dynamic.StormTopologyUnmarshaller!");
        }

        DynamicTopologyBuilder builder = new DynamicTopologyBuilder();
        if (isLocal)
            builder.launchLocalTopology(marshalled, unmarshaller);
        else
            builder.launchTopology(marshalled, unmarshaller);
    }

}
