package bgu.spl.net.impl.tftp.Frames;

import bgu.spl.net.impl.tftp.Frame;

public class LOGRQ extends Frame {
    private short opCode = 7;
    private String userName;

    public LOGRQ(String userName) {
        super(Frame.CommandTypes.LOGRQ);
        this.userName = userName;
    }

    public LOGRQ(byte[] bytes) {
        super(Frame.CommandTypes.LOGRQ);
        userName = new String(bytes, 2, bytes.length - 2);
    }

    public String getUserName() {
        return userName;
    }

    public byte[] toBytes() {
        byte[] opCodeBytes = ShortToTwoBytes(opCode);
        byte[] userNameBytes = userName.getBytes();
        byte[] result = new byte[userNameBytes.length + 3];
        System.arraycopy(opCodeBytes, 0, result, 0, 2);
        System.arraycopy(userNameBytes, 0, result, 2, userNameBytes.length);
        result[result.length - 1] = 0;
        return result;
    }
    
}
