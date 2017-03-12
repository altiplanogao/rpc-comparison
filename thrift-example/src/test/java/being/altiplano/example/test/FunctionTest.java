package being.altiplano.example.test;

import being.altiplano.example.thrift.AskerServer;
import being.altiplano.example.thrift.AsyncMethodCallbackFuture;
import being.altiplano.example.thriftmessage.*;
import being.altiplano.example.thriftrpc.Asker;
import net.moznion.random.string.RandomStringGenerator;
import org.apache.thrift.TException;
import org.apache.thrift.async.TAsyncClientManager;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.*;
import org.hamcrest.CoreMatchers;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.junit.rules.Timeout;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Created by gaoyuan on 10/03/2017.
 */
public class FunctionTest {
    private final static int port = 9998;
    @Rule
    public ErrorCollector collector = new ErrorCollector();

    @Rule
    public Timeout timeout = new Timeout(3_000);

    static final RandomStringGenerator generator = new RandomStringGenerator();
    static final String stringRegex = "\\w+\\d*\\s[0-9]{0,3}X";
    static final Random random = new Random();

    static class Helper {
        static final Method[] checkClientMethods;
        static final Method[] checkAsyncClientMethods;
        public final ErrorCollector collector;

        public Helper(ErrorCollector collector) {
            this.collector = collector;
        }

        static {
            List<Method> cm = new ArrayList<>();
            List<Method> acm = new ArrayList<>();
            Method[] allMethods = Helper.class.getDeclaredMethods();
            for (Method m : allMethods) {
                if (m.getName().startsWith("check")) {
                    if (m.getName().contains("Start"))
                        continue;
                    if (m.getName().contains("Stop"))
                        continue;
                    Class<?>[] paramTypes = m.getParameterTypes();
                    if (paramTypes.length == 1) {
                        if (paramTypes[0].equals(Asker.Client.class)) {
                            cm.add(m);
                        } else if (paramTypes[0].equals(Asker.AsyncClient.class)) {
                            acm.add(m);
                        }
                    }
                }
            }
            checkClientMethods = cm.toArray(new Method[cm.size()]);
            checkAsyncClientMethods = acm.toArray(new Method[cm.size()]);
        }

        protected void checkLowerCast(Asker.Client client) throws TException {
            String contentReference = generator.generateByRegex(stringRegex);
            checkLowerCast(client, contentReference);
        }

        protected void checkLowerCast(Asker.Client client, String contentReference) throws TException {
            LowerCastOut reply = client.lowercast(new LowerCastIn(contentReference));
            collector.checkThat(reply, CoreMatchers.notNullValue());
            collector.checkThat(reply.reply, CoreMatchers.equalTo(contentReference.toLowerCase()));
        }

        protected void checkUpperCast(Asker.Client client) throws TException {
            String contentReference = generator.generateByRegex(stringRegex);
            checkUpperCast(client, contentReference);
        }

        protected void checkUpperCast(Asker.Client client, String contentReference) throws TException {
            UpperCastOut reply = client.uppercast(new UpperCastIn(contentReference));
            collector.checkThat(reply, CoreMatchers.notNullValue());
            collector.checkThat(reply.reply, CoreMatchers.equalTo(contentReference.toUpperCase()));
        }

        protected void checkReverse(Asker.Client client) throws TException {
            String contentReference = generator.generateByRegex(stringRegex);
            checkReverse(client, contentReference);
        }

        protected void checkReverse(Asker.Client client, String contentReference) throws TException {
            ReverseOut reply = client.reverse(new ReverseIn(contentReference));
            collector.checkThat(reply, CoreMatchers.notNullValue());
            String expect = new StringBuilder().append(contentReference).reverse().toString();
            collector.checkThat(reply.reply, CoreMatchers.equalTo(expect));
        }

        protected void checkCount(Asker.Client client) throws TException {
            String contentReference = generator.generateByRegex(stringRegex);
            checkCount(client, contentReference);
        }

        protected void checkCount(Asker.Client client, String contentReference) throws TException {
            CountOut reply = client.count(new CountIn(contentReference));
            collector.checkThat(reply, CoreMatchers.notNullValue());
            collector.checkThat(reply.length, CoreMatchers.equalTo(contentReference.length()));
        }

