package bgu.spl.net.impl.tftp.Frames;

import bgu.spl.net.impl.tftp.Frame;

public class ACK extends Frame {

    private short opCode = 4;
    private short blockNumber;

    public ACK(short blockNumber) {
        super(Frame.CommandTypes.ACK);
        this.blockNumber = blockNumber;
    }

    public ACK(byte[] bytes) {
        super(Frame.CommandTypes.ACK);
        blockNumber = TwoBytesToShort(bytes[2], bytes[3]);
    }
    
    public short getBlockNumber() {
        return blockNumber;
    }

    public byte[] toBytes() {
        byte[] opCodeBytes = ShortToTwoBytes(opCode);
        byte[] blockNumberBytes = ShortToTwoBytes(blockNumber);
        byte[] result = new byte[4];
        System.arraycopy(opCodeBytes, 0, result, 0, 2);
        System.arraycopy(blockNumberBytes, 0, result, 2, 2);
        return result;
    }
}
