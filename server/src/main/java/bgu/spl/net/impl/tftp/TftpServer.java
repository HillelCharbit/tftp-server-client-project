package bgu.spl.net.impl.tftp;

import java.util.function.Supplier;

import bgu.spl.net.api.MessageEncoderDecoder;
import bgu.spl.net.api.MessagingProtocol;
import bgu.spl.net.srv.BaseServer;
import bgu.spl.net.srv.BlockingConnectionHandler;

public class TftpServer extends BaseServer<byte[]>{

    public TftpServer(int port, Supplier<MessagingProtocol<byte[]>> protocolFactory,
            Supplier<MessageEncoderDecoder<byte[]>> encdecFactory) {
        super(port, protocolFactory, encdecFactory);
        //TODO Auto-generated constructor stub
    }

    @Override
    protected void execute(BlockingConnectionHandler<byte[]> handler) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'execute'");
    }
    //TODO: Implement this
}
