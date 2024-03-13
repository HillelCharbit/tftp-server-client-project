package bgu.spl.net.api;

import bgu.spl.net.impl.tftp.Frame;

public interface MessagingProtocol<T> {
 
    /**
     * process the given message 
     * @param msg the received message
     * @return the response to send or null if no response is expected by the client
     */
    T process(Frame msg);
 
    /**
     * @return true if the connection should be terminated
     */
    boolean shouldTerminate();
 
}