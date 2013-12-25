package com.jcm.nioserver;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class Server implements Runnable {
    private static List<SelectionKey> wpool = new LinkedList<SelectionKey>();
    private static Selector selector;
    
    private ServerSocketChannel sschannel;
    private InetSocketAddress address;
    protected Notifier notifier;
    private int port;

    public Server(int port, int threadsCount) throws Exception {
        this.port = port;
        notifier = Notifier.getNotifier();

        for (int i = 0; i < threadsCount; i++) {
            Thread r = new Reader();
            Thread w = new Writer();
            r.start();
            w.start();
        }

        selector = Selector.open();
        sschannel = ServerSocketChannel.open();
        sschannel.configureBlocking(false);
        address = new InetSocketAddress(port);
        ServerSocket ss = sschannel.socket();
        ss.bind(address);
        sschannel.register(selector, SelectionKey.OP_ACCEPT);
    }

    public void run() {
        System.out.println("Server started ...");
        System.out.println("Server listening on port: " + port);
        while (true) {
            try {
                int num = 0;
                // 挑选准备好的socket通道
                num = selector.select();
                if (num > 0) {
                    Set<SelectionKey> selectedKeys = selector.selectedKeys();
                    Iterator<SelectionKey> it = selectedKeys.iterator();
                    while (it.hasNext()) {
                        SelectionKey key = (SelectionKey) it.next();
                        it.remove();
                        // 接受新的连接
                        if ( (key.readyOps() & SelectionKey.OP_ACCEPT) == SelectionKey.OP_ACCEPT) {
                           // Accept the new connection
                           ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
                           notifier.fireOnAccept();

                           SocketChannel sc = ssc.accept();
                           sc.configureBlocking(false);

                           // 准备好连接数据
                           Request request = new Request(sc);
                           notifier.fireOnAccepted(request);

                           // 注册读操作 , 以进行下一步的读操作
                           sc.register(selector,  SelectionKey.OP_READ, request);
                       } else if ((key.readyOps() & SelectionKey.OP_READ) == SelectionKey.OP_READ ) {
                           Reader.processRequest(key);
                           key.cancel();
                       } else if ( (key.readyOps() & SelectionKey.OP_WRITE) == SelectionKey.OP_WRITE ) {
                           Writer.processRequest(key);
                           key.cancel();
                       }
                    }
                } else {
                    addRegister();
                }
            } catch (Exception e) {
                notifier.fireOnError("Error occured in Server: " + e.getMessage());
                continue;
            }
        }
    }

    /**
     * 在 Selector 中注册新的写通道
     */
    private void addRegister() {
        synchronized (wpool) {
            while (!wpool.isEmpty()) {
                SelectionKey key = (SelectionKey) wpool.remove(0);
                SocketChannel schannel = (SocketChannel)key.channel();
                try {
                    schannel.register(selector,  SelectionKey.OP_WRITE, key.attachment());
                } catch (Exception e) {
                    try {
                        schannel.finishConnect();
                        schannel.close();
                        schannel.socket().close();
                        notifier.fireOnClosed((Request)key.attachment());
                    }
                    catch (Exception e1) {}
                    notifier.fireOnError("Error occured in addRegister: " + e.getMessage());
                }
            }
        }
    }

    /**
     * 添加新的写通道
     */
    public static void processWriteRequest(SelectionKey key) {
        synchronized (wpool) {
            wpool.add(wpool.size(), key);
            wpool.notifyAll();
        }
        selector.wakeup();
    }
}
