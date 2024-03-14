package bgu.spl.net.impl.tftp;

public abstract class Frame {

    public enum CommandTypes {
        LOGRQ,
        DELRQ,
        RRQ,
        WRQ,
        DIRQ,
        DISC,
        ACK,
        DATA,
        ERROR,
        BCAST,
    }

    private CommandTypes command;

    public Frame(CommandTypes command) {
        this.command = command;
    }

    public CommandTypes getCommand() {
        return command;
    }

    protected byte[] ShortToTwoBytes(short num) {
        byte[] bytesArr = new byte[2];
        bytesArr[0] = (byte)((num >> 8) & 0xFF);
        bytesArr[1] = (byte)(num & 0xFF);
        return bytesArr;
    }

    protected short TwoBytesToShort(byte first, byte second) {
        short result = (short)((first & 0xFF) << 8);
        result += (short)(second & 0xFF);
        return result;
    }

    abstract public byte[] toBytes();

}
