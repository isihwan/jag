package org.sean.kim.jag;

import com.github.anrwatchdog.ANRWatchDog;

import java.util.ArrayList;

public class JagAnrInterceptor implements ANRWatchDog.ANRInterceptor {
    private ArrayList<Cancellable> waitLists = new ArrayList<>();

    @Override
    public long intercept(long duration) {
        for (Cancellable getter : waitLists) {
            getter.cancel();
        }
        if (waitLists.size() > 0) {
            waitLists.clear();
            return duration;
        }
        return 0;
    }

    public void addGetter(Cancellable getter) {
        waitLists.add(getter);
    }
}
