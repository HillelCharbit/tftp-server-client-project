package bgu.spl.net.srv;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import bgu.spl.net.impl.tftp.Frame;

public interface Connections<T> {

    ConcurrentHashMap<Integer, ConnectionHandler<T>> getConnections();

    void connect(int connectionId, ConnectionHandler<T> handler);

    boolean send(int connectionId, T msg);

    void disconnect(int connectionId);

    void loginUser(int connectionId, String username);

    boolean isLoggedIn(int connectionId);
}

