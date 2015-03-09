package com.ncleung.storm.topology.dynamic;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;

public class Argument {
    // type used to lookup arguments in method/constructor
    String argumentType;
    // type used to create this argument
    String implementationType;
    // String used to construct this argument
    String value;
    // arguments used to build this argument
    List<Argument> arguments;

    // TODO clean this type stuff up
    public Class getArgumentType() throws ClassNotFoundException {
        if (argumentType != null)
            return Class.forName(argumentType);
        return String.class;
    }

    public Class getImplementationType() throws ClassNotFoundException {
        return Class.forName(getImplementationTypeString());
    }

    public String getImplementationTypeString() {
        if (implementationType != null)
            return implementationType;
        else if (argumentType != null)
            return argumentType;
        return "java.lang.String";
    }

    public Object reify() throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        if (argumentType == null && implementationType == null)
            return value;
        if (ReflectionHelpers.isArray(getImplementationTypeString())) {
            Object array = Array.newInstance(getImplementationType(), arguments.size());
            for (int i = 0; i < arguments.size(); ++i) {
                Argument argument = arguments.get(i);
                Array.set(array, i, argument.reify());
            }
            return array;
        } else if (ReflectionHelpers.isCollection(getImplementationTypeString())) {
            Collection collection = Collection.class.cast(ReflectionHelpers.reify(getImplementationType(), null));
            if (collection == null)
                return null;
            for (Argument argument : arguments) {
                collection.add(argument.reify());
            }
            return collection;
        }
        return ReflectionHelpers.reify(getImplementationType(), arguments);
    }
}
