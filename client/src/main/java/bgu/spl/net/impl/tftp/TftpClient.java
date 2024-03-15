package bgu.spl.net.impl.tftp;

import java.net.Socket;

import bgu.spl.net.api.MessagingProtocol;

public class TftpClient {
    public static void main(String[] args) {

        if (args.length == 0) {
            args = new String[]{"localhost", "7777"};
        }

        if (args.length < 2) {
            System.out.println("you must supply two arguments: host, message");
            System.exit(1);
        }
        Socket sock;

        try {
            sock = new Socket("localhost", 7777);

            MessagingProtocol<Frame> protocol = new TftpProtocol();
            
            Keyboard keyboard = new Keyboard(sock, protocol);
            Listener listener = new Listener(sock, protocol, keyboard);

            Thread listenerThread = new Thread(listener);
            Thread keyboardThread = new Thread(keyboard);
            listenerThread.start();
            keyboardThread.start();

            
        }
        catch (Exception e) {
            e.printStackTrace();
        }
}
}
