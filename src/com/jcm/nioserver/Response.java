package com.jcm.nioserver;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class Response {
    private SocketChannel sc;

    public Response(SocketChannel sc) {
        this.sc = sc;
    }

    /**
     * 返回数据写入连接通道
     * @param data 返回数据
     */
    public void send(byte[] data) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(data.length);
        buffer.put(data, 0, data.length);
        buffer.flip();
        while (buffer.hasRemaining()) {
            int len = sc.write(buffer);
            if (len < 0) {
                throw new EOFException();
            }
        }
    }
}
