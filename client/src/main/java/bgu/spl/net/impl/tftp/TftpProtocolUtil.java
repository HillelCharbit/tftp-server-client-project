package bgu.spl.net.impl.tftp;

import java.nio.file.Path;
import java.nio.file.Paths;

public class TftpProtocolUtil {

    public Path getPath(String fileName) {
        return Paths.get(System.getProperty("user.dir") + "/Files/" + fileName);
    }

    public String getDirPath() {
        return System.getProperty("user.dir") + "/Files/";
    }

    public boolean isFileExists(Path path) {
        return path.toFile().exists();
    }

    public boolean isFileExists(String fileName) {
        return isFileExists(getPath(fileName));
    }
    
}
