package bgu.spl.net.impl.tftp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;

import bgu.spl.net.api.BidiMessagingProtocol;
import bgu.spl.net.impl.tftp.Frames.*;
import bgu.spl.net.srv.Connections;


public class TftpProtocol implements BidiMessagingProtocol<Frame>  {
    
    private boolean shouldTerminate = false;
    private int connectionId;
    private Connections<Frame> connections;
    private Queue<byte[]> dataBlocksToSend;
    private ArrayList<byte[]> dataBlocksReceived;
    private short lastBlockNumSent;
    private Path currentFile;

    private TftpProtocolUtil util = new TftpProtocolUtil();
 
    @Override
    public void start(int connectionId, Connections<Frame> connections) {
        this.connectionId = connectionId;
        this.connections = connections;
        dataBlocksToSend = new LinkedBlockingDeque<>();
    }   

    @Override
    public void process(Frame frame) {
        if(frame == null || frame.getCommand() == null ) {
            ERROR error = new ERROR((short) 4, "Illegal TFTP operation - Unknown Opcode");
            connections.send(connectionId, error);
        }

        if (!isLoggedIn() && frame.getCommand() != Frame.CommandTypes.LOGRQ) {
            ERROR error = new ERROR((short) 6, "User not logged in");
            connections.send(connectionId, error);
            return;
        }
      
        switch (frame.getCommand()) {
            case RRQ:
                RRQ RRQframe = (RRQ) frame;
                RRQprocess(RRQframe.getFileName());
                break;
            case WRQ:
                WRQ WRQframe = (WRQ) frame;
                WRQprocess(WRQframe.getFileName());
                break;
            case DATA:
                DATA DATAframe = (DATA) frame;
                DATAprocess(DATAframe);
                break;
            case DELRQ:
                DELRQ DELRQframe = (DELRQ) frame;
                DELRQprocess(DELRQframe.getFileName());
                break;
            case LOGRQ:
                LOGRQ LOGRQframe = (LOGRQ) frame;
                LOGRQprocess(LOGRQframe.getUserName());
                break;
            case DIRQ:
                DIRQprocess();
                break;
            case DISC:
                DISCprocess();
                break;
            case ACK:
                ACK ACKframe = (ACK) frame;
                ACKprocess(ACKframe);
                break;
            case BCAST:
                // do nothing
            case ERROR:
                // do nothing
        }
    }

    @Override
    public boolean shouldTerminate() {
        return shouldTerminate;
    }

    private void RRQprocess(String fileName) {
        try {
            SharedResources.semaphore.acquire();
            if(util.isFileExists(fileName)) {
                File file = util.getPath(fileName).toFile();
                try (FileInputStream fileInputStream = new FileInputStream(file)) {
                    byte[] data = new byte[512];
                    int bytesRead;
                    while ((bytesRead = fileInputStream.read(data)) != -1) {
                        byte[] dataToSend = new byte[bytesRead];
                        System.arraycopy(data, 0, dataToSend, 0, bytesRead);
                        dataBlocksToSend.add(dataToSend);
                    }
                    // send first data block
                    byte[] firstDataBlock = dataBlocksToSend.peek();
                    sendDATA((short) 1, firstDataBlock, (short) firstDataBlock.length);
                } catch (Exception e) {
                    ERROR error = new ERROR((short) 2, "Access violation - File cannot be written, read or deleted");
                    connections.send(connectionId, error);
                }
            } else {
                ERROR error = new ERROR((short) 1, "File not found - RRQ DELRQ of non-existing file.");
                connections.send(connectionId, error);
            }
        }
        catch (Exception e) {
            ERROR error = new ERROR((short) 2, "Access violation - File cannot be read");
            connections.send(connectionId, error);
        }
        finally {
            SharedResources.semaphore.release();
        }
    }


    private void ACKprocess(ACK ackFrame) {
        if(ackFrame.getBlockNumber() == lastBlockNumSent) {
            System.out.println("ACK " + ackFrame.getBlockNumber());
            dataBlocksToSend.remove();
            if(!dataBlocksToSend.isEmpty()) {
                byte[] nextDataBlock = dataBlocksToSend.peek();
                sendDATA((short) (lastBlockNumSent + 1), nextDataBlock, (short) nextDataBlock.length);
            }
        }
        else {
            // send error
            ERROR error = new ERROR((short) 0, "wrong block number in ACK frame");
            connections.send(connectionId, error);
        }

                
                
    }
        
    
    private void WRQprocess(String fileName) {
        try {
            SharedResources.semaphore.acquire();
            if(util.isFileExists(fileName)) {
                ERROR error = new ERROR((short) 5, "File already exists");
                connections.send(connectionId, error);
            } 
            else {
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
        catch (Exception e) {
            ERROR error = new ERROR((short) 2, "Access violation - File cannot be written");
            connections.send(connectionId, error);
        }
        finally {
            SharedResources.semaphore.release();
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
        try {
            SharedResources.semaphore.acquire();
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
        finally {
            SharedResources.semaphore.release();
        }
    }
    
    private void LOGRQprocess(String userName) {
        SharedResources.LoggedConnectionIdToUsername.put(connectionId, userName);
        ACK ack = new ACK((short) 0);
        connections.send(connectionId, ack);
    }
    
    private void DELRQprocess(String fileName) {
        try{
            SharedResources.semaphore.acquire();
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
        catch (Exception e) {
            ERROR error = new ERROR((short) 2, "Access violation - File cannot be deleted");
            connections.send(connectionId, error);
        }
        finally {
            SharedResources.semaphore.release();
        }
    }
    
    private void sendBCAST(boolean isAdd, String fileName) {
        // send BCAST to all logged in clients
        BCAST bcast = new BCAST(isAdd, fileName);
        for (int loggedConnectionId : SharedResources.LoggedConnectionIdToUsername.keySet()) {
            {
                connections.send(loggedConnectionId, bcast);
            } 
        }

    }
    
    private void DISCprocess() {
        SharedResources.LoggedConnectionIdToUsername.remove(connectionId);
        // send ack
        ACK ack = new ACK((short) 0);
        connections.send(connectionId, ack);
        // disconnect
        connections.disconnect(connectionId);
        shouldTerminate = true;
    }
    
    private boolean isLoggedIn() {
        return SharedResources.LoggedConnectionIdToUsername.containsKey(connectionId);
    }
}
