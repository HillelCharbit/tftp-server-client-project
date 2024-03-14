package bgu.spl.net.impl.tftp;

import java.util.function.Supplier;

import bgu.spl.net.api.BidiMessagingProtocol;
import bgu.spl.net.api.MessageEncoderDecoder;
import bgu.spl.net.srv.BaseServer;
import bgu.spl.net.srv.BlockingConnectionHandler;
import bgu.spl.net.srv.Server;

public class TftpServer extends BaseServer<Frame>{

    public TftpServer(int port, Supplier<BidiMessagingProtocol<Frame>> protocolFactory,
            Supplier<MessageEncoderDecoder<Frame>> encdecFactory) {
        super(port, protocolFactory, encdecFactory);
        Server.threadPerClient(port, protocolFactory, encdecFactory).serve();
    }

    public static void main(String[] args) {
        // if (args.length != 1) {
        //     System.err.println("Usage: TftpServer <port>");
        //     System.exit(1);
        // }
        try {
            int port = 7777;
            TftpServer server = new TftpServer(port, TftpProtocol::new, TftpEncoderDecoder::new);
            server.serve();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void execute(BlockingConnectionHandler<Frame> handler) {
        new Thread(handler).start();
    }
}
