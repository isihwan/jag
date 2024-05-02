package org.sean.kim.jag;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.sean.kim.jag.util.Logger;

import java.util.concurrent.CancellationException;
import java.util.function.Consumer;

public class JagResult<T> implements Cancellable {
    private final Logger logger = new Logger(C.TAG, "JagResult");
    private final JagJob<T> job;
    private final Handler callbackHandler;
    private final JagAnrInterceptor jagAnrInterceptor;
    private final AtomicFuture<T> future;
    @Nullable private final Thread workThread;

    public JagResult(@NonNull JagJob<T> job, Handler callbackHandler, JagAnrInterceptor jagAnrInterceptor, @Nullable Handler workerHandler) {
        this.job = job;
        this.callbackHandler = callbackHandler;
        this.jagAnrInterceptor = jagAnrInterceptor;
        future = new AtomicFuture<>();
        Worker worker = new Worker(job, future);
        if (workerHandler != null) {
            workerHandler.post(worker);
            workThread = null;
        } else {
            workThread = new Thread(worker);
            workThread.start();
        }
    }

    public @NonNull T get(@NonNull T defaultValue){
        verifyMainThread();
        jagAnrInterceptor.addGetter(this);
        try {
            T value = future.get();
            return value;
        } catch (CancellationException | InterruptedException e) {
            onException(e);
            return defaultValue;
        }
    }
    public void onComplete(@NonNull Consumer<T> consumer) {
        future.addConsumer(callbackHandler, consumer);
    }

    public void onException(Exception e) {}

    private void verifyMainThread() {
        if (Thread.currentThread() != Looper.getMainLooper().getThread()) {
            throw new IllegalStateException("JAG is accessed on the wrong thread. Should not be on the main thread");
        }
    }

    @Override
    public void cancel() {
        logger.w("cancel");
        if (workThread != null && workThread.isAlive()) {
            if (logger.isEnableV()) logger.v("workThread.interrupt()");
            workThread.interrupt();
        }
        future.cancel(true);
    }

    private class Worker implements Runnable {
        private final JagJob<T> job;
        private final AtomicFuture<T> future;

        public Worker(JagJob<T> job, AtomicFuture<T> future) {
            this.job = job;
            this.future = future;
        }

        @Override
        public void run() {
            try {
                T result = job.work();
                future.set(result);
            } catch (Exception e) {
                if (!future.isCancelled())
                    future.cancel(true);
            }
        }
    }
}
