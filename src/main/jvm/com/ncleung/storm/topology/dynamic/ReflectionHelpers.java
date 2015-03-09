package com.ncleung.storm.topology.dynamic;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ReflectionHelpers {

    static class Arguments {
        Object   argumentObjects[];
        Class<?> argumentTypes[];

        public Arguments(List<Argument> arguments) throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
            List<Object> argumentObjects = new ArrayList<Object>();
            List<Class> argumentTypes = new ArrayList<Class>();
            if (arguments != null) {
                for (Argument argument : arguments) {
                    argumentTypes.add(argument.getArgumentType());
                    argumentObjects.add(argument.reify());
                }
            }
            this.argumentObjects = argumentObjects.toArray(new Object[argumentObjects.size()]);
            this.argumentTypes   = argumentTypes.toArray(new Class[argumentTypes.size()]);
        }
    }

    public static boolean isArray(String type) {
        return type != null && type.charAt(0) == '[';
    }

    public static boolean isCollection(String type) throws ClassNotFoundException {
        return type != null && Collection.class.isAssignableFrom(Class.forName(type));
    }

    public static Class<?> baseType(String type) throws ClassNotFoundException {
        if (isArray(type)) {
            String base = type.substring(0, type.length() - 2);
        }
        return Class.forName(type);
    }

    public static <T> T reify(Class<?> klazz, Class<T> superKlazz, List<Argument> inputArguments) throws ClassNotFoundException, InstantiationException, InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        Arguments arguments = new Arguments(inputArguments);
        int numArguments = arguments.argumentObjects.length;

        Constructor[] constructors = klazz.getConstructors();
        Constructor toInvoke = null;
        methodLoop: for (Constructor constructor : constructors) {
            Class<?>[] paramTypes = constructor.getParameterTypes();
            if (numArguments == 0 && (paramTypes == null || paramTypes.length == 0)) {
                toInvoke = constructor;
                break;
            } else if (numArguments == 0 || paramTypes == null || paramTypes.length != numArguments) {
                continue;
            }

            for (int i = 0; i < arguments.argumentObjects.length; ++i) {
                if (!paramTypes[i].isAssignableFrom(arguments.argumentObjects[i].getClass())) {
                    continue methodLoop;
                }
            }
            toInvoke = constructor;
        }
        Object reified = null;
        if (toInvoke != null) {
            try {
                reified = toInvoke.newInstance(arguments.argumentObjects);
            } catch (Exception t) {
                t.printStackTrace();
            }
        }
        if (reified != null)
            return superKlazz.cast(reified);
        throw new NoSuchMethodException("Failed to create object of type " + klazz.getName() + " with " + numArguments + " arguments!");
    }

    public static <T> T reify(Class<T> klazz, List<Argument> inputArguments) throws ClassNotFoundException, InstantiationException, InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        return reify(klazz, klazz, inputArguments);
    }

    public static Object invokeMethod(Object obj, String methodName, List<Argument> inputArguments) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException {
        Arguments arguments = new Arguments(inputArguments);
        int numArguments = arguments.argumentObjects.length;
        Class<?> klazz = obj.getClass();

        // per http://stackoverflow.com/questions/19886065/java-getmethod-with-subclass-parameter
        Method[] methods = klazz.getMethods();
        Method toInvoke = null;
        methodLoop: for (Method method : methods) {
            if (!methodName.equals(method.getName())) {
                continue;
            }
            Class<?>[] paramTypes = method.getParameterTypes();

            // TODO TESTING CODE REMOVE ME
            {
                StringBuilder builder = new StringBuilder();
                if (paramTypes != null) {
                    for (Class<?> argClass : paramTypes) {
                        builder.append(argClass.getName()).append(":");
                    }
                }
                //System.out.println("Found Method: " + methodName + ", arguments: " + builder.toString());
            }

            if (numArguments == 0 && (paramTypes == null || paramTypes.length == 0)) {
                toInvoke = method;
                break;
            } else if (numArguments == 0 || paramTypes == null
                    || paramTypes.length != numArguments) {
                continue;
            }

            for (int i = 0; i < arguments.argumentObjects.length; ++i) {
                if (!paramTypes[i].isAssignableFrom(arguments.argumentTypes[i])) {
                    continue methodLoop;
                }
            }
            toInvoke = method;
            break;
        }
        if (toInvoke != null) {
            return toInvoke.invoke(obj, arguments.argumentObjects);
        }
        // TODO fill in this string
        StringBuilder builder = new StringBuilder();
        builder.append("Could not find method ").append(methodName).append(" with argument types: ");
        for (Class<?> argClass : arguments.argumentTypes) {
            builder.append(argClass.getName()).append(":");
        }
        throw new NoSuchMethodException(builder.toString());
    }

    public static void main(String args[]) throws Exception {
        Integer intArr[] = new Integer[0];
        System.out.println(intArr.getClass().getName());
        //Array.newInstance
    }

}
