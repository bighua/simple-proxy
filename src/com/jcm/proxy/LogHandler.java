package com.jcm.proxy;

import org.apache.log4j.Logger;

import com.jcm.nioserver.Request;
import com.jcm.nioserver.event.EventAdapter;
import com.jcm.util.Util;

public class LogHandler extends EventAdapter {

    public static final Logger log = Logger.getLogger(LogHandler.class);
    
    @Override
    public void onAccept() throws Exception {
        log.info("Ready for the new connection...");
    }
    
    @Override
    public void onAccepted(Request request) throws Exception {
        log.info("connect with " + request.getAddress().getHostAddress() + ":" + request.getPort());
    }

    @Override
    public void onRead(Request request)  throws Exception {
        log.info(Util.LINE_SEPARATOR + new String(request.getDataInput(), "UTF-8"));
    }

//    @Override
//    public void onWrite(Request request, Response response) throws Exception {
//        log.info(Util.LINE_SEPARATOR + "transfer response from " + );
//    }
    
    @Override
    public void onClosed(Request request) throws Exception {
        log.info("Close connection with " + request.getAddress().getHostAddress() + ":" + request.getPort());
    }

    @Override
    public void onError(String error) {
        log.error("Exception occurs: \n" + error);
    }
}
