package being.altiplano.example.avro;

import being.altiplano.example.avrorpc.*;
import net.moznion.random.string.RandomStringGenerator;
import org.apache.avro.AvroRemoteException;
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

/**
 * Created by gaoyuan on 11/03/2017.
 */
public class FunctionSyncTest {
    public static final int port = 9999;
    @Rule
    public ErrorCollector collector = new ErrorCollector();

    @Rule
    public Timeout timeout = new Timeout(3_000);

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
                        if (paramTypes[0].equals(Asker.class)) {
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

        protected void checkLowerCast(Asker client) throws AvroRemoteException {
            String contentReference = generator.generateByRegex(stringRegex);
            checkLowerCast(client, contentReference);
        }

        protected void checkLowerCast(Asker client, String contentReference) throws AvroRemoteException {
            LowerCastOut reply = client.lowercast(new LowerCastIn(contentReference));
            collector.checkThat(reply, CoreMatchers.notNullValue());
            collector.checkThat(reply.getReply().toString(), CoreMatchers.equalTo(contentReference.toLowerCase()));
        }

        protected void checkUpperCast(Asker client) throws AvroRemoteException {
            String contentReference = generator.generateByRegex(stringRegex);
            checkUpperCast(client, contentReference);
        }

        protected void checkUpperCast(Asker client, String contentReference) throws AvroRemoteException {
            UpperCastOut reply = client.uppercast(new UpperCastIn(contentReference));
            collector.checkThat(reply, CoreMatchers.notNullValue());
            collector.checkThat(reply.getReply().toString(), CoreMatchers.equalTo(contentReference.toUpperCase()));
        }

        protected void checkReverse(Asker client) throws AvroRemoteException {
            String contentReference = generator.generateByRegex(stringRegex);
            checkReverse(client, contentReference);
        }

        protected void checkReverse(Asker client, String contentReference) throws AvroRemoteException {
            ReverseOut reply = client.reverse(new ReverseIn(contentReference));
            collector.checkThat(reply, CoreMatchers.notNullValue());
            String expect = new StringBuilder().append(contentReference).reverse().toString();
            collector.checkThat(reply.getReply().toString(), CoreMatchers.equalTo(expect));
        }

        protected void checkCount(Asker client) throws AvroRemoteException {
            String contentReference = generator.generateByRegex(stringRegex);
            checkCount(client, contentReference);
        }

        protected void checkCount(Asker client, String contentReference) throws AvroRemoteException {
            CountOut reply = client.count(new CountIn(contentReference));
            collector.checkThat(reply, CoreMatchers.notNullValue());
            collector.checkThat(reply.length, CoreMatchers.equalTo(contentReference.length()));
        }

        protected void checkEcho(Asker client) throws AvroRemoteException {
            String contentReference = generator.generateByRegex(stringRegex);
            int times = 1 + random.nextInt(5);
            checkEcho(client, contentReference, times);
        }

        protected void checkEcho(Asker client, String contentReference, int times) throws AvroRemoteException {
            EchoOut reply = client.echo(new EchoIn(contentReference, times));
            collector.checkThat(reply, CoreMatchers.notNullValue());
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < times; ++i) {
                sb.append(contentReference);
            }
            collector.checkThat(reply.getReply().toString(), CoreMatchers.equalTo(sb.toString()));
        }

        protected void checkRandom(Asker client, int times) throws AvroRemoteException {
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
    public void testSyncFunctions() throws IOException {
        Server server = null;

        Helper helper = new Helper(collector);

        try {
            server = new NettyServer(
                    new SpecificResponder(Asker.class, new AskerImpl()), new InetSocketAddress(port));

            NettyTransceiver client = new NettyTransceiver(new InetSocketAddress(port));
            Asker poxy = SpecificRequestor.getClient(Asker.class, client);

            helper.checkEcho(poxy);
            helper.checkCount(poxy);
            helper.checkReverse(poxy);
            helper.checkUpperCast(poxy);
            helper.checkLowerCast(poxy);

            helper.checkRandom(poxy, 5 + helper.random.nextInt(10));

        } finally {
            if (server != null) {
                server.close();
            }
        }
    }
}
