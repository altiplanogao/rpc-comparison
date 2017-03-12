package being.altiplano.example.thrift;

import being.altiplano.example.thriftrpc.Asker;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TServer.Args;
import org.apache.thrift.server.TSimpleServer;
import org.apache.thrift.transport.TSSLTransportFactory;
import org.apache.thrift.transport.TServerTransport;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by gaoyuan on 10/03/2017.
 */
public class SecureAskerServer {

    private final int port;
    private final String keyStore;
    private final String keyPass;
    private final AtomicBoolean started = new AtomicBoolean(false);

    private Asker.Processor processor;
    private TServer server;

    public SecureAskerServer(String keyStore, String keyPass, int port) {
        this.keyStore = keyStore;
        this.keyPass = keyPass;
        this.port = port;
    }

    public void start() {
        if (started.compareAndSet(false, true)) {
            AskerHandler handler = new AskerHandler();
            processor = new Asker.Processor(handler);
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    secure(processor);
                }
            };
            new Thread(runnable).start();
        }
    }

    public void stop() {
        if (started.compareAndSet(true, false)) {
            server.stop();
            server = null;
            processor = null;
        }
    }

    private void secure(Asker.Processor processor) {
        try {
       /*
       * Use TSSLTransportParameters to setup the required SSL parameters. In this example
       * we are setting the keystore and the keystore password. Other things like algorithms,
       * cipher suites, client auth etc can be set.
       */
            TSSLTransportFactory.TSSLTransportParameters params = new TSSLTransportFactory.TSSLTransportParameters();
            // The Keystore contains the private key
            params.setKeyStore(keyStore, keyPass, null, null);

      /*
       * Use any of the TSSLTransportFactory to get a server transport with the appropriate
       * SSL configuration. You can use the default settings if properties are set in the command line.
       * Ex: -Djavax.net.ssl.keyStore=.keystore and -Djavax.net.ssl.keyStorePassword=thrift
       *
       * Note: You need not explicitly call open(). The underlying server socket is bound on return
       * from the factory class.
       */
            TServerTransport serverTransport = TSSLTransportFactory.getServerSocket(port, 0, null, params);
            TServer server = new TSimpleServer(new Args(serverTransport).processor(processor));

            // Use this for a multi threaded server
            // TServer server = new TThreadPoolServer(new TThreadPoolServer.Args(serverTransport).processor(processor));

            System.out.println("Starting the secure server...");
            server.serve();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
