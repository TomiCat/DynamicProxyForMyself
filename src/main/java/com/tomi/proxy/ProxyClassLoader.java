package com.tomi.proxy;

import java.io.*;

public class ProxyClassLoader extends ClassLoader {
    public Class<?> findClass(String className){
        String path = ProxyClassLoader.class.getResource("").getPath();
        String packageName = ProxyClassLoader.class.getPackage().getName();
        File file = new File(path, className + ".class");

        FileInputStream in = null;
        ByteArrayOutputStream out = null;
        try {
            byte[] datas = new byte[1024];
            int len = 0;
            in = new FileInputStream(file);
            out = new ByteArrayOutputStream();
            while((len = in.read(datas)) != -1){
                out.write(datas, 0, len);
            }

            className = packageName + "." + className;
            return defineClass(className, out.toByteArray(), 0, out.size());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try{
                if(null != in) in.close();
            }catch (Exception e){
                e.printStackTrace();
            }

            try{
                if(null != out) out.close();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return null;
    }

}

