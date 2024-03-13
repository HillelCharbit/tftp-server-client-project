package bgu.spl.net.impl.tftp.Frames;

import bgu.spl.net.impl.tftp.Frame;

public class DELRQ extends Frame {
    private short opCode = 8;
    private String fileName;
    
    public DELRQ(String fileName) {
        super(Frame.CommandTypes.DELERQ);
        this.fileName = fileName;
    }

    public DELRQ(byte[] bytes) {
        super(Frame.CommandTypes.DELERQ);
        fileName = new String(bytes, 2, bytes.length - 2);
    }

    public String getFileName() {
        return fileName;
    }

    public byte[] toBytes() {
        byte[] opCodeBytes = ShortToTwoBytes(opCode);
        byte[] fileNameBytes = fileName.getBytes();
        byte[] result = new byte[fileNameBytes.length + 2];
        System.arraycopy(opCodeBytes, 0, result, 0, 2);
        System.arraycopy(fileNameBytes, 0, result, 2, fileNameBytes.length);
        return result;
    }
    
}
