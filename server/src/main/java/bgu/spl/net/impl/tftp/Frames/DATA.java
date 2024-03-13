package bgu.spl.net.impl.tftp.Frames;

import bgu.spl.net.impl.tftp.Frame;

public class DATA extends Frame {

    private short opCode = 3;
    private short packetSize;
    private short blockNumber;
    private byte[] data;

    public DATA(short blockNumber, byte[] data) {
        super(Frame.CommandTypes.DATA);
        this.blockNumber = blockNumber;
        this.data = data;
        this.packetSize = (short) data.length;
    }

    public DATA(byte[] bytes) {
        super(Frame.CommandTypes.DATA);
        packetSize = TwoBytesToShort(bytes[2], bytes[3]);
        blockNumber = TwoBytesToShort(bytes[4], bytes[5]);
        data = new byte[packetSize];
        System.arraycopy(bytes, 6, data, 0, packetSize);
    }
    
    public byte[] getData() {
        return data;
    }

    public short getBlockNumber() {
        return blockNumber;
    }

    public byte[] toBytes() {
        byte[] opCodeBytes = ShortToTwoBytes(opCode);
        byte[] packetSizeBytes = ShortToTwoBytes(packetSize);
        byte[] blockNumberBytes = ShortToTwoBytes(blockNumber);
        byte[] result = new byte[packetSize + 6];
        System.arraycopy(opCodeBytes, 0, result, 0, 2);
        System.arraycopy(packetSizeBytes, 0, result, 2, 2);
        System.arraycopy(blockNumberBytes, 0, result, 4, 2);
        System.arraycopy(data, 0, result, 6, packetSize);
        return result;
    }        
}
