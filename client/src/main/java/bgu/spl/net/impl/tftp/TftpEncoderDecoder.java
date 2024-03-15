package bgu.spl.net.impl.tftp;

import java.util.Arrays;
import bgu.spl.net.api.MessageEncoderDecoder;
import bgu.spl.net.impl.tftp.Frames.*;

public class TftpEncoderDecoder implements MessageEncoderDecoder<Frame>{

    private byte[] bytes = new byte[1 << 10]; //start with 1k
    private int len = 0;
    Frame.CommandTypes command = null;
    boolean completed = false;
    private int expectedLength = 0;

    private byte[] decodeNextByte(byte nextByte) {
        //notice that the top 128 ascii characters have the same representation as their utf-8 counterparts
        //this allow us to do the following comparison
        if (command == null) { // still didn't get the opCode
            pushByte(nextByte);
            if (len == 2) {
                command = opCodeToCommand(TwoBytesToShort(bytes[0], bytes[1]));
                setExpectedLength();
                if (len == expectedLength) {
                    byte[] result = extractResult();
                    reset();
                    return result;
                }
            }
            
            return null;
        } 
        else {
            switch (command) {
                case ACK:
                    completed = pushByteUntilExpectedLength(nextByte);
                    if (completed) {
                        byte[] result = extractResult();
                        reset();
                        return result;
                    }
                    break;
                case DATA:
                    completed = pushByteUntilExpectedLength(nextByte);
                    if (completed) {
                        if (len == 4) {
                            setExpectedLength(TwoBytesToShort(bytes[2], bytes[3]) + 6);
                        }
                        else {
                            byte[] result = extractResult();
                            reset();
                            return result;
                        }
                    }
                    break;
                case BCAST:
                    completed = pushByteUntilZero(nextByte);
                    if (completed) {
                        byte[] result = extractResult();
                        reset();
                        return result;
                    }
                    break;
                case ERROR:
                    completed = pushByteUntilZero(nextByte);
                    if (completed) {
                        byte[] result = extractResult();
                        reset();
                        return result;
                    }
                    break;
            }
            return null;
        }
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

    private boolean pushByteUntilZero(byte nextByte) {
        if (nextByte == 0) {
            return true;
        }
        pushByte(nextByte);
        return false;
    }

    private boolean pushByteUntilExpectedLength(byte nextByte) {
        pushByte(nextByte);
        if (len == expectedLength) {
            return true;
        }
        return false;
    }


    private byte[] extractResult() {
        byte[] result = Arrays.copyOf(bytes, len);
        return result;
    }

    private void reset() {
        len = 0;
        command = null;
        completed = false;
        expectedLength = 0;
    }

    private void setExpectedLength() {
        switch (command) {
            case ACK:
                expectedLength = 4;
                break;
            case DISC:
                expectedLength = 2;
                break;
            case DIRQ:
                expectedLength = 2;
                break;
            case DATA:
                expectedLength = 4;
                break;
        }
    }

    private void setExpectedLength(int length) {
        expectedLength = length;
    }

    @Override
    public byte[] encode(Frame message) {
        byte[] result = message.toBytes();
        return result;
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
            case 3: // DATA
                return Frame.CommandTypes.DATA;
            case 4: // ACK
                return Frame.CommandTypes.ACK;
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
            case DATA:
                return new DATA(bytes);
            default:
                return null;
        }                
    }
}