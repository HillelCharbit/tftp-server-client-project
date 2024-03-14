package bgu.spl.net.impl.tftp;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

public class SharedResources {

    public static ConcurrentHashMap<Integer, String> LoggedConnectionIdToUsername = new ConcurrentHashMap<>();

    public static Semaphore semaphore = new Semaphore(1, true);

}
