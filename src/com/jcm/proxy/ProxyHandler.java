package com.jcm.proxy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.binary.StringUtils;

import com.jcm.nioserver.Request;
import com.jcm.nioserver.Response;
import com.jcm.nioserver.event.EventAdapter;
import com.jcm.util.AESCoder;
import com.jcm.util.Util;

/**
 * 代理服务器处理请求
 */
public class ProxyHandler extends EventAdapter {

    public void onRead(Request request)  throws Exception {
        // 解析请求URL，转换成真实请求url
        String indexOp = parse(request);
        if (indexOp != null) {
            String host = Util.p.getProperty(indexOp + "_host");
            int port = Integer.valueOf(Util.p.getProperty(indexOp + "_port"));
            Socket s = new Socket(host,port);
            OutputStream output = s.getOutputStream();
            output.write(request.getDataInput());
            output.flush();
            s.shutdownOutput();
            request.attach(s);
            LogHandler.log.info("transfer the request to " + host + ":" + port);
        } else {
            LogHandler.log.error("Illegal request from "  + request.getAddress().getHostAddress() + ":" + request.getPort());
        }
    }
    
    public void onWrite(Request request, Response response)  throws Exception {
        if (request.attachment() == null) return;
        Socket s = (Socket)request.attachment();
        try {
            // 将服务器端数据写入reponse
            response.send(readFromSocket(s.getInputStream()));
        } finally {
            s.close();
        }
    }
    
    private String parse(Request request) throws Exception {
        String indexOp = null;
        String requestStr = new String(request.getDataInput());
        String url = requestStr.substring(0, requestStr.indexOf(Util.LINE_SEPARATOR)).split(Util.SPACE)[1];
        byte[] key = Hex.decodeHex(url.substring(0, 32).toCharArray());
        byte[] data = Hex.decodeHex(url.substring(32).toCharArray());
        String realUrl = StringUtils.newString(AESCoder.decrypt(data, AESCoder.toKey(key)), "UTF-8");
        int indexKey = realUrl.indexOf(Util.INDEX_KEY);
        if (indexKey > -1) {
            indexOp = realUrl.substring(indexKey);
            realUrl = realUrl.replace(indexOp, "");
            requestStr.replace(url, realUrl);
            request.setDataInput(requestStr.getBytes());
            indexOp = indexOp.substring(indexKey + Util.INDEX_KEY.length() + 1);
        }
        return indexOp;
    }
    
    public byte[] readFromSocket(InputStream in) throws IOException {
        int off = 0;
        int r = 0;
        int bufferSize = 1024;
        byte[] bufferData = new byte[bufferSize];
        byte[] data = new byte[bufferSize * 10];

        while (true) {
            r = in.read(bufferData);
            if (r == -1) break;
            if ( (off + r) > data.length) {
                data = grow(data, bufferSize * 10);
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
