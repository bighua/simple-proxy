package com.jcm.nioserver;

import java.util.List;
import java.util.LinkedList;
import java.nio.channels.SocketChannel;
import java.nio.channels.SelectionKey;

public final class Writer extends Thread {
    private static List<SelectionKey> pool = new LinkedList<SelectionKey>();
    private static Notifier notifier = Notifier.getNotifier();

    public Writer() {
    }

    public void run() {
        while (true) {
            try {
                SelectionKey key;
                synchronized (pool) {
                    while (pool.isEmpty()) {
                        pool.wait();
                    }
                    key = (SelectionKey) pool.remove(0);
                }

                write(key);
            }
            catch (Exception e) {
                continue;
            }
        }
    }

    /**
     * 向客户端发送数据，然后关闭连接，并分别触发 onWrite，onClosed 事件
     * @param key SelectionKey
     */
    public void write(SelectionKey key) {
        try {
            SocketChannel sc = (SocketChannel) key.channel();
            Response response = new Response(sc);

            // 触发fireOnWrite事件
            notifier.fireOnWrite((Request)key.attachment(), response);

            // 关闭
            sc.finishConnect();
            sc.socket().close();
            sc.close();

            // 触发onClosed事件
            notifier.fireOnClosed((Request)key.attachment());
        }
        catch (Exception e) {
            notifier.fireOnError("Error occured in Writer: " + e.getMessage());
        }
    }

    /**
     * 提交写服务线程向客户端发送回应数据
     */
    public static void processRequest(SelectionKey key) {
        synchronized (pool) {
            pool.add(pool.size(), key);
            pool.notifyAll();
        }
    }
}
