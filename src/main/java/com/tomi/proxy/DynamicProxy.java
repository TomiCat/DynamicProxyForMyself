package com.tomi.proxy;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

public class DynamicProxy {

    private static final String ln = "\r\n";

    public static Object newProxyInstance(ProxyClassLoader classLoader, Class<?>[] classes, MyInvocationHandler h) {
        String src = generateSrc(classes);
        String path = DynamicProxy.class.getResource("").getPath();
        FileWriter out = null;
        File file = null;
        try {
            file = new File(path, "$Proxy.java");
            out = new FileWriter(file);
            out.write(src);
            out.flush();
            compile(file);
            Class<?> $Proxy = classLoader.findClass("$Proxy");
            Constructor<?> constructor = $Proxy.getConstructor(MyInvocationHandler.class);
            return constructor.newInstance(h);
        } catch (IOException | NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) out.close();
            }catch (Exception e){

            }
        }
        return null;
    }

    public static void compile(File file) {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager manager = compiler.getStandardFileManager(null, null, null);
        Iterable<? extends JavaFileObject> it = manager.getJavaFileObjects(file);

        JavaCompiler.CompilationTask task = compiler.getTask(null, manager, null, null, null, it);
        task.call();
        try {
            file.delete();
        } finally {
            try {
                if(null != manager) manager.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static String generateSrc(Class<?>[] interfaces) {
        StringBuilder sb = new StringBuilder();
        sb.append("package com.tomi.proxy;" + ln);
        sb.append("import com.tomi.proxy.MyInvocationHandler;" + ln);
        sb.append("import com.tomi.proxy.Animal;" + ln);
        sb.append("import java.lang.reflect.*;" + ln);
        sb.append("public class $Proxy implements ");
        StringBuilder classes = new StringBuilder();
        for(Class clazz :interfaces) {
            classes.append(clazz.getSimpleName()+",");
        }
        String clazzs = classes.toString();
        clazzs = clazzs.substring(0, clazzs.length() - 1);
        sb.append(clazzs+" {" + ln);

        sb.append("private MyInvocationHandler h;"+ln);
        sb.append("public $Proxy(MyInvocationHandler invocationHandler) {"+ln);
        sb.append("          this.h = invocationHandler;"+ln);
        sb.append("}"+ln);

        for(Class clazz :interfaces) {
            Method[] methods = clazz.getMethods();
            for(Method method:methods) {
                sb.append("public " + method.getReturnType().getName() + " " + method.getName() + "(");
                Class<?>[] parameterTypes = method.getParameterTypes();
                Parameter[] parameters = method.getParameters();

                StringBuilder methodStrSb = new StringBuilder();
                StringBuilder paramClassesSb = new StringBuilder();
                StringBuilder paramNameSb = new StringBuilder();
                for(Parameter para:parameters) {
                    String paramName = para.getName();
                    String paramType = para.getClass().getSimpleName().toLowerCase();
                    methodStrSb.append(paramType + " " + paramName + ",");
                    paramClassesSb.append(paramType+",");
                    paramNameSb.append(paramName+",");
                }
                String methodStr = methodStrSb.toString();
                if(methodStr.length() > 0)
                    methodStr = methodStr.substring(0, methodStr.length() - 1);

                String paramClasses = paramClassesSb.toString();
                if(paramClasses.length() > 0)
                    paramClasses = paramClasses.substring(0, paramClasses.length() - 1);

                String paramNames = paramNameSb.toString();
                if(paramNames.length() > 0)
                    paramNames = paramNames.substring(0, paramNames.length() - 1);

                sb.append(methodStr + "){" + ln);
                sb.append("try{" + ln);
                    sb.append("Method m = " + clazz.getName() + ".class.getMethod(\"" + method.getName() + "\", new Class[]{" + paramClasses + "});" + ln);
                    sb.append(hashReturnValue(method.getReturnType()) ? "return ": "");
                    sb.append(getCaseCode("this.h.invoke(this,m,new Object[]{" + paramNames + "})",method.getReturnType()));
                    sb.append(";" + ln);
                sb.append("} catch(Error _ex) {"+ln);
                sb.append("} catch(Throwable e) {"+ln);
                sb.append("     throw new UndeclaredThrowableException(e);" + ln);
                sb.append("}"+ln);
                sb.append(getReturnEmptyCode(method.getReturnType())+ln);
                sb.append("}" + ln);
            }
        }

        sb.append("}");
        return sb.toString();
    }

    private static Map<Class,Class> mappings = new HashMap<Class, Class>();
    static {
        mappings.put(int.class,Integer.class);
    }

    private static String getCaseCode(String code,Class<?> returnClass){
        if(mappings.containsKey(returnClass)){
            return "((" + mappings.get(returnClass).getName() +  ")" + code + ")." + returnClass.getSimpleName() + "Value()";
        }
        return code;
    }

    private static String getReturnEmptyCode(Class<?> returnClass){
        if(mappings.containsKey(returnClass)){
            return "return 0;";
        }else if(returnClass == void.class){
            return "";
        }else {
            return "return null;";
        }
    }

    private static boolean hashReturnValue(Class clazz){
        return clazz != void.class;
    }
}
