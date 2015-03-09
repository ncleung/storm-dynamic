package com.ncleung.storm.topology.dynamic;

import backtype.storm.topology.IRichSpout;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class StormSpout {
    String name;
    // class to create for this spout
    String className;
    List<Argument> arguments;
    Integer numTasks;
    Integer numExecutors;
    Integer maxPending;

    public IRichSpout reify() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        return ReflectionHelpers.reify(Class.forName(this.className), IRichSpout.class, arguments);
    }
}
