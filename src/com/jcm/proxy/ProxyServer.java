package com.jcm.proxy;


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.log4j.Logger;

import com.jcm.nioserver.Notifier;
import com.jcm.nioserver.Server;
import com.jcm.util.Util;

public class ProxyServer {

    public static final Logger log = Logger.getLogger(ProxyServer.class);
    
    public static void main(String[] args) {
//        startNIOServer();
        startServer();
    }
    
    private static void startNIOServer() {
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
    
    private static void startServer() {
        ServerSocket ssock = null;
        
        try {
            ssock = new ServerSocket(Integer.valueOf(Util.p.getProperty("port")));
            while (true) {
                log.info("Ready for the new connection...");
                Socket s = ssock.accept();
                new com.jcm.ioserver.Server(s);
            }
        } catch (NumberFormatException | IOException e) {
            e.printStackTrace();
        } finally {
            try {
                ssock.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
    }
}
