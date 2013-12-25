package com.jcm.proxy;


import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import com.jcm.nioserver.Notifier;
import com.jcm.nioserver.Server;
import com.ztools.conf.Environment;

public class ProxyServer {

    public static void main(String[] args) {
        try {
            ProxyHandler proxy = new ProxyHandler();
            Notifier notifier = Notifier.getNotifier();
            notifier.addListener(proxy);
            
            InputStream inputStream = new FileInputStream(new File(Environment.getContext()+"conf/jcm.properties"));
            Properties p = new Properties();
            p.load(inputStream);
            Server server = new Server(Integer.valueOf(p.getProperty("port")), 
            		Integer.valueOf(p.getProperty("rw_thread_count")));
            Thread tServer = new Thread(server);
            tServer.start();
        }
        catch (Exception e) {
            System.out.println("Server error: " + e.getMessage());
            System.exit(-1);
        }
    }
}
