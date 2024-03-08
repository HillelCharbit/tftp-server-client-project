package bgu.spl.net.impl.tftp;

import java.util.Arrays;
import bgu.spl.net.api.MessageEncoderDecoder;

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

    private void pushByte(byte nextByte) {
        if (len >= bytes.length) {
            bytes = Arrays.copyOf(bytes, len * 2);
        }

        bytes[len++] = nextByte;
    }

    @Override
    public byte[] encode(byte[] message) {
        return (message + "\n").getBytes(); //uses utf8 by default
    }

    public short firstTwoBytesToShort(byte[] bytes){
        // converting 2 byte array to a short
        if(bytes.length < 2)
            throw new IllegalArgumentException("byte array must be of length at least 2");
         
        
        return (short) (((short) bytes[0]) << 8 | (short) (bytes[1]) & 0x00ff);
    }
}