package com.jcm.nioserver.event;

import com.jcm.nioserver.Request;
import com.jcm.nioserver.Response;

public interface ServerListener {

   /**
    * 错误处理
    * @param error 错误信息
    */
   public void onError(String error);

   /**
    * 连接处理
    */
   public void onAccept() throws Exception;

   /**
    * 连接成功
    * @param request 
    */
   public void onAccepted(Request request) throws Exception;

   /**
    * 请求读取
    * @param request 
    */
   public void onRead(Request request) throws Exception;

   /**
    * 结果数据返回
    * @param request 
    */
   public void onWrite(Request request, Response response) throws Exception;

   /**
    * 连接关闭
    * @param request 
    */
   public void onClosed(Request request) throws Exception;
}
