package being.altiplano.example.avro;

import being.altiplano.example.avrorpc.*;
import net.moznion.random.string.RandomStringGenerator;
import org.apache.avro.ipc.Callback;
import org.apache.avro.ipc.NettyServer;
import org.apache.avro.ipc.NettyTransceiver;
import org.apache.avro.ipc.Server;
import org.apache.avro.ipc.specific.SpecificRequestor;
import org.apache.avro.ipc.specific.SpecificResponder;
import org.hamcrest.CoreMatchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.junit.rules.Timeout;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Created by gaoyuan on 11/03/2017.
 */
public class FunctionAsyncTest {
    public static final int port = 9999;
    @Rule
    public ErrorCollector collector = new ErrorCollector();

    @Rule
    public Timeout timeout = new Timeout(3_000);

    public static class AsyncCallbackFuture<T> extends CompletableFuture<T> implements Callback<T> {
        @Override
        public void handleResult(T t) {
            super.complete(t);
        }

        @Override
        public void handleError(Throwable throwable) {
            super.completeExceptionally(throwable);
        }
    }

    public static class AskerCallableWrapper {
        private final Asker.Callback callback;

        public AskerCallableWrapper(Asker.Callback callback) {
            this.callback = callback;
        }

        Future<EchoOut> echo(EchoIn in) throws IOException {
            AsyncCallbackFuture<EchoOut> future = new AsyncCallbackFuture<>();
            callback.echo(in, future);
            return future;
        }

        Future<CountOut> count(CountIn in) throws IOException {
            AsyncCallbackFuture<CountOut> future = new AsyncCallbackFuture<>();
            callback.count(in, future);
            return future;
        }

        Future<ReverseOut> reverse(ReverseIn in) throws IOException {
            AsyncCallbackFuture<ReverseOut> future = new AsyncCallbackFuture<>();
            callback.reverse(in, future);
            return future;
        }

        Future<UpperCastOut> uppercast(UpperCastIn in) throws IOException {
            AsyncCallbackFuture<UpperCastOut> future = new AsyncCallbackFuture<>();
            callback.uppercast(in, future);
            return future;
        }

        Future<LowerCastOut> lowercast(LowerCastIn in) throws IOException {
            AsyncCallbackFuture<LowerCastOut> future = new AsyncCallbackFuture<>();
            callback.lowercast(in, future);
            return future;
        }

    }


    public static class Helper {

        static final RandomStringGenerator generator = new RandomStringGenerator();
        static final String stringRegex = "\\w+\\d*\\s[0-9]{0,3}X";
        static final Method[] checkClientMethods;
        static final Random random = new Random();
        private final ErrorCollector collector;


        static {
            List<Method> cm = new ArrayList<>();
            Method[] allMethods = Helper.class.getDeclaredMethods();
            for (Method m : allMethods) {
                if (m.getName().startsWith("check")) {
                    if (m.getName().contains("Start"))
                        continue;
                    if (m.getName().contains("Stop"))
                        continue;
                    Class<?>[] paramTypes = m.getParameterTypes();
                    if (paramTypes.length == 1) {
                        if (paramTypes[0].equals(AskerCallableWrapper.class)) {
                            cm.add(m);
                        }
                    }
                }
            }
            checkClientMethods = cm.toArray(new Method[cm.size()]);
        }

        public Helper(ErrorCollector collector) {
            this.collector = collector;
        }

        protected void checkLowerCast(AskerCallableWrapper client) throws IOException, ExecutionException, InterruptedException {
            String contentReference = generator.generateByRegex(stringRegex);
            checkLowerCast(client, contentReference);
        }

        protected void checkLowerCast(AskerCallableWrapper client, String contentReference) throws IOException, ExecutionException, InterruptedException {
            AsyncCallbackFuture<LowerCastOut> f = new AsyncCallbackFuture<>();
            LowerCastOut reply = client.lowercast(new LowerCastIn(contentReference)).get();
            collector.checkThat(reply, CoreMatchers.notNullValue());
            collector.checkThat(reply.getReply().toString(), CoreMatchers.equalTo(contentReference.toLowerCase()));
        }

