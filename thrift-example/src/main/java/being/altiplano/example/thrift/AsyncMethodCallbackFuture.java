package being.altiplano.example.thrift;

import org.apache.thrift.async.AsyncMethodCallback;

import java.util.concurrent.CompletableFuture;

/**
 * Created by gaoyuan on 11/03/2017.
 */
public class AsyncMethodCallbackFuture<T> extends CompletableFuture<T> implements AsyncMethodCallback<T> {
    @Override
    public void onComplete(T o) {
        super.complete(o);
    }

    @Override
    public void onError(Exception e) {
        super.completeExceptionally(e);
    }
}
