package bgu.spl.net.impl.tftp;

import java.util.concurrent.ConcurrentHashMap;

import bgu.spl.net.srv.ConnectionHandler;
import bgu.spl.net.srv.Connections;

public class ConnectionsImpl<T> implements Connections<T> {
    
    private ConcurrentHashMap<Integer, ConnectionHandler<T>> connections;

    public ConnectionsImpl() {
        connections = new ConcurrentHashMap<>();
    }

    @Override
    public void connect(int connectionId, ConnectionHandler<T> handler) {
        connections.putIfAbsent(connectionId, handler);
    }

    @Override
    public boolean send(int connectionId, T msg) {
        if (connections.containsKey(connectionId)) {
            connections.get(connectionId).send(msg);
            return true;
        }
        return false;
    }

    @Override
    public void disconnect(int connectionId) {
        try {
            connections.get(connectionId).close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        connections.remove(connectionId);
    }

    public ConcurrentHashMap<Integer, ConnectionHandler<T>> getConnections() {
        return connections;
    }


    public boolean isLoggedIn(int connectionId) {
        if (connections.containsKey(connectionId)) {
            return connections.get(connectionId).isLoggedIn();
        }
        return false;
    }

    public void loginUser(int connectionId, String username) {
        if (connections.containsKey(connectionId)) {
            connections.get(connectionId).login(username);
        }
    }
 
    
}
