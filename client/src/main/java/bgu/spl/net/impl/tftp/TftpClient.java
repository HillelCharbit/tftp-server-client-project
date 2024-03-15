package bgu.spl.net.impl.tftp;

import java.net.Socket;

public class TftpClient {
    //TODO: implement the main logic of the client, when using a thread per client the main logic goes here
    public static void main(String[] args) {
        if (args.length == 0) {
            args = new String[]{"localhost", "7777"};
        }

        if (args.length < 2) {
            System.out.println("you must supply two arguments: host, message");
            System.exit(1);
        }
        try{
            Socket sock = new Socket(args[0], Integer.parseInt(args[1]));
            TftpProtocol protocol = new TftpProtocol();
            
            Keyboard keyboard = new Keyboard(sock, protocol);
            Listener listener = new Listener(sock, protocol, keyboard);

            Thread listenerThread = new Thread(listener);
            Thread keyboardThread = new Thread(keyboard);
            listenerThread.start();
            keyboardThread.start();

        } catch (Exception ignored){
        }
    }
}
