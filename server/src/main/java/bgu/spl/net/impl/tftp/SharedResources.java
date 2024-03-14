package bgu.spl.net.impl.tftp;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

public class SharedResources {
    
    public ConcurrentHashMap<Integer, String> connectionIdToUsername = new ConcurrentHashMap<>();

    public Semaphore semaphore = new Semaphore(1, true);

}
