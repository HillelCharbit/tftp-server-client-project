package bgu.spl.net.impl.tftp;

import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import bgu.spl.net.impl.tftp.Frames.*;

public class Listener implements Runnable{
    Socket sock;
    TftpEncoderDecoder encdec;
    TftpProtocol protocol;
    Keyboard keyboard;
    
    public Listener(Socket sock, TftpProtocol protocol, Keyboard keyboard){
        this.sock = sock;
        this.encdec = new TftpEncoderDecoder();
        this.protocol = protocol;
        this.keyboard = keyboard;

    }

    public void run() {
        try {
            int byteRead = 0;
            while (!protocol.shouldTerminate() && (byteRead = sock.getInputStream().read()) != -1) {
                byte[] message = encdec.decodeNextByte((byte) byteRead);
                if (message != null) {
                    Frame frame = getFrameFromByte(message); 
                    Frame response = protocol.process(frame);
                    if (response != null) {
                        byte[] encoded = encdec.encode(response);
                        keyboard.send(encoded);
                        
                        synchronized (keyboard.lock) {
                            keyboard.lock.notify();
                        }
                    }
                    
                }

            }
        } catch (Exception ignored) {
        }
    }

    private Frame getFrameFromByte(byte[] message){
        short opCode = Frame.TwoBytesToShort(message[0], message[1]);
        String arg;

        switch (opCode) {
            case 1:
                arg = new String(message, 2, message.length - 3, StandardCharsets.UTF_8);
                return new RRQ(arg);
            case 2:
            arg = new String(message, 2, message.length - 3, StandardCharsets.UTF_8);
            return new WRQ(arg);
            case 3:
                short packetSize = Frame.TwoBytesToShort(message[2], message[3]);
                short blockNumber = Frame.TwoBytesToShort(message[4], message[5]);
                byte[] data = Arrays.copyOfRange(message, 6, message.length);
                return new DATA(blockNumber, data, packetSize);

            case 4:
                short blockNumberAck = Frame.TwoBytesToShort(message[2], message[3]);
                return new ACK(blockNumberAck);
            case 5:
                short errorCode = Frame.TwoBytesToShort(message[2], message[3]);
                String errorMsg = new String(message, 4, message.length - 5, StandardCharsets.UTF_8);
                return new ERROR(errorCode, errorMsg);
            case 6:
                return new DIRQ();
            case 7:
                arg = new String(message, 2, message.length - 3, StandardCharsets.UTF_8);
                return new LOGRQ(arg);
            case 8:
                arg = new String(message, 2, message.length - 3, StandardCharsets.UTF_8);
                return new DELRQ(arg);
            case 9:
                boolean added = message[2] == 1;
                arg = new String(message, 3, message.length - 4, StandardCharsets.UTF_8);
                return new BCAST(added, arg);
            case 10:
                return new DISC();
            default:
                return null;
        }

    }
    
}
