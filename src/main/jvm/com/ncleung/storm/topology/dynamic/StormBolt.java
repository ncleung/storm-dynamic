package com.ncleung.storm.topology.dynamic;

import backtype.storm.topology.IBasicBolt;
import backtype.storm.topology.IRichBolt;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

public class StormBolt {
    String name;
    String className;
    List <Argument> arguments;
    Integer numTasks;
    Integer numExecutors;
    List<Dependency> dependencies;

    public Object reify() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Class<?> boltClass = Class.forName(this.className);
        if (IBasicBolt.class.isAssignableFrom(boltClass)) {
            return ReflectionHelpers.reify(Class.forName(this.className), IBasicBolt.class, arguments);
        } else if (IRichBolt.class.isAssignableFrom(boltClass)) {
            return ReflectionHelpers.reify(Class.forName(this.className), IRichBolt.class, arguments);
        }
        throw new RuntimeException("Class " + className + " implements neither IBasicBolt nor IRichBolt");
    }

}
