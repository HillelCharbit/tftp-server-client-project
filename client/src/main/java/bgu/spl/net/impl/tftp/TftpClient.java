package bgu.spl.net.impl.tftp;

import java.net.Socket;

import bgu.spl.net.api.MessagingProtocol;

public class TftpClient {
    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("Usage: TftpClient <host> <port> <filename>");
            System.exit(1);
        }
        String host = args[0];
        int port = Integer.parseInt(args[1]);

        Socket sock;

        try {
            sock = new Socket(host, port);

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
