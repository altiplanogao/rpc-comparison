package being.altiplano.example.thrift;

import being.altiplano.example.thriftrpc.Asker;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.server.THsHaServer;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TSimpleServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.apache.thrift.transport.TNonblockingServerTransport;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by gaoyuan on 10/03/2017.
 */
public class AskerServer {

    private final int port;
    private final AtomicBoolean started = new AtomicBoolean(false);

    private TServer server;
    private final boolean blocking;
    private volatile CountDownLatch start;
    private volatile CountDownLatch stop;

    public AskerServer(int port, boolean blocking) {
        this.port = port;
        this.blocking = blocking;
    }

    public void start() {
        if (started.compareAndSet(false, true)) {
            start = new CountDownLatch(1);
            stop = new CountDownLatch(1);
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        if (blocking) {
                            blockMode();
                        } else {
                            nonBlockMode();
                        }
                    } finally {
                        stop.countDown();
                    }
                }
            };
            new Thread(runnable).start();
        }
        try {
            start.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        if (started.compareAndSet(true, false)) {
            server.stop();
            server = null;
        }
        try {
            stop.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void blockMode() {
        AskerHandler handler = new AskerHandler();
        Asker.Processor processor = new Asker.Processor(handler);
        try {
            boolean useThreadPool = true;
            if (useThreadPool) {
                TServerTransport serverTransport = new TServerSocket(port);
                TThreadPoolServer.Args arg = new TThreadPoolServer.Args(serverTransport);
                arg.protocolFactory(new TCompactProtocol.Factory());
//                arg.processorFactory(new TProcessorFactory(processor));
                arg.processor(processor);
                server = new TThreadPoolServer(arg);
            } else {
                TServerTransport serverTransport = new TServerSocket(port);
                TSimpleServer.Args arg = new TSimpleServer.Args(serverTransport);
                arg.protocolFactory(new TCompactProtocol.Factory());
                arg.processor(processor);
                server = new TSimpleServer(arg);
            }
            start.countDown();


            System.out.println("Starting the blockMode server...");
            server.serve();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void nonBlockMode() {
        AskerHandler handler = new AskerHandler();
        Asker.Processor processor = new Asker.Processor(handler);
        try {
            TNonblockingServerTransport transport = new TNonblockingServerSocket(port);
            THsHaServer.Args arg = new THsHaServer.Args(transport);
            arg.protocolFactory(new TCompactProtocol.Factory());
//            arg.transportFactory(new TFramedTransport.Factory());
//            arg.processorFactory(new TProcessorFactory(processor));
            arg.processor(processor);
            server = new THsHaServer(arg);
            start.countDown();

            System.out.println("Starting the nonBlock server...");
            server.serve();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
