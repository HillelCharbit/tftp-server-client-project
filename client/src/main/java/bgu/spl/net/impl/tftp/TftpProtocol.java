package bgu.spl.net.impl.tftp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;

import bgu.spl.net.api.MessagingProtocol;
import bgu.spl.net.impl.tftp.Frames.*;


public class TftpProtocol implements MessagingProtocol<Frame>  {
    
    private boolean shouldTerminate = false;
    private Queue<byte[]> dataBlocksToSend;
    private ArrayList<byte[]> dataBlocksReceived;
    private short lastBlockNumSent;
    private Frame.CommandTypes lastCommandSent;
    private File processFile;

    private TftpProtocolUtil util = new TftpProtocolUtil();
 

    @Override
    public Frame process(Frame frame) {
        Frame response = null;
        switch (frame.getCommand()) {
            case DATA:
                DATA DATAframe = (DATA) frame;
                response = DATAprocess(DATAframe);
                break;
            case ACK:
                ACK ACKframe = (ACK) frame;
                response = ACKprocess(ACKframe);
                break;
            case BCAST:
                BCAST BCASTframe = (BCAST) frame;
                response = BCASTprocess(BCASTframe);
            case ERROR:
                ERROR ERRORframe = (ERROR) frame;
                response = ERRORprocess(ERRORframe);
        }
        return response;
    }

    @Override
    public boolean shouldTerminate() {
        return shouldTerminate;
    }


    private Frame ACKprocess(ACK ackFrame) {
        if (lastCommandSent == Frame.CommandTypes.WRQ) {
            readFile();
            return new DATA(++lastBlockNumSent, dataBlocksToSend.peek(), (short) dataBlocksToSend.peek().length);
        }
        if (lastCommandSent == Frame.CommandTypes.DATA) {
            if (dataBlocksToSend.isEmpty()) {
                System.out.println("WRQ " + processFile.getName() + " complete");
                processFile = null;
                return null;
            }
            return new DATA(++lastBlockNumSent, dataBlocksToSend.peek(), (short) dataBlocksToSend.peek().length);
        }
        if (lastCommandSent == Frame.CommandTypes.DISC) {
            shouldTerminate = true;
            return null;
        }
        return null;
    }

    private Frame DATAprocess(DATA dataFrame) {
        if (lastCommandSent == Frame.CommandTypes.RRQ) {
            try (FileOutputStream fos = new FileOutputStream(processFile, true)) {
                fos.write(dataFrame.getData());
                if (dataFrame.getPacketSize() < 512) {
                    System.out.println("RRQ " + processFile.getName() + " complete");
                    processFile = null;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else if (lastCommandSent == Frame.CommandTypes.DIRQ) {
            dataBlocksReceived.add(dataFrame.getData());
            if (dataFrame.getPacketSize() < 512) {
                writeReceivedDIRQ();
            }
        }
        return new ACK(dataFrame.getBlockNumber());
    }

    private Frame BCASTprocess(BCAST bcastFrame) {
        if (!bcastFrame.isAdded()) {
            System.out.println("BCAST del " + bcastFrame.getFileName());
        }
        else {
            System.out.println("BCAST add " + bcastFrame.getFileName());
        }
        return null;
    }

    private Frame ERRORprocess(ERROR errorFrame) {
        System.out.println("ERROR " + errorFrame.getErrorCode() + " " + errorFrame.getErrorMsg());
        return null;
    }
    
    private void writeReceivedDIRQ() {
        for (byte[] block : dataBlocksReceived) {
            int start = 0; 
            for (int i = 0; i < block.length; i++) {
                if (block[i] == 0) { 
                    String name = new String(block, start, i - start, StandardCharsets.UTF_8);
                    System.out.println(name);
                    start = i + 1; // Move to the next name's start
                }
            }
            // Check if there's a last file name without a zero byte at the end
            if (start < block.length) {
                String name = new String(block, start, block.length - start, StandardCharsets.UTF_8);
                System.out.println(name);
            }
        }
    }
    
    private void readFile() {
        try (FileInputStream fis = new FileInputStream(processFile)) {
            byte[] buffer = new byte[512];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                byte[] data = new byte[bytesRead];
                System.arraycopy(buffer, 0, data, 0, bytesRead);
                dataBlocksToSend.add(data);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