        protected void checkEcho(Asker.Client client) throws TException {
            String contentReference = generator.generateByRegex(stringRegex);
            int times = 1 + random.nextInt(5);
            checkEcho(client, contentReference, times);
        }

        protected void checkEcho(Asker.Client client, String contentReference, int times) throws TException {
            EchoOut reply = client.echo(new EchoIn(contentReference, times));
            collector.checkThat(reply, CoreMatchers.notNullValue());
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < times; ++i) {
                sb.append(contentReference);
            }
            collector.checkThat(reply.reply, CoreMatchers.equalTo(sb.toString()));
        }

        protected void checkRandom(Asker.Client client, int times) throws TException {
            int ms = checkClientMethods.length;
            try {
                for (int i = 0; i < times; ++i) {
                    Method m = checkClientMethods[random.nextInt(ms)];
                    m.invoke(this, client);
                }
            } catch (IllegalAccessException | InvocationTargetException e) {
                collector.addError(e);
            }
        }


        protected void checkLowerCast(Asker.AsyncClient client) throws TException, ExecutionException, InterruptedException {
            String contentReference = generator.generateByRegex(stringRegex);
            checkLowerCast(client, contentReference);
        }

        protected void checkLowerCast(Asker.AsyncClient client, String contentReference) throws TException, ExecutionException, InterruptedException {
            AskerAsyncClientWrapper clientWrapper = new AskerAsyncClientWrapper(client);
            LowerCastOut reply = clientWrapper.lowercast(new LowerCastIn(contentReference)).get();
            collector.checkThat(reply, CoreMatchers.notNullValue());
            collector.checkThat(reply.reply, CoreMatchers.equalTo(contentReference.toLowerCase()));
        }

        protected void checkUpperCast(Asker.AsyncClient client) throws TException, ExecutionException, InterruptedException {
            String contentReference = generator.generateByRegex(stringRegex);
            checkUpperCast(client, contentReference);
        }

        protected void checkUpperCast(Asker.AsyncClient client, String contentReference) throws TException, ExecutionException, InterruptedException {
            AskerAsyncClientWrapper clientWrapper = new AskerAsyncClientWrapper(client);
            UpperCastOut reply = clientWrapper.uppercast(new UpperCastIn(contentReference)).get();
            collector.checkThat(reply, CoreMatchers.notNullValue());
            collector.checkThat(reply.reply, CoreMatchers.equalTo(contentReference.toUpperCase()));
        }

        protected void checkReverse(Asker.AsyncClient client) throws TException, ExecutionException, InterruptedException {
            String contentReference = generator.generateByRegex(stringRegex);
            checkReverse(client, contentReference);
        }

        protected void checkReverse(Asker.AsyncClient client, String contentReference) throws TException, ExecutionException, InterruptedException {
            AskerAsyncClientWrapper clientWrapper = new AskerAsyncClientWrapper(client);
            ReverseOut reply = clientWrapper.reverse(new ReverseIn(contentReference)).get();
            collector.checkThat(reply, CoreMatchers.notNullValue());
            String expect = new StringBuilder().append(contentReference).reverse().toString();
            collector.checkThat(reply.reply, CoreMatchers.equalTo(expect));
        }

        protected void checkCount(Asker.AsyncClient client) throws TException, ExecutionException, InterruptedException {
            String contentReference = generator.generateByRegex(stringRegex);
            checkCount(client, contentReference);
        }

        protected void checkCount(Asker.AsyncClient client, String contentReference) throws TException, ExecutionException, InterruptedException {
            AskerAsyncClientWrapper clientWrapper = new AskerAsyncClientWrapper(client);
            CountOut reply = clientWrapper.count(new CountIn(contentReference)).get();
            collector.checkThat(reply, CoreMatchers.notNullValue());
            collector.checkThat(reply.length, CoreMatchers.equalTo(contentReference.length()));
        }

        protected void checkEcho(Asker.AsyncClient client) throws TException, ExecutionException, InterruptedException {
            String contentReference = generator.generateByRegex(stringRegex);
            int times = 1 + random.nextInt(5);
            checkEcho(client, contentReference, times);
        }

