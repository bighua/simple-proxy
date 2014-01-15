package com.jcm.nioserver;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

public class Reader extends Thread {
    // 请求队列
    private static List<SelectionKey> pool = new LinkedList<SelectionKey>();
    private static Notifier notifier = Notifier.getNotifier();
    private static int BUFFER_SIZE = 1024;

    public static final Logger log = Logger.getLogger(Reader.class);
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
     * @throws InterruptedException 
     */
    public static byte[] readRequest(SocketChannel sc) throws IOException, InterruptedException {
        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
        int off = 0;
        int r = 0;
        byte[] data = new byte[BUFFER_SIZE * 10];
        int attempts = 1;

        while ( true ) {
            buffer.clear();
            r = sc.read(buffer);
            if (r == -1) break;
            if (r == 0) {
                if (attempts <= 10) {
//                    log.debug("Attemp to get data for " + attempts +" times");
                    attempts++;
                    Thread.sleep(100);
//                    r = doRead(sc, buffer);
                } else {
                    break;
                }
            }
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

    public static int doRead(SocketChannel socketChannel, ByteBuffer byteBuffer) throws IOException{

        int count = 1; 
        int byteRead = 0; 
        long readTimeout = 100;
        Selector readSelector = null; 
        SelectionKey tmpKey = null; 

        try{

            while (count > 0){ 
                count = socketChannel.read(byteBuffer);  //[1] 
                if (count > -1) 
                    byteRead += count; 
                else 
                    byteRead = count; 
            }

            if (byteRead == 0){ 
                readSelector = Selector.open(); //[2] 
                if (readSelector == null){ 
                    return 0; 
                } 
                count = 1; 
                tmpKey = socketChannel.register(readSelector,SelectionKey.OP_READ);
                tmpKey.interestOps(tmpKey.interestOps() | SelectionKey.OP_READ); 
                int code = readSelector.select(readTimeout); // [3] 
                tmpKey.interestOps(tmpKey.interestOps() & (~SelectionKey.OP_READ)); 
                if (code == 0){ 
                    return 0; // Return on the main Selector and try again. 
                } 
                while (count > 0){ 
                    count = socketChannel.read(byteBuffer); // [4] 
                    if (count > -1) 
                        byteRead += count; 
                    else 
                        byteRead = count;
                } 
            } 
        } finally { 
            if (tmpKey != null) 
                tmpKey.cancel(); 
            if (readSelector != null){ 
                try{ 
                    readSelector.selectNow(); 
                } catch (IOException ex){ 
                } 
            } 
        } 
        byteBuffer.flip(); 
        return byteRead; 
    } 
}
