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
}