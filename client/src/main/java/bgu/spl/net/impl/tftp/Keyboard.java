package bgu.spl.net.impl.tftp;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import bgu.spl.net.api.MessagingProtocol;
import bgu.spl.net.impl.tftp.Frames.*;

public class Keyboard implements Runnable{
    private BufferedOutputStream out;
    private BufferedReader in;
    private MessagingProtocol<Frame> protocol;
    public Object lock = new Object();

    public Keyboard(Socket sock, MessagingProtocol<Frame> protocol){
        try{
        out = new BufferedOutputStream(sock.getOutputStream());
        in = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
        } catch (IOException ignored){
        }
                 
        this.protocol = protocol;
    }

    @Override
    public void run() {
        while (!protocol.shouldTerminate()) {
            try {
                String message = in.readLine();

                if (message != null) {
                    Frame frame = Frame.getFrameFromString(message);
                    protocol.setLastCommandSent(frame.getCommand());
                    send(frame.toBytes());
                    if (frame instanceof DISC) {
                        synchronized (lock) {
                            lock.wait();
                        }
                    }
                }
            } catch (IOException | InterruptedException ignored) {

            }
            
        }
    } 

    public synchronized void send(byte[] msg) {
        try {
            out.write(msg);
            out.flush();
        } catch (IOException ignored) {
        }
    }
}
