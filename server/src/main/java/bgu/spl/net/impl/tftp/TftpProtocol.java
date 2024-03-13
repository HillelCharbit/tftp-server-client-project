package bgu.spl.net.impl.tftp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import bgu.spl.net.api.BidiMessagingProtocol;
import bgu.spl.net.impl.tftp.Frames.*;
import bgu.spl.net.srv.Connections;


public class TftpProtocol implements BidiMessagingProtocol<Frame>  {
    
    private boolean shouldTerminate = false;
    private int connectionId;
    private Connections<Frame> connections;
    private ArrayList<byte[]> dataBlocks;
    private short lastBlockNumSent;



    private TftpProtocolUtil util = new TftpProtocolUtil();
 
    private final int opCodeLen = 2;


    @Override
    public void start(int connectionId, Connections<Frame> connections) {
        this.connectionId = connectionId;
        this.connections = connections;
    }   

    @Override
    public void process(Frame frame) {
        if(frame == null || frame.getCommand() == null ) {
            ERROR error = new ERROR((short) 4, "Illegal TFTP operation - Unknown Opcode");
            connections.send(connectionId, error);
        }
      
        switch (frame.getCommand()) {
            case RRQ:
                RRQ RRQframe = (RRQ) frame;
                RRQprocess(RRQframe.getFileName());
            case WRQ:
                WRQ WRQframe = (WRQ) frame;
                WRQprocess(WRQframe.getFileName());
            case DATA:
                DATA DATAframe = (DATA) frame;
                DATAprocess(DATAframe);
            case DELRQ:
                DELRQ DELRQframe = (DELRQ) frame;
                DELRQprocess(DELRQframe.getFileName());
            // case LOGRQ:
            //     LOGRQ LOGRQframe = (LOGRQ) frame;
            //     LOGRQprocess(LOGRQframe.getUserName());
            // case DIRQ:
            //     DIRQprocess();
            // case DISC:
            //     DISCprocess();
            case ACK:
                ACK ACKframe = (ACK) frame;
                ACKprocess(ACKframe);
        }
    }

    @Override
    public boolean shouldTerminate() {
        return shouldTerminate;
    }

    private void RRQprocess(String fileName) {
        if(isLoggedIn()) {
            if(util.isFileExists(fileName)) {
                File file = util.getPath(fileName).toFile();
                try (FileInputStream fileInputStream = new FileInputStream(file)) {
                    byte[] data = new byte[512];
                    int bytesRead;
                    short blockNum = 1;
                    while ((bytesRead = fileInputStream.read(data)) != -1) {
                        sendDATA(blockNum, data);
                        lastBlockNumSent = blockNum;
                        // sleep until ack
                        this.wait();
                        blockNum++;
                    }
                } catch (Exception e) {
                    ERROR error = new ERROR((short) 2, "Access violation - File cannot be written, read or deleted");
                    connections.send(connectionId, error);
                }
            } else {
                ERROR error = new ERROR((short) 1, "File not found - RRQ DELRQ of non-existing file.");
                connections.send(connectionId, error);
            }
        }
    }

    private void ACKprocess(ACK ackFrame) {
        short blockNum = ackFrame.getBlockNumber();
        if(blockNum == lastBlockNumSent) {
            // notify
            this.notifyAll();
        } else {
            // send error
            ERROR error = new ERROR((short) 0, "wrong block number in ACK frame");
            connections.send(connectionId, error);
        }
    }
        
    
    private void WRQprocess(String fileName) {
        if(isLoggedIn()){
            if(util.isFileExists(fileName)) {
                ERROR error = new ERROR((short) 5, "File already exists");
                connections.send(connectionId, error);
            } else {
                // send ack
                ACK ack = new ACK((short) 0);
                connections.send(connectionId, ack);
                dataBlocks = new ArrayList<>();
            }
        }
    }

    private void sendDATA(short blockNum, byte[] data) {
        Frame dataFrame = new DATA(blockNum, data);
        connections.send(connectionId, dataFrame);
    }

