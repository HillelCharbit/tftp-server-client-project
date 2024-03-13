package bgu.spl.net.impl.tftp;

import java.nio.file.Path;
import java.nio.file.Paths;

public class TftpProtocolUtil {
    
    public short TwoBytesToShort(byte first, byte second) {
        return (short)((first & 0xff) << 8 | (second & 0xff));
    }

    public byte[] ShortToTwoBytes(short num) {
        byte[] bytesArr = new byte[2];
        bytesArr[0] = (byte)((num >> 8) & 0xFF);
        bytesArr[1] = (byte)(num & 0xFF);
        return bytesArr;
    }

    public String BytesToString(byte[] bytes, int start, int end) {
        return new String(bytes, start, end - start);
    }

    public Path getPath(String fileName) {
        return Paths.get(System.getProperty("user.dir") + "/Files/" + fileName);
    }

    public boolean isFileExists(Path path) {
        return path.toFile().exists();
    }

    public boolean isFileExists(String fileName) {
        return isFileExists(getPath(fileName));
    }



}