        protected void checkUpperCast(AskerCallableWrapper client) throws IOException, ExecutionException, InterruptedException {
            String contentReference = generator.generateByRegex(stringRegex);
            checkUpperCast(client, contentReference);
        }

        protected void checkUpperCast(AskerCallableWrapper client, String contentReference) throws IOException, ExecutionException, InterruptedException {
            UpperCastOut reply = client.uppercast(new UpperCastIn(contentReference)).get();
            collector.checkThat(reply, CoreMatchers.notNullValue());
            collector.checkThat(reply.getReply().toString(), CoreMatchers.equalTo(contentReference.toUpperCase()));
        }

        protected void checkReverse(AskerCallableWrapper client) throws IOException, ExecutionException, InterruptedException {
            String contentReference = generator.generateByRegex(stringRegex);
            checkReverse(client, contentReference);
        }

        protected void checkReverse(AskerCallableWrapper client, String contentReference) throws IOException, ExecutionException, InterruptedException {
            ReverseOut reply = client.reverse(new ReverseIn(contentReference)).get();
            collector.checkThat(reply, CoreMatchers.notNullValue());
            String expect = new StringBuilder().append(contentReference).reverse().toString();
            collector.checkThat(reply.getReply().toString(), CoreMatchers.equalTo(expect));
        }

        protected void checkCount(AskerCallableWrapper client) throws IOException, ExecutionException, InterruptedException {
            String contentReference = generator.generateByRegex(stringRegex);
            checkCount(client, contentReference);
        }

        protected void checkCount(AskerCallableWrapper client, String contentReference) throws IOException, ExecutionException, InterruptedException {
            CountOut reply = client.count(new CountIn(contentReference)).get();
            collector.checkThat(reply, CoreMatchers.notNullValue());
            collector.checkThat(reply.length, CoreMatchers.equalTo(contentReference.length()));
        }

        protected void checkEcho(AskerCallableWrapper client) throws IOException, ExecutionException, InterruptedException {
            String contentReference = generator.generateByRegex(stringRegex);
            int times = 1 + random.nextInt(5);
            checkEcho(client, contentReference, times);
        }

        protected void checkEcho(AskerCallableWrapper client, String contentReference, int times) throws IOException, ExecutionException, InterruptedException {
            EchoOut reply = client.echo(new EchoIn(contentReference, times)).get();
            collector.checkThat(reply, CoreMatchers.notNullValue());
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < times; ++i) {
                sb.append(contentReference);
            }
            collector.checkThat(reply.getReply().toString(), CoreMatchers.equalTo(sb.toString()));
        }

        protected void checkRandom(AskerCallableWrapper client, int times) throws IOException, ExecutionException, InterruptedException {
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
    }

    @Test
    public void testAsyncFunctions() throws IOException, ExecutionException, InterruptedException {
        Server server = null;
        Helper helper = new Helper(collector);

        try {
            server = new NettyServer(
                    new SpecificResponder(Asker.class, new AskerImpl()), new InetSocketAddress(port));

            NettyTransceiver client = new NettyTransceiver(new InetSocketAddress(port));
            Asker.Callback poxy = SpecificRequestor.getClient(Asker.Callback.class, client);

            AskerCallableWrapper callableWrapper = new AskerCallableWrapper(poxy);

            helper.checkEcho(callableWrapper);
            helper.checkCount(callableWrapper);
            helper.checkReverse(callableWrapper);
            helper.checkUpperCast(callableWrapper);
            helper.checkLowerCast(callableWrapper);

            helper.checkRandom(callableWrapper, 5 + helper.random.nextInt(10));

        } finally {
            if (server != null) {
                server.close();
            }
        }
    }
}
