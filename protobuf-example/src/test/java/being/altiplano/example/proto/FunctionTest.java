package being.altiplano.example.proto;

import being.altiplano.example.protomsgs.Msgs;
import being.altiplano.example.protomsgs.Msgs.*;
import being.altiplano.example.protorpc.AskerGrpc;
import being.altiplano.example.protorpc.AskerGrpc.AskerBlockingStub;
import being.altiplano.example.protorpc.AskerGrpc.AskerStub;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import net.moznion.random.string.RandomStringGenerator;
import org.hamcrest.CoreMatchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.junit.rules.Timeout;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Created by gaoyuan on 12/03/2017.
 */
public class FunctionTest {

    public static final int port = 9999;
    static final RandomStringGenerator generator = new RandomStringGenerator();
    static final String stringRegex = "\\w+\\d*\\s[0-9]{0,3}X";
    static final Random random = new Random();

    @Rule
    public ErrorCollector collector = new ErrorCollector();

    @Rule
    public Timeout timeout = new Timeout(3_000);

    public static class SyncHelper {

        static final Method[] checkClientMethods;
        private final ErrorCollector collector;

        static {
            List<Method> cm = new ArrayList<>();
            Method[] allMethods = SyncHelper.class.getDeclaredMethods();
            for (Method m : allMethods) {
                if (m.getName().startsWith("check")) {
                    if (m.getName().contains("Start"))
                        continue;
                    if (m.getName().contains("Stop"))
                        continue;
                    Class<?>[] paramTypes = m.getParameterTypes();
                    if (paramTypes.length == 1) {
                        if (paramTypes[0].equals(AskerBlockingStub.class)) {
                            cm.add(m);
                        }
                    }
                }
            }
            checkClientMethods = cm.toArray(new Method[cm.size()]);
        }

        public SyncHelper(ErrorCollector collector) {
            this.collector = collector;
        }

        protected void checkLowerCast(AskerBlockingStub client) {
            String contentReference = generator.generateByRegex(stringRegex);
            checkLowerCast(client, contentReference);
        }

        protected void checkLowerCast(AskerBlockingStub client, String contentReference) {
            Msgs.LowerCastOut reply = client.lowerCast(LowerCastIn.newBuilder().setContent(contentReference).build());
            collector.checkThat(reply, CoreMatchers.notNullValue());
            collector.checkThat(reply.getReply().toString(), CoreMatchers.equalTo(contentReference.toLowerCase()));
        }

        protected void checkUpperCast(AskerBlockingStub client) {
            String contentReference = generator.generateByRegex(stringRegex);
            checkUpperCast(client, contentReference);
        }

        protected void checkUpperCast(AskerBlockingStub client, String contentReference) {
            UpperCastOut reply = client.upperCast(UpperCastIn.newBuilder().setContent(contentReference).build());
            collector.checkThat(reply, CoreMatchers.notNullValue());
            collector.checkThat(reply.getReply().toString(), CoreMatchers.equalTo(contentReference.toUpperCase()));
        }

        protected void checkReverse(AskerBlockingStub client) {
            String contentReference = generator.generateByRegex(stringRegex);
            checkReverse(client, contentReference);
        }

        protected void checkReverse(AskerBlockingStub client, String contentReference) {
            ReverseOut reply = client.reverse(ReverseIn.newBuilder().setContent(contentReference).build());
            collector.checkThat(reply, CoreMatchers.notNullValue());
            String expect = new StringBuilder().append(contentReference).reverse().toString();
            collector.checkThat(reply.getReply().toString(), CoreMatchers.equalTo(expect));
        }

        protected void checkCount(AskerBlockingStub client) {
            String contentReference = generator.generateByRegex(stringRegex);
            checkCount(client, contentReference);
        }

        protected void checkCount(AskerBlockingStub client, String contentReference) {
            CountOut reply = client.count(CountIn.newBuilder().setContent(contentReference).build());
            collector.checkThat(reply, CoreMatchers.notNullValue());
            collector.checkThat(reply.getLength(), CoreMatchers.equalTo(contentReference.length()));
        }

        protected void checkEcho(AskerBlockingStub client) {
            String contentReference = generator.generateByRegex(stringRegex);
            int times = 1 + random.nextInt(5);
            checkEcho(client, contentReference, times);
        }

