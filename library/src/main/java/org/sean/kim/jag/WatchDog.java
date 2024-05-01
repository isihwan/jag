package org.sean.kim.jag;

import com.github.anrwatchdog.ANRWatchDog;

public interface WatchDog {
    static WatchDog wrap(Object anrWatchDog) {
        return new ANRWatchDogWrapper((ANRWatchDog) anrWatchDog);
    }

    Thread.State getState();
    void start();

    void setANRInterceptor(JagAnrInterceptor jagAnrInterceptor);
}
