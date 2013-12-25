package com.jcm.nioserver;

import java.util.ArrayList;

import com.jcm.nioserver.event.ServerListener;

public class Notifier {
    private static ArrayList<ServerListener> listeners = null;
    private static Notifier instance = null;

    private Notifier() {
        listeners = new ArrayList<ServerListener>();
    }

    /**
     * 单例：触发器实例
     * @return 触发器实例
     */
    public static synchronized Notifier getNotifier() {
        if (instance == null) {
            instance = new Notifier();
        }
        return instance;
    }

    /**
     * 添加监听器
     * @param 监听器
     */
    public void addListener(ServerListener l) {
        synchronized (listeners) {
            if (!listeners.contains(l))
                listeners.add(l);
        }
    }

    public void fireOnAccept() throws Exception {
        for (int i = listeners.size() - 1; i >= 0; i--)
            ( (ServerListener) listeners.get(i)).onAccept();
    }

    public void fireOnAccepted(Request request) throws Exception {
        for (int i = listeners.size() - 1; i >= 0; i--)
            ( (ServerListener) listeners.get(i)).onAccepted(request);
    }

    void fireOnRead(Request request) throws Exception {
        for (int i = listeners.size() - 1; i >= 0; i--)
            ( (ServerListener) listeners.get(i)).onRead(request);

    }

    void fireOnWrite(Request request, Response response)  throws Exception  {
        for (int i = listeners.size() - 1; i >= 0; i--)
            ( (ServerListener) listeners.get(i)).onWrite(request, response);

    }

    public void fireOnClosed(Request request) throws Exception {
        for (int i = listeners.size() - 1; i >= 0; i--)
            ( (ServerListener) listeners.get(i)).onClosed(request);
    }

    public void fireOnError(String error) {
        for (int i = listeners.size() - 1; i >= 0; i--)
            ( (ServerListener) listeners.get(i)).onError(error);
    }
}
