package bgu.spl.net.impl.tftp.Frames;

import bgu.spl.net.impl.tftp.Frame;

public class DIRQ extends Frame {
    private short opCode = 6;

    public DIRQ() {
        super(Frame.CommandTypes.DIRQ);
    }

    public byte[] toBytes() {
        byte[] opCodeBytes = ShortToTwoBytes(opCode);
        byte[] result = new byte[2];
        System.arraycopy(opCodeBytes, 0, result, 0, 2);
        return result;
    }
    
}