        protected void checkEcho(Asker.AsyncClient client, String contentReference, int times) throws TException, ExecutionException, InterruptedException {
            AskerAsyncClientWrapper clientWrapper = new AskerAsyncClientWrapper(client);
            EchoOut reply = clientWrapper.echo(new EchoIn(contentReference, times)).get();
            collector.checkThat(reply, CoreMatchers.notNullValue());
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < times; ++i) {
                sb.append(contentReference);
            }
            collector.checkThat(reply.reply, CoreMatchers.equalTo(sb.toString()));
        }

        protected void checkRandom(Asker.AsyncClient client, int times) throws TException {
            int ms = checkAsyncClientMethods.length;
            try {
                for (int i = 0; i < times; ++i) {
                    Method m = checkAsyncClientMethods[random.nextInt(ms)];
                    m.invoke(this, client);
                }
            } catch (IllegalAccessException | InvocationTargetException e) {
                collector.addError(e);
            }
        }
    }

    private static class AskerAsyncClientWrapper {
        private final Asker.AsyncClient asyncClient;

        public AskerAsyncClientWrapper(Asker.AsyncClient asyncClient) {
            this.asyncClient = asyncClient;
        }

        Future<EchoOut> echo(EchoIn in) throws TException {
            AsyncMethodCallbackFuture<EchoOut> future = new AsyncMethodCallbackFuture<>();
            asyncClient.echo(in, future);
            return future;
        }

        Future<CountOut> count(CountIn in) throws TException {
            AsyncMethodCallbackFuture<CountOut> future = new AsyncMethodCallbackFuture<>();
            asyncClient.count(in, future);
            return future;
        }

        Future<ReverseOut> reverse(ReverseIn in) throws TException {
            AsyncMethodCallbackFuture<ReverseOut> future = new AsyncMethodCallbackFuture<>();
            asyncClient.reverse(in, future);
            return future;
        }

        Future<UpperCastOut> uppercast(UpperCastIn in) throws TException {
            AsyncMethodCallbackFuture<UpperCastOut> future = new AsyncMethodCallbackFuture<>();
            asyncClient.uppercast(in, future);
            return future;
        }

        Future<LowerCastOut> lowercast(LowerCastIn in) throws TException {
            AsyncMethodCallbackFuture<LowerCastOut> future = new AsyncMethodCallbackFuture<>();
            asyncClient.lowercast(in, future);
            return future;
        }
    }

    public static TTransport transport(int port) {
        return new TSocket("localhost", port);
    }

    @Test
    public void testBlockSync() throws TException {
        AskerServer server = new AskerServer(port, true);
        server.start();

        try (TTransport transport = transport(port)) {
            transport.open();

            TProtocol protocol = new TCompactProtocol(transport);
            final Asker.Client client = new Asker.Client(protocol);

            Helper helper = new Helper(collector);

            helper.checkEcho(client);
            helper.checkCount(client);
            helper.checkReverse(client);
            helper.checkUpperCast(client);
            helper.checkLowerCast(client);

            helper.checkRandom(client, 5 + random.nextInt(10));
        }
        server.stop();
    }

    @Test
    public void testNonBlockSync() throws TException {
        AskerServer server = new AskerServer(port, false);
        server.start();

        try (TTransport transport = new TFramedTransport(transport(port))) {
            transport.open();

            TProtocol protocol = new TCompactProtocol(transport);
            final Asker.Client client = new Asker.Client(protocol);

            Helper helper = new Helper(collector);

            helper.checkEcho(client);
            helper.checkCount(client);
            helper.checkReverse(client);
            helper.checkUpperCast(client);
            helper.checkLowerCast(client);

            helper.checkRandom(client, 5 + random.nextInt(10));
        }
        server.stop();
    }

    @Test
    @Ignore
    public void testAsync() throws TException, IOException, ExecutionException, InterruptedException {
        AskerServer server = new AskerServer(port, false);
        server.start();

        try (SocketChannel socketChannel = SocketChannel.open()) {
            socketChannel.connect(new InetSocketAddress("localhost", port));
            TNonblockingTransport transport = new TNonblockingSocket(socketChannel);

            final Asker.AsyncClient client = new Asker.AsyncClient(
                    new TCompactProtocol.Factory(),
                    new TAsyncClientManager(), transport);

            Helper helper = new Helper(collector);

            helper.checkEcho(client);
            helper.checkCount(client);
            helper.checkReverse(client);
            helper.checkUpperCast(client);
            helper.checkLowerCast(client);

            helper.checkRandom(client, 5 + random.nextInt(10));
        }
        server.stop();
    }

}
