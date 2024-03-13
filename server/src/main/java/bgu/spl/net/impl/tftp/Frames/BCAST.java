package bgu.spl.net.impl.tftp.Frames;

import bgu.spl.net.impl.tftp.Frame;

public class BCAST extends Frame {
    private short opCode = 9;
    private boolean added;
    private String fileName;

    public BCAST(boolean added, String fileName) {
        super(Frame.CommandTypes.BCAST);
        this.added = added;
        this.fileName = fileName;
    }

    public BCAST(byte[] bytes) {
        super(Frame.CommandTypes.BCAST);
        this.added = bytes[2] == 1;
        this.fileName = new String(bytes, 3, bytes.length - 3);
    }

    public byte[] toBytes() {
        byte[] opCodeBytes = ShortToTwoBytes(opCode);
        byte addedByte = (byte) (added ? 1 : 0);
        byte[] fileNameBytes = fileName.getBytes();
        byte[] result = new byte[fileNameBytes.length + 4];
        System.arraycopy(opCodeBytes, 0, result, 0, 2);
        result[2] = addedByte;
        System.arraycopy(fileNameBytes, 0, result, 3, fileNameBytes.length);
        return result;
    }
}
