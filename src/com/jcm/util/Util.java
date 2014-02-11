package com.jcm.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.jcm.proxy.LogHandler;
import com.ztools.conf.Environment;

public class Util {

    public static String LINE_SEPARATOR = System.getProperty("line.separator");
    
    public static String FILE_SEPARATOR = System.getProperty("file.separator");
    
    public static String SPACE = " ";
    
    public static String INDEX_KEY = "indexOp";
    
    public static Properties p = new Properties();
    
    static {
        try {
            InputStream inputStream = Environment.findInputStreamByResource("conf/jcm.properties", Util.class);
            p.load(inputStream);
        } catch (IOException e) {
            LogHandler.log.error("File conf/jcm.properties is not found.");
            LogHandler.log.error(e.getMessage());
        }
    }
}
