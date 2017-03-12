package being.altiplano.example.proto;

import being.altiplano.example.protomsgs.Msgs;
import being.altiplano.example.protomsgs.Msgs.*;
import being.altiplano.example.protorpc.AskerGrpc;
import io.grpc.stub.StreamObserver;

/**
 * Created by gaoyuan on 11/03/2017.
 */
public class AskerService extends AskerGrpc.AskerImplBase {
    @Override
    public void echo(Msgs.EchoIn request, StreamObserver<Msgs.EchoOut> responseObserver) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < request.getTimes(); ++i) {
            sb.append(request.getContent());
        }
        EchoOut out = EchoOut.newBuilder().setReply(sb.toString()).build();
        responseObserver.onNext(out);
        responseObserver.onCompleted();
    }

    @Override
    public void count(Msgs.CountIn request, StreamObserver<Msgs.CountOut> responseObserver) {
        CountOut out = CountOut.newBuilder().setLength(request.getContent().length()).build();
        responseObserver.onNext(out);
        responseObserver.onCompleted();
    }

    @Override
    public void reverse(Msgs.ReverseIn request, StreamObserver<Msgs.ReverseOut> responseObserver) {
        StringBuilder sb = new StringBuilder(request.getContent());
        ReverseOut out = ReverseOut.newBuilder().setReply(sb.reverse().toString()).build();
        responseObserver.onNext(out);
        responseObserver.onCompleted();
    }

    @Override
    public void upperCast(Msgs.UpperCastIn request, StreamObserver<Msgs.UpperCastOut> responseObserver) {
        UpperCastOut out = UpperCastOut.newBuilder().setReply(request.getContent().toUpperCase()).build();
        responseObserver.onNext(out);
        responseObserver.onCompleted();
    }

    @Override
    public void lowerCast(Msgs.LowerCastIn request, StreamObserver<Msgs.LowerCastOut> responseObserver) {
        LowerCastOut out = LowerCastOut.newBuilder().setReply(request.getContent().toLowerCase()).build();
        responseObserver.onNext(out);
        responseObserver.onCompleted();
    }
}
