package com.jcm.proxy;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

import com.jcm.nioserver.Notifier;
import com.jcm.nioserver.Server;
import com.jcm.util.Util;

public class ProxyServer {

    public static final Logger logger = Logger.getLogger(ProxyServer.class);
    
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
            
            int port = Integer.valueOf(Util.p.getProperty("port"));
            Server server = new Server(port, 
                    Integer.valueOf(Util.p.getProperty("rw_thread_count")));
            Thread tServer = new Thread(server);
            tServer.start();
            logger.info("Server started ...");
            logger.info("Server listening on port: " + port);
        } catch (Exception e) {
        	logger.error("Server error: " + e.getMessage());
            System.exit(-1);
        }
    }
    
    private static void startServer() {
        ServerSocket ssock = null;
        
        try {
            int port = Integer.valueOf(Util.p.getProperty("port"));
            ssock = new ServerSocket(port);
            logger.info("Server started ...");
            logger.info("Server listening on port: " + port);
            ExecutorService pool = Executors.newCachedThreadPool();
            while (true) {
                logger.info("Ready for the new connection...");
                Socket s = ssock.accept();
                pool.execute(new com.jcm.ioserver.Server(s));
            }
        } catch (NumberFormatException | IOException e) {
        	logger.error(e.getMessage());
        } finally {
            try {
                ssock.close();
            } catch (IOException e) {
            	logger.error(e.getMessage());
            }
        }
        
    }
}
