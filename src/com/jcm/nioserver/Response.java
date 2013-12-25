package com.jcm.nioserver;

import java.nio.channels.SocketChannel;
import java.nio.ByteBuffer;
import java.io.IOException;

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
        sc.write(buffer);
    }
}
