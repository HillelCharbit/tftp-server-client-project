package bgu.spl.net.impl.tftp;

import java.util.function.Supplier;

import bgu.spl.net.api.MessageEncoderDecoder;
import bgu.spl.net.api.MessagingProtocol;
import bgu.spl.net.srv.BaseServer;
import bgu.spl.net.srv.BlockingConnectionHandler;
import bgu.spl.net.srv.Server;

public class TftpServer extends BaseServer<byte[]>{

    public TftpServer(int port, Supplier<MessagingProtocol<byte[]>> protocolFactory,
            Supplier<MessageEncoderDecoder<byte[]>> encdecFactory) {
        super(port, protocolFactory, encdecFactory);
        Server.threadPerClient(port, protocolFactory, encdecFactory).serve();
    }

    @Override
    protected void execute(BlockingConnectionHandler<byte[]> handler) {
        new Thread(handler).start();
    }
}
