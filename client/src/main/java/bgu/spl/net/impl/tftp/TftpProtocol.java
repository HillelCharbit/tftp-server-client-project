package bgu.spl.net.impl.tftp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingDeque;

import bgu.spl.net.api.MessagingProtocol;
import bgu.spl.net.impl.tftp.Frames.*;


public class TftpProtocol implements MessagingProtocol<Frame>  {
    
    private boolean shouldTerminate = false;
    private short lastBlockNumber = 0;
    private Queue<byte[]> packetQueue = new ConcurrentLinkedQueue<>(); // packets that require ACK only
    private Frame lastFrame = null;
    private String filename = "";
    
    private ArrayDeque<Byte> currentDirName = new ArrayDeque<>();
    String lastCommand = ""; 

    @Override
    public void process(Frame frame) {

        switch (frame.getCommand()) {
            case RRQ:
                break;
            case WRQ:
                break;
            case DATA:
                break;
            case DELRQ:
                break;
            case LOGRQ:
                break;
            case DIRQ:
                break;
            case DISC:
                break;
            case ACK:
                break;
            case BCAST:
                break;
            case ERROR:
                break;
        }
    }

    @Override
    public boolean shouldTerminate() {
        return shouldTerminate;
    }

    private void ACKprocess(ACK ackFrame) {
        short ackBlockNum = TftpEncoderDecoder.bytesToShort(message[2], message[3]);
        OpCodes commandOpCode; // the opcode of the command that required an ack message
        byte[] lastSentPacket = null;

        System.out.println("ACK " + ackBlockNum);

        if (ackBlockNum == 0)
            commandOpCode = lastKeyboardOptOpcode;
        else if (packetsQueue.isEmpty())
            return createErrorMessage(Errors.NOT_DEFINED);
        else {
            lastSentPacket = packetsQueue.peek();

            // the type of the last messege sent.
            commandOpCode = OpCodes.extractOpcode(lastSentPacket);
        }

        short packetBlockNum = commandOpCode == OpCodes.DATA
                ? TftpEncoderDecoder.bytesToShort(lastSentPacket[4], lastSentPacket[5])
                : 0;
        byte[] response = null;

        if (packetBlockNum != ackBlockNum) {
            // last massege blockNumber");
            return createErrorMessage(Errors.NOT_DEFINED);
        }
        }
        else {
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
                if (currentFile != null) { // already in the middle of a file transfer
                    // send error
                    ERROR error = new ERROR((short) 2, "Access violation - File cannot be written");
                    connections.send(connectionId, error);
                }
                //create file
                try {
                    currentFile = Files.createFile(util.getPath(fileName));
                    dataBlocksReceived = new ArrayList<>();
                    // send ack
                    ACK ack = new ACK((short) 0);
                    connections.send(connectionId, ack);
                } catch (Exception e) {
                    ERROR error = new ERROR((short) 2, "Access violation - File cannot be written");
                    connections.send(connectionId, error);
                }
            }
        }

    }

    private void sendDATA(short blockNum, byte[] data, short packetSize) {
        Frame dataFrame = new DATA(blockNum, data, packetSize);
        lastBlockNumSent = blockNum;
        connections.send(connectionId, dataFrame);
    }

    private void DATAprocess(DATA dataFrame) {
        
        // send ack
        ACK ack = new ACK(dataFrame.getBlockNumber());
        connections.send(connectionId, ack);

        if(dataFrame.getBlockNumber() == (short)(dataBlocksReceived.size() + 1)) {
            dataBlocksReceived.add(dataFrame.getData());
            // if last block
            if(dataFrame.getPacketSize() < 512) {
                int totalSize = 0;
                for (byte[] dataBlock : dataBlocksReceived) {
                    totalSize += dataBlock.length;
                }
                // write to file
                byte[] fileData = new byte[totalSize];
                int index = 0;
                for (byte[] dataBlock : dataBlocksReceived) {
                    System.arraycopy(dataBlock, 0, fileData, index, dataBlock.length);
                    index += dataBlock.length;
                }
                
                try (FileOutputStream fileOutputStream = new FileOutputStream(currentFile.toFile())) {
                    fileOutputStream.write(fileData);
                } catch (Exception e) {
                    ERROR error = new ERROR((short) 2, "Access violation - File cannot be written");
                    connections.send(connectionId, error);
                }
                // send BCAST
                sendBCAST(true, currentFile.getFileName().toString());
                currentFile = null;
            }
        } else {
            // send error
            ERROR error = new ERROR((short) 0, "wrong block number in DATA frame");
            connections.send(connectionId, error);
        }
    }
    
    private void DIRQprocess() {
        if(isLoggedIn()) {
            try {
                // create a list of all file names in the directory
                File dir = new File(util.getDirPath());
                File[] files = dir.listFiles();
                String[] fileNames = new String[files.length];
                for (int i = 0; i < files.length; i++) {
                    if (files[i].isFile()){
                        fileNames[i] = files[i].getName();
                    }
                }
                // create a list of bytes, divide the file names with 0 byte at the end of each name
                byte[] dirBytes = new byte[0];
                for (String fileName : fileNames) {
                    byte[] fileNameBytes = fileName.getBytes();
                    byte[] newDirBytes = new byte[dirBytes.length + fileNameBytes.length + 1];
                    System.arraycopy(dirBytes, 0, newDirBytes, 0, dirBytes.length);
                    System.arraycopy(fileNameBytes, 0, newDirBytes, dirBytes.length, fileNameBytes.length);
                    newDirBytes[dirBytes.length + fileNameBytes.length] = 0;
                    dirBytes = newDirBytes;
                }
                // add DATA packets
                short blockNum = 1;
                int index = 0;
                while (index < dirBytes.length) {
                    byte[] data = new byte[512];
                    for (int i = 0; i < 512 && index < dirBytes.length; i++) {
                        data[i] = dirBytes[index];
                        index++;
                    }
                    if (index < 512){
                        byte[] dataToSend = new byte[index-1];
                        System.arraycopy(data, 0, dataToSend, 0, index-1);
                        data = dataToSend;
                    }
                    dataBlocksToSend.add(data);
                    blockNum++;
                }
                sendDATA((short)1, dataBlocksToSend.peek(), (short) dataBlocksToSend.peek().length);
            }
            catch (Exception e) {
                ERROR error = new ERROR((short) 2, "Access violation - File cannot be read");
                connections.send(connectionId, error);
            }
        } else {
            // send error
            ERROR error = new ERROR((short) 6, "User not logged in");
            connections.send(connectionId, error);
        }
    }
    
    private void LOGRQprocess(String userName) {
        if(isLoggedIn()) {
            // send error
            ERROR error = new ERROR((short) 7, "User already logged in");
            connections.send(connectionId, error);
        }
        else {
            connections.loginUser(connectionId, userName);
            ACK ack = new ACK((short) 0);
            connections.send(connectionId, ack);
        }
    }
    
    private void DELRQprocess(String fileName) {
        if(util.isFileExists(fileName)) {
            Path path = util.getPath(fileName);
            try {
                Files.delete(path);
                // send ack
                ACK ack = new ACK((short) 0);
                connections.send(connectionId, ack);
                // send BCAST
                sendBCAST(false, fileName);
            } catch (Exception e) {
                ERROR error = new ERROR((short) 2, "Access violation - File cannot be deleted");
                connections.send(connectionId, error);
            }
        } else {
            // send error
            ERROR error = new ERROR((short) 1, "File not found - DELRQ of non-existing file.");
            connections.send(connectionId, error);
        }
    }
    
    private void sendBCAST(boolean isAdd, String fileName) {
        // send BCAST to all logged in clients
        BCAST bcast = new BCAST(isAdd, fileName);
        for (int connectionId : connections.getConnections().keySet()) {
            if (connections.isLoggedIn(connectionId)) {
                connections.send(connectionId, bcast);
            }
        }

    }
    
    private void DISCprocess() {
        if(isLoggedIn()) {
            // send ack
            ACK ack = new ACK((short) 0);
            connections.send(connectionId, ack);
            // disconnect
            connections.disconnect(connectionId);
            shouldTerminate = true;
        } else {
            // send error
            ERROR error = new ERROR((short) 6, "User not logged in");
            connections.send(connectionId, error);
        }
    }
    
    private boolean isLoggedIn() {
        return connections.isLoggedIn(connectionId);
    }
}
