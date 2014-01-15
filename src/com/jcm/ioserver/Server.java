package com.jcm.ioserver;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;

import org.apache.log4j.Logger;

import com.jcm.util.Util;

public class Server extends Thread {

    public static final Logger log = Logger.getLogger(Server.class);
    
    //尝试与目标主机连接次数
    static public int CONNECT_RETRIES=5;
    //每次建立连接的间隔时间
    static public int CONNECT_PAUSE=5;
    //每次尝试连接的最大时间
    static public int TIMEOUT=50;
    //缓冲区最大字节数
    static public int BUFSIZ=1024;
    // 与客户端相连的Socket
    protected Socket csocket;
    
    public Server(Socket cs) {
        csocket=cs;
        log.info("connect with " + cs.getInetAddress().getHostName() + ":" + cs.getPort());
    }

    public void run(){
        //读取请求头
        String buffer = "";
        String host = Util.p.getProperty("index_add_host");
        int port = Integer.valueOf(Util.p.getProperty("index_add_port"));
        Socket ssocket = null;
        //cis为客户端输入流，sis为目标主机输入流
        InputStream cis = null;
        InputStream sis = null;
        //cos为客户端输出流，sos为目标主机输出流
        OutputStream cos = null;
        OutputStream sos = null;
        try{
            csocket.setSoTimeout(TIMEOUT);
            cis=csocket.getInputStream();
            cos=csocket.getOutputStream();
            while(true){
                int c=cis.read();
                //-1为结尾标志
                if(c==-1) break;
                //读入第一行数据
                if(c=='\r'||c=='\n') break;
                buffer=buffer+(char)c;
            }
            String head =transferRequestURL(buffer, host + ":" + port);
            int retry=CONNECT_RETRIES;
            while (retry--!=0) {
                try {
                    //尝试建立与目标主机的连接
                    ssocket = new Socket(host,port);
                    log.info("transfer the request to " + host + ":" + port);
                    break;
                } catch (Exception e) { }
                // 等待
                Thread.sleep(CONNECT_PAUSE);
            }
            if(ssocket!=null){
                ssocket.setSoTimeout(TIMEOUT);
                sis=ssocket.getInputStream();
                sos=ssocket.getOutputStream();
                //将请求头写入
                sos.write(head.getBytes());
                //建立通信管道
                pipe(cis,sis,sos,cos);
            }
        } catch(InterruptedException | IOException e){
            log.error(e.getMessage());
        } finally {
            try {
                if (csocket != null) {
                    csocket.close();
                }
                if (cis != null) {
                    cis.close();
                }
                if (cos != null) {
                    cos.close();
                }
            } 
            catch (Exception e1) {
                log.error("Client Socket Closed Exception:" + e1.getMessage());
            }
            try {
                if (ssocket != null) {
                    ssocket.close();
                }
                if (sis != null) {
                    sis.close();
                }
                if (sos != null) {
                    sos.close();
                }
            } 
            catch (Exception e2) {
                log.error("Server Socket Closed Exception:" + e2.getMessage());
            }
        }
    }
    
    public static String transferRequestURL(String buffer, String targetAddr){
        String[] tokens = buffer.split(" ");
        String url = tokens[1];
        int start = url.indexOf("http://");
        if(start >= 0) {
            url = url.replace(url.substring(start, url.indexOf("/", start + "http://".length())), targetAddr);
        } else {
//            url = "http://" + targetAddr + url;
        }
        buffer = tokens[0] + " " + url + " " + tokens[2];
        return buffer;
    }
    
    public void pipe(InputStream cis,InputStream sis,OutputStream sos,OutputStream cos){
        try {
            int length;
            byte bytes[]=new byte[BUFSIZ];
            while (true) {
                try {
                    if ((length=cis.read(bytes))>0) {
                        sos.write(bytes,0,length);
                        sos.flush();
                    } else if (length < 0) {
                        break;
                    }
                } catch(SocketTimeoutException e){
                } catch (IOException e) {
                    log.error("Request Exception:" + e.getMessage());
                }
                try {
                    if ((length=sis.read(bytes))>0) {
                        cos.write(bytes,0,length);
                        cos.flush();
                    } else if (length<0) {
                        break;
                    }
                } catch(SocketTimeoutException e){
                } catch (InterruptedIOException e) {
                    log.error("Response Exception:" + e.getMessage());
                }
            }
        } catch (Exception e0) {
            log.error("Pipe异常: " + e0.getMessage());
        }
    }

}
