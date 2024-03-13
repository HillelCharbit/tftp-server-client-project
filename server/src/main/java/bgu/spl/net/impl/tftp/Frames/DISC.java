package bgu.spl.net.impl.tftp.Frames;

import bgu.spl.net.impl.tftp.Frame;

public class DISC extends Frame {
    private short opCode = 10;

    public DISC() {
        super(Frame.CommandTypes.DISC);
    }

    public byte[] toBytes() {
        byte[] opCodeBytes = ShortToTwoBytes(opCode);
        byte[] result = new byte[2];
        System.arraycopy(opCodeBytes, 0, result, 0, 2);
        return result;
    }
    
}
