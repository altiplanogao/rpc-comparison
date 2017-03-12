package being.altiplano.example.avro;

import being.altiplano.example.avrorpc.*;
import org.apache.avro.AvroRemoteException;

/**
 * Created by gaoyuan on 11/03/2017.
 */
public class AskerImpl implements Asker {
    @Override
    public EchoOut echo(EchoIn message) throws AvroRemoteException {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < message.getTimes(); ++i) {
            sb.append(message.getContent());
        }
        return new EchoOut(sb.toString());
    }

    @Override
    public CountOut count(CountIn message) throws AvroRemoteException {
        return new CountOut(message.getContent().length());
    }

    @Override
    public ReverseOut reverse(ReverseIn message) throws AvroRemoteException {
        StringBuilder sb = new StringBuilder(message.getContent());
        return new ReverseOut(sb.reverse().toString());
    }

    @Override
    public UpperCastOut uppercast(UpperCastIn message) throws AvroRemoteException {
        return new UpperCastOut(message.getContent().toString().toUpperCase());
    }

    @Override
    public LowerCastOut lowercast(LowerCastIn message) throws AvroRemoteException {
        return new LowerCastOut(message.getContent().toString().toLowerCase());
    }
}
