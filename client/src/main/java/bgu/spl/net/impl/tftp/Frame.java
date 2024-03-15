package bgu.spl.net.impl.tftp;

import bgu.spl.net.impl.tftp.Frames.*;

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

    public static byte[] ShortToTwoBytes(short num) {
        byte[] bytesArr = new byte[2];
        bytesArr[0] = (byte)((num >> 8) & 0x00FF);
        bytesArr[1] = (byte)(num & 0x00FF);
        return bytesArr;
    }

    public static short TwoBytesToShort(byte first, byte second) {
        short result = (short)((first & 0x00FF) << 8);
        result += (short)(second & 0x00FF);
        return result;
    }

    public static Frame getFrameFromString(String str) {
        int index = str.indexOf(" ");
        String[] args = new String[2];

        if (index == -1) {
            args[0] = str;
        } else {
            args[0] = str.substring(0, index);
            args[1] = str.substring(index + 1);
        }
        CommandTypes command = CommandTypes.valueOf(args[0]);
        switch (command) {
            case LOGRQ:
                return new LOGRQ(args[1]);
            case DELRQ:
                return new DELRQ(args[1]);
            case RRQ:
                return new RRQ(args[1]);
            case WRQ:
                return new WRQ(args[1]);
            case DATA:
            case ACK:
            case ERROR:
            case DIRQ:
                return new DIRQ();
            case DISC:
                return new DISC();
            case BCAST:
            default:
                return null;
        }
    }

    abstract public byte[] toBytes();

}
