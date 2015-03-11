package com.ncleung.storm.topology.dynamic;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class Argument {
    // type used to lookup arguments in method/constructor
    String argumentType;
    // type used to create the argument
    String implementationType;
    // String used to construct this argument
    String value;
    // arguments used to build this argument
    List<Argument> arguments;

    static Map<String, Class> primitiveMap;
    static {
        primitiveMap = new HashMap();
        primitiveMap.put("byte"     , byte.class    );
        primitiveMap.put("short"    , short.class   );
        primitiveMap.put("int"      , int.class     );
        primitiveMap.put("long"     , long.class    );
        primitiveMap.put("float"    , float.class   );
        primitiveMap.put("double"   , double.class  );
        primitiveMap.put("boolean"  , boolean.class );
        primitiveMap.put("char"     , char.class    );
    }

    public Class getClass(String className) throws ClassNotFoundException {
        Class mapped = primitiveMap.get(className);
        if (mapped != null) {
            return mapped;
        }
        return Class.forName(className);
    }

    public Class getArgumentType() throws ClassNotFoundException {
        if (argumentType != null)
            return getClass(argumentType);
        return String.class;
    }

    public Class getImplementationType() throws ClassNotFoundException {
        if (implementationType != null)
            return getClass(implementationType);
        if (argumentType != null)
            return getClass(argumentType);
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
        else if (value != null) {
            List<Argument> args = new ArrayList();
            Argument dummy = new Argument();
            dummy.value = this.value;
            args.add(dummy);
            return ReflectionHelpers.reify(getImplementationType(), args);
        }
        if (ReflectionHelpers.isArray(getArgumentTypeString())) {
            Object array = Array.newInstance(getImplementationType(), arguments.size());
            for (int i = 0; i < arguments.size(); ++i) {
                Argument argument = arguments.get(i);
                Array.set(array, i, argument.reify());
            }
            return array;
        } else if (ReflectionHelpers.isCollection(getArgumentTypeString())) {
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
