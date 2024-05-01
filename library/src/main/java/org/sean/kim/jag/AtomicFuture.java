package org.sean.kim.jag;

import android.os.Handler;
import android.util.Pair;

import java.util.ArrayList;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

public class AtomicFuture<Result> implements Future<Result> {
    private final CountDownLatch latch = new CountDownLatch(1);
    private final ArrayList<Pair<Handler, Consumer<Result>>> completes = new ArrayList<>();
    private Result value;
    private boolean bCanceld = false;

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        bCanceld  = true;
        boolean result = isDone();
        if (mayInterruptIfRunning && latch.getCount() > 0) {
            latch.countDown();
        }
        return result;
    }

    @Override
    public boolean isCancelled() {
        return bCanceld;
    }

    @Override
    public boolean isDone() {
        return latch.getCount() == 0;
    }

    @Override
    public Result get() throws InterruptedException {
        if (bCanceld) {
            throw new CancellationException();
        }
        latch.await();
        if (bCanceld)
            throw new InterruptedException();
        return value;
    }

    @Override
    public Result get(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
        if (bCanceld) {
            throw new CancellationException();
        }
        if (latch.await(timeout, unit)) {
            if (bCanceld)
                throw new InterruptedException();
            return value;
        } else {
            throw new TimeoutException();
        }
    }

    public void set(Result result) {
        if (bCanceld) {
            //Log.e("This Future is Canceld");
            return;
        }
        if (latch.getCount() == 0) {
            throw new RuntimeException("Atomic Future must be put only once!!!");
        }
        value = result;
        for (Pair<Handler, Consumer<Result>> consumer : completes) {
            consumer.first.post(()->consumer.second.accept(result));
        }
        latch.countDown();
    }

    public void addConsumer(Handler callbackHandler, Consumer<Result> completeConsumer) {
        completes.add(Pair.create(callbackHandler, completeConsumer));
    }
}
