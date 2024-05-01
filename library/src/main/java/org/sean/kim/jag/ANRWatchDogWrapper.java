package org.sean.kim.jag;

import com.github.anrwatchdog.ANRWatchDog;

public class ANRWatchDogWrapper implements WatchDog {
    private final ANRWatchDog anrWatchDog;

    public ANRWatchDogWrapper(ANRWatchDog anrWatchDog) {
        this.anrWatchDog = anrWatchDog;
    }

    @Override
    public Thread.State getState() {
        return anrWatchDog.getState();
    }

    @Override
    public void start() {
        anrWatchDog.start();
    }

    @Override
    public void setANRInterceptor(JagAnrInterceptor jagAnrInterceptor) {
        anrWatchDog.setANRInterceptor(jagAnrInterceptor);
    }
}