        protected void checkEcho(AskerBlockingStub client, String contentReference, int times) {
            EchoOut reply = client.echo(EchoIn.newBuilder().setContent(contentReference).setTimes(times).build());
            collector.checkThat(reply, CoreMatchers.notNullValue());
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < times; ++i) {
                sb.append(contentReference);
            }
            collector.checkThat(reply.getReply().toString(), CoreMatchers.equalTo(sb.toString()));
        }

        protected void checkRandom(AskerBlockingStub client, int times) {
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

    public static class AsyncCallFuture<V> extends CompletableFuture<V> implements StreamObserver<V> {
        private V value;

        @Override
        public void onNext(V v) {
            value = v;
        }

        @Override
        public void onError(Throwable throwable) {
            super.completeExceptionally(throwable);
        }

        @Override
        public void onCompleted() {
            super.complete(value);
        }
    }

    public static class AskerCallableWrapper {
        private final AskerStub callback;

        public AskerCallableWrapper(AskerStub callback) {
            this.callback = callback;
        }

        Future<EchoOut> echo(EchoIn in) throws IOException {
            AsyncCallFuture<EchoOut> future = new AsyncCallFuture<>();
            callback.echo(in, future);
            return future;
        }

        Future<CountOut> count(CountIn in) throws IOException {
            AsyncCallFuture<CountOut> future = new AsyncCallFuture<>();
            callback.count(in, future);
            return future;
        }

        Future<ReverseOut> reverse(ReverseIn in) throws IOException {
            AsyncCallFuture<ReverseOut> future = new AsyncCallFuture<>();
            callback.reverse(in, future);
            return future;
        }

        Future<UpperCastOut> uppercast(UpperCastIn in) throws IOException {
            AsyncCallFuture<UpperCastOut> future = new AsyncCallFuture<>();
            callback.upperCast(in, future);
            return future;
        }

        Future<LowerCastOut> lowercast(LowerCastIn in) throws IOException {
            AsyncCallFuture<LowerCastOut> future = new AsyncCallFuture<>();
            callback.lowerCast(in, future);
            return future;
        }

    }

    public static class AsyncHelper {

        static final Method[] checkClientMethods;
        private final ErrorCollector collector;

        static {
            List<Method> cm = new ArrayList<>();
            Method[] allMethods = AsyncHelper.class.getDeclaredMethods();
            for (Method m : allMethods) {
                if (m.getName().startsWith("check")) {
                    if (m.getName().contains("Start"))
                        continue;
                    if (m.getName().contains("Stop"))
                        continue;
                    Class<?>[] paramTypes = m.getParameterTypes();
                    if (paramTypes.length == 1) {
                        if (paramTypes[0].equals(AskerStub.class)) {
                            cm.add(m);
                        }
                    }
                }
            }
            checkClientMethods = cm.toArray(new Method[cm.size()]);
        }

        public AsyncHelper(ErrorCollector collector) {
            this.collector = collector;
        }

        protected void checkLowerCast(AskerStub client) throws InterruptedException, ExecutionException, IOException {
            String contentReference = generator.generateByRegex(stringRegex);
            checkLowerCast(client, contentReference);
        }

        protected void checkLowerCast(AskerStub client, String contentReference) throws IOException, ExecutionException, InterruptedException {
            Msgs.LowerCastOut reply = new AskerCallableWrapper(client).lowercast(LowerCastIn.newBuilder().setContent(contentReference).build()).get();
            collector.checkThat(reply, CoreMatchers.notNullValue());
            collector.checkThat(reply.getReply().toString(), CoreMatchers.equalTo(contentReference.toLowerCase()));
        }

        protected void checkUpperCast(AskerStub client) throws InterruptedException, ExecutionException, IOException {
            String contentReference = generator.generateByRegex(stringRegex);
            checkUpperCast(client, contentReference);
        }

        protected void checkUpperCast(AskerStub client, String contentReference) throws IOException, ExecutionException, InterruptedException {
            UpperCastOut reply = new AskerCallableWrapper(client).uppercast(UpperCastIn.newBuilder().setContent(contentReference).build()).get();
            collector.checkThat(reply, CoreMatchers.notNullValue());
            collector.checkThat(reply.getReply().toString(), CoreMatchers.equalTo(contentReference.toUpperCase()));
        }

        protected void checkReverse(AskerStub client) throws InterruptedException, ExecutionException, IOException {
            String contentReference = generator.generateByRegex(stringRegex);
            checkReverse(client, contentReference);
        }

        protected void checkReverse(AskerStub client, String contentReference) throws IOException, ExecutionException, InterruptedException {
            ReverseOut reply = new AskerCallableWrapper(client).reverse(ReverseIn.newBuilder().setContent(contentReference).build()).get();
            collector.checkThat(reply, CoreMatchers.notNullValue());
            String expect = new StringBuilder().append(contentReference).reverse().toString();
            collector.checkThat(reply.getReply().toString(), CoreMatchers.equalTo(expect));
        }

        protected void checkCount(AskerStub client) throws InterruptedException, ExecutionException, IOException {
            String contentReference = generator.generateByRegex(stringRegex);
            checkCount(client, contentReference);
        }

        protected void checkCount(AskerStub client, String contentReference) throws IOException, ExecutionException, InterruptedException {
            CountOut reply = new AskerCallableWrapper(client).count(CountIn.newBuilder().setContent(contentReference).build()).get();
            collector.checkThat(reply, CoreMatchers.notNullValue());
            collector.checkThat(reply.getLength(), CoreMatchers.equalTo(contentReference.length()));
        }

        protected void checkEcho(AskerStub client) throws InterruptedException, ExecutionException, IOException {
            String contentReference = generator.generateByRegex(stringRegex);
            int times = 1 + random.nextInt(5);
            checkEcho(client, contentReference, times);
        }

        protected void checkEcho(AskerStub client, String contentReference, int times) throws IOException, ExecutionException, InterruptedException {
            EchoOut reply = new AskerCallableWrapper(client).echo(EchoIn.newBuilder().setContent(contentReference).setTimes(times).build()).get();
            collector.checkThat(reply, CoreMatchers.notNullValue());
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < times; ++i) {
                sb.append(contentReference);
            }
            collector.checkThat(reply.getReply().toString(), CoreMatchers.equalTo(sb.toString()));
        }

        protected void checkRandom(AskerStub client, int times) {
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

    private static class AskerServer {
        Server server;

        public void start() throws IOException {
            ServerBuilder serverBuilder = ServerBuilder.forPort(port);
            server = serverBuilder.addService(new AskerService()).build();
            server.start();
        }

        public void stop() throws InterruptedException {
            server.shutdown();
            server.awaitTermination();
        }
    }

    private static class AskerClient {
        private final ManagedChannel channel;
        public final AskerBlockingStub blockingStub;
        public final AskerStub asyncStub;

        public AskerClient() {
            ManagedChannelBuilder channelBuilder = ManagedChannelBuilder.forAddress("localhost", port)
                    .usePlaintext(true);
            this.channel = channelBuilder.build();
            this.blockingStub = AskerGrpc.newBlockingStub(channel);
            this.asyncStub = AskerGrpc.newStub(channel);
        }
    }

    @Test
    public void testSyncFunctions() throws IOException, InterruptedException {
        ServerBuilder.forPort(port);
        AskerServer server = new AskerServer();

        try {
            server.start();

            AskerClient client = new AskerClient();
            AskerBlockingStub blockingStub = client.blockingStub;

            SyncHelper helper = new SyncHelper(collector);

            helper.checkEcho(blockingStub);
            helper.checkCount(blockingStub);
            helper.checkReverse(blockingStub);
            helper.checkUpperCast(blockingStub);
            helper.checkLowerCast(blockingStub);

            helper.checkRandom(blockingStub, 5 + random.nextInt(10));

        } finally {
            if (server != null) {
                server.stop();
            }
        }
    }

    @Test
    public void testAsyncFunctions() throws IOException, InterruptedException, ExecutionException {
        ServerBuilder.forPort(port);
        AskerServer server = new AskerServer();

        try {
            server.start();

            AskerClient client = new AskerClient();
            AskerStub stub = client.asyncStub;

            AsyncHelper helper = new AsyncHelper(collector);

            helper.checkEcho(stub);
            helper.checkCount(stub);
            helper.checkReverse(stub);
            helper.checkUpperCast(stub);
            helper.checkLowerCast(stub);

            helper.checkRandom(stub, 5 + random.nextInt(10));

        } finally {
            if (server != null) {
                server.stop();
            }
        }
    }
}
