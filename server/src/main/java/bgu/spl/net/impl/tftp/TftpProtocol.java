package bgu.spl.net.impl.tftp;

import java.util.ArrayList;

import bgu.spl.net.api.BidiMessagingProtocol;
import bgu.spl.net.srv.Connections;

public class TftpProtocol implements BidiMessagingProtocol<byte[]>  {
    private boolean shouldTerminate = false;
    private int connectionId;
    private Connections<byte[]> connections;

    private String userName;
    private String fileName;
    private short packetSize;
    private short blockNum;
    private ArrayList<byte[]> dataBlocks;
    private ArrayList<Integer> loggedClients;
    private boolean delOrAdd;
    private String errorMsg;
 
    private final int opCodeLen = 2;


    @Override
    public void start(int connectionId, Connections<byte[]> connections) {
        this.connectionId = connectionId;
        this.connections = connections;
    }   

    @Override
    public void process(byte[] message) {
        int index = 0;
        Short opCode = TwoBytesToShort(message[index], message[++index]);

        if(opCode < 1 || opCode > 10) {
            // ERROR
            byte[] errorMsg = "Illegal TFTP operation".getBytes();
            // error message should end with a zero byte
            connections.send(connectionId, errorMsg);
            return;
        }
        // TODO implement this
      
        switch (opCode) {
            case 1: // RRQ
                RRQprocess(message);
                break;
        
            case 2: // WRQ
                WRQprocess(message);
                break;
        
            case 3: // DATA
                DATAprocess(message);
                break;
        
            case 4: // ACK
                ACKprocess(message);
                break;
        
            case 5: // ERROR
                ERRORprocess(message);
                break;
        
            case 6: // DIRQ
                DIRQprocess(message);
                break;
        
            case 7: // LOGRQ
                LOGRQprocess(message);
                break;
        
            case 8: // DELRQ
                DELRQprocess(message);
                break;
        
            case 9: // BCAST
                BCASTprocess(message);
                break;
        
            case 10: // DISC
                DISCprocess(message);
                break;
        
            default:
                break;
        }
        
    }

    @Override
    public boolean shouldTerminate() {
        return shouldTerminate;
    }

    private void RRQprocess(byte[] message) {
        if(isLoggedIn()) {
            byte[] fileNameBytes = new byte[message.length - opCodeLen];
            for (int i = opCodeLen; i < message.length; i++) {
                fileNameBytes[i - opCodeLen] = message[i];
            }
            fileName = new String(fileNameBytes);

            if(fileExists(fileName)) {
                // send data
            } else {
                // send error
            }
        }
    }
    
    private void WRQprocess(byte[] message) {
        if(isLoggedIn()){
            byte[] fileNameBytes = new byte[message.length - opCodeLen];
            for (int i = opCodeLen; i < message.length; i++) {
                fileNameBytes[i - opCodeLen] = message[i];
            }
            fileName = new String(fileNameBytes);
            if(fileExists(fileName)) {
                // send error
            } else {
                // send ack
            }
        }
         
        
    }
    
    private boolean fileExists(String fileName2) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'fileExists'");
    }

    private void DATAprocess(byte[] message) {
        int index = opCodeLen;
        packetSize = TwoBytesToShort(message[index], message[++index]);
        blockNum = TwoBytesToShort(message[++index], message[++index]);
        index++;
        byte[] dataBlock = new byte[packetSize - index];
        for (int i = index; i < dataBlock.length; i++) {
            dataBlock[i - index] = message[i];
        }
        dataBlocks.add(dataBlock);
    }
    
    private void ACKprocess(byte[] message) {
        int index = opCodeLen;

        blockNum = TwoBytesToShort(message[index], message[++index]);

        String ACKmsg = "ACK " + blockNum + " received";
        byte[] ACKmsgInBytes = ACKmsg.getBytes();

        connections.send(connectionId, ACKmsgInBytes);
    }
    
    private void ERRORprocess(byte[] message) {
        int index = opCodeLen;
        short errorCode = TwoBytesToShort(message[index], message[++index]);

        index++;
        byte[] errorMsgBytes = new byte[message.length - index];
        
        for (int i = index; i < errorMsgBytes.length; i++) {
            errorMsgBytes[i - index] = message[i];
        }
        

        errorMsg = "Error " + errorCode + ": " + new String(errorMsgBytes);
        connections.send(connectionId, errorMsg.getBytes());
    }
    
    private void DIRQprocess(byte[] message) {
        if(isLoggedIn()) {
            // send all files in the directory
            // for(String file: files) {
            //     byte[] fileNameBytes = file.getBytes();
            //     connections.send(connectionId, message);
            // }
        } else {
            // send error
        }
    }
    
    private void LOGRQprocess(byte[] message) {
        if(isLoggedIn()) {
            // send error
        }
        else {
            // log in
            byte[] userNameBytes = new byte[message.length - opCodeLen];
            for (int i = opCodeLen; i < message.length; i++) {
                userNameBytes[i - opCodeLen] = message[i];
            }
            userName = new String(userNameBytes);
            loggedClients.add(connectionId);

            // send ack
        }

    }
    
    private void DELRQprocess(byte[] message) {
        byte[] fileNameBytes = new byte[message.length - opCodeLen];
        for (int i = opCodeLen; i < message.length; i++) {
            fileNameBytes[i - opCodeLen] = message[i];
        }
        fileName = new String(fileNameBytes);

        if(fileExists(fileName)) {
            // delete file
        } else {
            // send error
        }
    }
    
    private void BCASTprocess(byte[] message) {
        int index = opCodeLen;
        boolean delOrAdd = message[index] == 0 ? false : true;
        index++;
        byte[] fileNameBytes = new byte[message.length - index];
        for (int i = index; i < message.length; i++) {
            fileNameBytes[i - index] = message[i];
        }
        fileName = new String(fileNameBytes);
        
        String BCASTmsg = delOrAdd ? "File " + fileName + " added" : "File " + fileName + " deleted";

        byte[] BCASTmsgBytes = BCASTmsg.getBytes(); 
        for(int ClientID: loggedClients){
            connections.send(ClientID, BCASTmsgBytes);
        }
    }
    
    private void DISCprocess(byte[] message) {
        connections.disconnect(connectionId);
    }
    
    

    public short TwoBytesToShort(byte firstByte, byte secondByte){
        // converting 2 byte array to a short
         
        return (short) (((short) firstByte) << 8 | (short) (secondByte) & 0x00ff);
    }

    private boolean isLoggedIn() {
        for(int loggedClient: loggedClients){
            if(loggedClient == connectionId){
                return true;
            }
        }
        return false;
    }


    
}
