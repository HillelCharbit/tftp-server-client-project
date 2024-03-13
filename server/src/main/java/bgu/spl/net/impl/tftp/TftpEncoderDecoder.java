package bgu.spl.net.impl.tftp;

import java.util.Arrays;
import bgu.spl.net.api.MessageEncoderDecoder;
import bgu.spl.net.impl.tftp.Frames.*;

public class TftpEncoderDecoder implements MessageEncoderDecoder<byte[]>{

    private byte[] bytes = new byte[1 << 10]; //start with 1k
    private int len = 0;

    @Override
    public byte[] decodeNextByte(byte nextByte) {
        //notice that the top 128 ascii characters have the same representation as their utf-8 counterparts
        //this allow us to do the following comparison
        if (nextByte == '\n') {
            len = 0;
            return bytes;
        }

        pushByte(nextByte);
        return null; //not a line yet
    }   

    public Frame decodeNextByteFrame(byte nextByte) {
        byte[] bytes = decodeNextByte(nextByte);
        if (bytes != null) {
            short opCode = TwoBytesToShort(bytes[0], bytes[1]);
            Frame.CommandTypes command = opCodeToCommand(opCode);
            Frame frame = createFrame(command, bytes);
            return frame;
        }
        return null;
    }
        
    private void pushByte(byte nextByte) {
        if (len >= bytes.length) {
            bytes = Arrays.copyOf(bytes, len * 2);
        }

        bytes[len++] = nextByte;
    }

    @Override
    public byte[] encode(Frame message) {
        return (message + "\n").getBytes(); //uses utf8 by default
    }

    private short TwoBytesToShort(byte first, byte second) {
        return (short)((first & 0xff) << 8 | (second & 0xff));
    }

    private Frame.CommandTypes opCodeToCommand(short opCode)
    {
        switch (opCode) {
            case 1: // RRQ
                return Frame.CommandTypes.RRQ;
            case 2: // WRQ
                return Frame.CommandTypes.WRQ;
            case 6: // DIRQ
                return Frame.CommandTypes.DIRQ;
            case 7: // LOGRQ
                return Frame.CommandTypes.LOGRQ;
            case 8: // DELRQ
                return Frame.CommandTypes.DELRQ;
            case 10: // DISC
                return Frame.CommandTypes.DISC;
            default:
                return null;
        }
    }

    private Frame createFrame(Frame.CommandTypes command, byte[] bytes)
    {
        switch (command) {
            case ACK:
                return new ACK(bytes);
            case RRQ:
                return new RRQ(bytes);
            case WRQ:
                return new WRQ(bytes);
            case DIRQ:
                return new DIRQ();
            case LOGRQ:
                return new LOGRQ(bytes);
            case DELRQ:
                return new DELRQ(bytes);
            case DISC:
                return new DISC();
            default:
                return null;
        }                
        
}