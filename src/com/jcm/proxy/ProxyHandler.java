package com.jcm.proxy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import com.jcm.nioserver.Request;
import com.jcm.nioserver.Response;
import com.jcm.nioserver.event.EventAdapter;

/**
 * 代理服务器处理请求
 */
public class ProxyHandler extends EventAdapter {
    public ProxyHandler() {
    }

    private int BUFFER_SIZE = 1024;
    private Socket s = null;

    @Override
    public void onAccept() throws Exception {
        System.out.println("===================ready for the new connection...");
    }
    
    @Override
    public void onAccepted(Request request) throws Exception {
        System.out.println("connect with " + request.getAddress().getHostAddress() + ":" + request.getPort());
    }

    public void onRead(Request request)  throws Exception {
//        String host = "localhost";
//        int port=1200;
        String host = "search.8chedao.com";
        int port=38254;
        // 解析请求URL，转换成真实请求url
        if (filter(request)) {
            s = new Socket(host,port);
            OutputStream output = s.getOutputStream();
            output.write(request.getDataInput());
            output.flush();
            s.shutdownOutput();
            System.out.println("transfer the request to " + host + ":" + port);
        } else {
            System.out.println("Illegal request from "  + host + ":" + port);
        }
    }
    
    public void onWrite(Request request, Response response)  throws Exception {
        try {
            // 将服务器端数据写入reponse
            response.send(readFromSocket(s.getInputStream()));
        } finally {
            s.close();
        }
    }
    
    public void onClosed(Request request) throws Exception {
        System.out.println("Close connection with " + request.getAddress().getHostAddress() + ":" + request.getPort());
    }

    public void onError(String error) {
        System.out.println("Exception occurs: \n" + error);
    }
    
    private boolean filter(Request request) {
        boolean result = true;
        return result;
    }
    
    public byte[] readFromSocket(InputStream in) throws IOException {
        int off = 0;
        int r = 0;
        byte[] bufferData = new byte[BUFFER_SIZE];
        byte[] data = new byte[BUFFER_SIZE * 10];

        while (true) {
            r = in.read(bufferData);
            if (r == -1) break;
            if ( (off + r) > data.length) {
                data = grow(data, BUFFER_SIZE * 10);
            }
            System.arraycopy(bufferData, 0, data, off, r);
            off += r;
        }
        byte[] res = new byte[off];
        System.arraycopy(data, 0, res, 0, off);
        return res;
    }
    
    public byte[] grow(byte[] src, int size) {
        byte[] tmp = new byte[src.length + size];
        System.arraycopy(src, 0, tmp, 0, src.length);
        return tmp;
    }
}
