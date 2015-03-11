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
    // String used to construct this argument
    String value;
    // arguments used to build this argument
    List<Argument> arguments;

    static Map<String, Class> primitiveTypeMap;
    static {
        primitiveTypeMap = new HashMap();
        primitiveTypeMap.put("byte"     , byte.class    );
        primitiveTypeMap.put("short"    , short.class   );
        primitiveTypeMap.put("int"      , int.class     );
        primitiveTypeMap.put("long"     , long.class    );
        primitiveTypeMap.put("float"    , float.class   );
        primitiveTypeMap.put("double"   , double.class  );
        primitiveTypeMap.put("boolean"  , boolean.class );
        primitiveTypeMap.put("char"     , char.class    );
    }
    static Map<String, Class> primitiveImplementationMap;
    static {
        primitiveImplementationMap = new HashMap();
        primitiveImplementationMap.put("byte"       , Byte.class        );
        primitiveImplementationMap.put("short"      , Short.class       );
        primitiveImplementationMap.put("int"        , Integer.class     );
        primitiveImplementationMap.put("long"       , Long.class        );
        primitiveImplementationMap.put("float"      , Float.class       );
        primitiveImplementationMap.put("double"     , Double.class      );
        primitiveImplementationMap.put("boolean"    , Boolean.class     );
        primitiveImplementationMap.put("char"       , Character.class   );
    }

    public Class getClass(String className) throws ClassNotFoundException {
        Class mapped = primitiveTypeMap.get(className);
        if (mapped != null)
            return mapped;
        return Class.forName(className);
    }

    public Class getImplementationClass(String className) throws ClassNotFoundException {
        Class mapped = primitiveImplementationMap.get(className);
        if (mapped != null)
            return mapped;
        return Class.forName(className);
    }

    public Class getArgumentType() throws ClassNotFoundException {
        if (argumentType != null)
            return getClass(argumentType);
        return String.class;
    }

    public Class getImplementationType() throws ClassNotFoundException {
        if (argumentType != null)
            return getImplementationClass(argumentType);
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
