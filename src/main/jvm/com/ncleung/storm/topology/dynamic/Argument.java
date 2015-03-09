package com.ncleung.storm.topology.dynamic;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;

public class Argument {
    // type used to lookup arguments in method/constructor
    String argumentType;
    // String used to construct this argument
    String value;
    // arguments used to build this argument
    List<Argument> arguments;

    public Class getArgumentType() throws ClassNotFoundException {
        if (argumentType != null)
            return Class.forName(argumentType);
        return String.class;
    }

    public String getArgumentTypeString() {
        if (argumentType != null)
            return argumentType;
        // String is the default type for an argument
        return "java.lang.String";
    }

    public Object reify() throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        if (argumentType == null)
            return value;
        if (ReflectionHelpers.isArray(getArgumentTypeString())) {
            Object array = Array.newInstance(getArgumentType(), arguments.size());
            for (int i = 0; i < arguments.size(); ++i) {
                Argument argument = arguments.get(i);
                Array.set(array, i, argument.reify());
            }
            return array;
        } else if (ReflectionHelpers.isCollection(getArgumentTypeString())) {
            Collection collection = Collection.class.cast(ReflectionHelpers.reify(getArgumentType(), null));
            if (collection == null)
                return null;
            for (Argument argument : arguments) {
                collection.add(argument.reify());
            }
            return collection;
        }
        return ReflectionHelpers.reify(getArgumentType(), arguments);
    }
}
