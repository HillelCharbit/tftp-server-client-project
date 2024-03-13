package bgu.spl.net.srv;

import java.util.concurrent.ConcurrentHashMap;

public class ConnectionsImpl implements Connections<byte[]> {
    
    private ConcurrentHashMap<Integer, ConnectionHandler<byte[]>> connections;

    public ConnectionsImpl() {
        connections = new ConcurrentHashMap<>();
    }

    @Override
    public void connect(int connectionId, ConnectionHandler<byte[]> handler) {
        connections.putIfAbsent(connectionId, handler);
    }

    @Override
    public boolean send(int connectionId, byte[] msg) {
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

    public ConcurrentHashMap<Integer, ConnectionHandler<byte[]>> getConnections() {
        return connections;
    }

    public void send(byte[] msg, int connectionId) {
        if (connections.containsKey(connectionId)) {
            connections.get(connectionId).send(msg);
        }
    }    

    
}