    private void DATAprocess(DATA dataFrame) {
        short blockNum = dataFrame.getBlockNumber();
        byte[] data = dataFrame.getData();
        if(blockNum == dataBlocks.size() + 1) {
            dataBlocks.add(data);
            // if last block
            if(data.length < 512) {
                // write to file
                byte[] fileData = new byte[dataBlocks.size() * 512];
                for(int i = 0; i < dataBlocks.size(); i++) {
                    for(int j = 0; j < 512; j++) {
                        fileData[i * 512 + j] = dataBlocks.get(i)[j];
                    }
                }
                try (FileOutputStream fileOutputStream = new FileOutputStream(util.getPath("fileName").toFile())) {
                    fileOutputStream.write(fileData);
                } catch (Exception e) {
                    ERROR error = new ERROR((short) 2, "Access violation - File cannot be written");
                    connections.send(connectionId, error);
                }
                // send BCAST
            }
            // send ack
            ACK ack = new ACK(blockNum);
            connections.send(connectionId, ack);
        } else {
            // send error
            ERROR error = new ERROR((short) 0, "wrong block number in DATA frame");
            connections.send(connectionId, error);
        }
    }
    
    // private void ACKprocess(byte[] message) {
    //     int index = opCodeLen;

    //     blockNum = util.TwoBytesToShort(message[index], message[++index]);

    //     String ACKmsg = "ACK " + blockNum + " received";
    //     byte[] ACKmsgInBytes = ACKmsg.getBytes();

    //     connections.send(connectionId, ACKmsgInBytes);
    // }
    
    // private void ERRORprocess(byte[] message) {
    //     int index = opCodeLen;
    //     short errorCode = util.TwoBytesToShort(message[index], message[++index]);

    //     index++;
    //     byte[] errorMsgBytes = new byte[message.length - index];
        
    //     for (int i = index; i < errorMsgBytes.length; i++) {
    //         errorMsgBytes[i - index] = message[i];
    //     }
        

    //     errorMsg = "Error " + errorCode + ": " + new String(errorMsgBytes);
    //     connections.send(connectionId, errorMsg.getBytes());
    // }
    
    // private void DIRQprocess() {
    //     if(isLoggedIn()) {
    //         // send all files in the directory
    //         // for(String file: files) {
    //         //     byte[] fileNameBytes = file.getBytes();
    //         //     connections.send(connectionId, message);
    //         // }
    //     } else {
    //         // send error
    //     }
    // }
    
    // private void LOGRQprocess(String userName) {
    //     if(isLoggedIn()) {
    //         // send error
    //     }
    //     else {
    //         // log in

    //         // send ack
    //     }

    // }
    
    private void DELRQprocess(String fileName) {
        if(util.isFileExists(fileName)) {
            Path path = util.getPath(fileName);
            try {
                Files.delete(path);
                // send ack
                ACK ack = new ACK((short) 0);
                connections.send(connectionId, ack);
                // send BCAST

            } catch (Exception e) {
                ERROR error = new ERROR((short) 2, "Access violation - File cannot be deleted");
                connections.send(connectionId, error);
            }
        } else {
            // send error
            ERROR error = new ERROR((short) 1, "File not found - DELRQ of non-existing file.");
        }
    }
    
    // private void BCASTprocess(byte[] message) {
    //     int index = opCodeLen;
    //     boolean delOrAdd = message[index] == 0 ? false : true;
    //     index++;
    //     byte[] fileNameBytes = new byte[message.length - index];
    //     for (int i = index; i < message.length; i++) {
    //         fileNameBytes[i - index] = message[i];
    //     }
    //     fileName = new String(fileNameBytes);
        
    //     String BCASTmsg = delOrAdd ? "File " + fileName + " added" : "File " + fileName + " deleted";

    //     byte[] BCASTmsgBytes = BCASTmsg.getBytes(); 
    //     for(int ClientID: loggedClients){
    //         connections.send(ClientID, BCASTmsgBytes);
    //     }
    // }
    
    private void DISCprocess() {
        connections.disconnect(connectionId);
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
