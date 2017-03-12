package being.altiplano.example.thrift;

import being.altiplano.example.thriftmessage.*;
import being.altiplano.example.thriftrpc.Asker;
import org.apache.thrift.TException;

/**
 * Created by gaoyuan on 10/03/2017.
 */
public class AskerHandler implements Asker.Iface {
    @Override
    public EchoOut echo(EchoIn param) throws TException {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < param.getTimes(); ++i) {
            sb.append(param.getContent());
        }
        return new EchoOut(sb.toString());
    }

    @Override
    public CountOut count(CountIn param) throws TException {
        return new CountOut(param.getContent().length());
    }

    @Override
    public ReverseOut reverse(ReverseIn param) throws TException {
        StringBuilder sb = new StringBuilder(param.getContent());
        return new ReverseOut(sb.reverse().toString());
    }

    @Override
    public UpperCastOut uppercast(UpperCastIn param) throws TException {
        return new UpperCastOut(param.getContent().toUpperCase());
    }

    @Override
    public LowerCastOut lowercast(LowerCastIn param) throws TException {
        return new LowerCastOut(param.getContent().toLowerCase());
    }
}
