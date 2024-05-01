package org.sean.kim.jag;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import org.sean.kim.jag.util.Logger;

import java.util.concurrent.CancellationException;
import java.util.function.Consumer;

public class JagResult<T> implements Cancellable {
    private final Logger logger = new Logger(C.TAG, "JagResult");
    private final JagJob<T> job;
    private final Handler callbackHandler;
    private final JagAnrInterceptor jagAnrInterceptor;
    private final AtomicFuture<T> future;
    private final WorkThread workThread;
    private final boolean keepGoingJob;

    public JagResult(@NonNull JagJob<T> job, Handler callbackHandler, JagAnrInterceptor jagAnrInterceptor, boolean keepGoingJob) {
        this.job = job;
        this.callbackHandler = callbackHandler;
        this.jagAnrInterceptor = jagAnrInterceptor;
        this.keepGoingJob = keepGoingJob;
        future = new AtomicFuture<>();
        workThread = new WorkThread(job, future);
        workThread.start();
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
        if (!keepGoingJob && workThread.isAlive()) {
            if (logger.isEnableV()) logger.v("workThread.interrupt()");
            workThread.interrupt();
        }
        future.cancel(true);
    }

    private class WorkThread extends Thread {
        private final JagJob<T> job;
        private final AtomicFuture<T> future;

        public WorkThread(JagJob<T> job, AtomicFuture<T> future) {
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
