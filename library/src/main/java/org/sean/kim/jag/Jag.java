package org.sean.kim.jag;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.github.anrwatchdog.ANRWatchDog;

/**
 * Jag is a simple library for async without ANR in Android.
 * JAG is Just Async Getter.
 */
public final class Jag {

    private final WatchDog watchDog;
    private final Looper mainLooper;
    private final Handler mainHandler;
    private final JagAnrInterceptor jagAnrInterceptor;

    public Jag(@NonNull Context context) {
        this(context, new JagDefaultWatchDog(5000));
    }

    public Jag(@NonNull Context context, @NonNull Object watchDog) {
        this(context, WatchDog.wrap(watchDog));
    }

    public Jag(@NonNull Context context, @NonNull WatchDog watchDog) {
        this.watchDog = watchDog;
        mainLooper = context.getMainLooper();
        mainHandler = new Handler(mainLooper);
        if (watchDog.getState() == Thread.State.NEW) {
            watchDog.start();
        }
        jagAnrInterceptor = new JagAnrInterceptor();
        watchDog.setANRInterceptor(jagAnrInterceptor);
    }

    public void release() {
        mainHandler.removeCallbacksAndMessages(null);
    }

    public<T> @NonNull JagResult<T> request(@NonNull JagJob<T> job) {
        return request(job, null);
    }

    public<T> @NonNull JagResult<T> request(@NonNull JagJob<T> job, Handler workerHandler) {
        return new JagResult<>(job, mainHandler, jagAnrInterceptor, workerHandler);
    }
}
