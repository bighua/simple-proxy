package com.jcm.proxy;


import com.jcm.nioserver.Notifier;
import com.jcm.nioserver.Server;
import com.jcm.util.Util;

public class ProxyServer {

    public static void main(String[] args) {
        try {
            ProxyHandler proxy = new ProxyHandler();
            LogHandler log = new LogHandler();
            Notifier notifier = Notifier.getNotifier();
            notifier.addListener(proxy);
            notifier.addListener(log);
            
            Server server = new Server(Integer.valueOf(Util.p.getProperty("port")), 
                    Integer.valueOf(Util.p.getProperty("rw_thread_count")));
            Thread tServer = new Thread(server);
            tServer.start();
        } catch (Exception e) {
            LogHandler.log.error("Server error: " + e.getMessage());
            System.exit(-1);
        }
    }
}
