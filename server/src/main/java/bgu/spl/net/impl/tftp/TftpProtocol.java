package bgu.spl.net.impl.tftp;

import bgu.spl.net.api.BidiMessagingProtocol;
import bgu.spl.net.srv.Connections;

public class TftpProtocol implements BidiMessagingProtocol<byte[]>  {
    private boolean shouldTerminate = false;
    private int connectionId;
    private Connections<byte[]> connections;
    private final int opCodeLen = 2;


    @Override
    public void start(int connectionId, Connections<byte[]> connections) {
        this.connectionId = connectionId;
        this.connections = connections;
    }   

    @Override
    public void process(byte[] message) {
        int index = 0;
        Short opCode = firstTwoBytesToShort(message[index], message[++index]);
        // TODO implement this
        byte[] fileName;
        int    blockNum;
        int    packetSize;
        switch (opCode) {
            case 1: // RRQ
                fileName = new byte[message.length-opCodeLen];
                for( int i = opCodeLen; i < message.length; i++){
                    fileName[i-opCodeLen] = message[i];
                }
                break;

            case 2: // WRQ
                fileName = new byte[message.length-opCodeLen];
                for( int i = opCodeLen; i < message.length; i++){
                    fileName[i-opCodeLen] = message[i];
                }
                break;

            case 3: // DATA
                packetSize = firstTwoBytesToShort(message[++index], message[++index]);
                blockNum = firstTwoBytesToShort(message[++index], message[++index]);
                index++;
                byte[] data = new byte[packetSize-index];
                for( int i = index; i < data.length; i++){
                    data[i-index] = message[i];
                }
                break;

            case 4: // ACK
                blockNum = firstTwoBytesToShort(message[++index], message[++index]);
                break;
        
            case 5: // ERROR
                int errorCode = firstTwoBytesToShort(message[++index], message[++index]);   
                index++;

                byte[] errorMsg = new byte[message.length-index];
                for( int i = index; i < errorMsg.length; i++){
                    errorMsg[i-index] = message[i];
                }
                break;

            case 6: // DIRQ
                break;

            case 7: // LOGRQ
                byte[] userName = new byte[message.length-opCodeLen];
                for( int i = opCodeLen; i < message.length; i++){
                    userName[i-opCodeLen] = message[i];
                }
                break;

            case 8: // DELRQ
                fileName = new byte[message.length-opCodeLen];
                for( int i = opCodeLen; i < message.length; i++){
                    fileName[i-opCodeLen] = message[i];
                }
                break;

            case 9: // BCAST
                boolean delOrAdd = message[++index] == 0 ? false : true;
                index++;

                fileName = new byte[message.length-index];
                for( int i = index; i < message.length; i++){
                    fileName[i-index] = message[i];
                }

            case 10: // DISC
                connections.disconnect(connectionId);

                                      
            default:
                break;
        }
    }

    @Override
    public boolean shouldTerminate() {
        // TODO implement this
        throw new UnsupportedOperationException("Unimplemented method 'shouldTerminate'");
    } 

    public short firstTwoBytesToShort(byte firstByte, byte secondByte){
        // converting 2 byte array to a short
         
        return (short) (((short) firstByte) << 8 | (short) (secondByte) & 0x00ff);
    }


    
}
