package bgu.spl.net.impl.tftp.Frames;

import bgu.spl.net.impl.tftp.Frame;

public class DELRQ extends Frame {
    private short opCode = 8;
    private String fileName;
    
    public DELRQ(String fileName) {
        super(Frame.CommandTypes.DELRQ);
        this.fileName = fileName;
    }

    public DELRQ(byte[] bytes) {
        super(Frame.CommandTypes.DELRQ);
        fileName = new String(bytes, 2, bytes.length - 2);
    }

    public String getFileName() {
        return fileName;
    }

    public byte[] toBytes() {
        byte[] opCodeBytes = ShortToTwoBytes(opCode);
        byte[] fileNameBytes = fileName.getBytes();
        byte[] result = new byte[fileNameBytes.length + 3];
        System.arraycopy(opCodeBytes, 0, result, 0, 2);
        System.arraycopy(fileNameBytes, 0, result, 2, fileNameBytes.length);
        result[result.length - 1] = 0;
        return result;
    }
    
}
