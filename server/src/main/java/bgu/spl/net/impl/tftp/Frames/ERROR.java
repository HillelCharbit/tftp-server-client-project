package bgu.spl.net.impl.tftp.Frames;

import bgu.spl.net.impl.tftp.Frame;

public class ERROR extends Frame{
    private short opCode = 5;
    private short errorCode;
    private String errorMsg;

    public ERROR(short errorCode, String errorMsg) {
        super(Frame.CommandTypes.ERROR);
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
    }

    public ERROR(byte[] bytes) {
        super(Frame.CommandTypes.ERROR);
        this.errorCode = TwoBytesToShort(bytes[2], bytes[3]);
        this.errorMsg = new String(bytes, 4, bytes.length - 5);
    }

    public short getErrorCode() {
        return errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public byte[] toBytes() {
        byte[] opCodeBytes = ShortToTwoBytes(opCode);
        byte[] errorCodeBytes = ShortToTwoBytes(errorCode);
        byte[] errorMsgBytes = errorMsg.getBytes();
        byte[] result = new byte[errorMsgBytes.length + 4];
        System.arraycopy(opCodeBytes, 0, result, 0, 2);
        System.arraycopy(errorCodeBytes, 0, result, 2, 2);
        System.arraycopy(errorMsgBytes, 0, result, 4, errorMsgBytes.length);
        return result;
    }    
}
