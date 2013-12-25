package com.jcm.nioserver;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.List;

public class Reader extends Thread {
	// 请求队列
    private static List<SelectionKey> pool = new LinkedList<SelectionKey>();
    private static Notifier notifier = Notifier.getNotifier();
    private static int BUFFER_SIZE = 1024;

    public Reader() {
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

                read(key);
            }
            catch (Exception e) {
                continue;
            }
        }
    }

    /**
     * 开启读操作
     * @param key SelectionKey
     */
    public void read(SelectionKey key) {
        try {
            SocketChannel sc = (SocketChannel) key.channel();
            byte[] clientData =  readRequest(sc);

            Request request = (Request)key.attachment();
            request.setDataInput(clientData);

            // 触发onRead
            notifier.fireOnRead(request);

            Server.processWriteRequest(key);
        }
        catch (Exception e) {
            notifier.fireOnError("Error occured in Reader: " + e.getMessage());
        }
    }

    /**
     * 添加本线程，唤醒其他阻塞线程
     */
    public static void processRequest(SelectionKey key) {
        synchronized (pool) {
            pool.add(pool.size(), key);
            pool.notifyAll();
        }
    }

    /**
     * 读入请求信息
     * @param sc 连接socket
     */
    public static byte[] readRequest(SocketChannel sc) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
        int off = 0;
        int r = 0;
        byte[] data = new byte[BUFFER_SIZE * 10];

        while ( true ) {
            buffer.clear();
            r = sc.read(buffer);
            if (r <= 0) break;
            if ( (off + r) > data.length) {
                data = grow(data, BUFFER_SIZE * 10);
            }
            byte[] buf = buffer.array();
            System.arraycopy(buf, 0, data, off, r);
            off += r;
        }
        byte[] req = new byte[off];
        System.arraycopy(data, 0, req, 0, off);
        return req;
    }

    public static byte[] grow(byte[] src, int size) {
        byte[] tmp = new byte[src.length + size];
        System.arraycopy(src, 0, tmp, 0, src.length);
        return tmp;
    }
}
