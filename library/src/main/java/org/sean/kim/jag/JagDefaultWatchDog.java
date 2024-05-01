package org.sean.kim.jag;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;

public class JagDefaultWatchDog implements WatchDog {
    private final int watchInterval;
    private final Handler handler;
    private Thread.State state = Thread.State.NEW;
    private Handler _uiHandler = new Handler(Looper.getMainLooper());
    private JagAnrInterceptor anrInterceptor;

    public JagDefaultWatchDog(int watchInterval) {
        this.watchInterval = watchInterval;
        HandlerThread handlerThread = new HandlerThread("JagDefaultWatchDog");
        handlerThread.start();
        this.handler = new AnrCheckHandler(handlerThread.getLooper());
    }

    public JagDefaultWatchDog(int watchInterval, Looper looper) {
        assert looper != Looper.getMainLooper();
        this.watchInterval = watchInterval;
        this.handler = new AnrCheckHandler(looper);
    }

    @Override
    public Thread.State getState() {
        return state;
    }

    @Override
    public void start() {
        state = Thread.State.RUNNABLE;
        handler.sendEmptyMessage(AnrCheckHandler.WHAT_START);
    }

    @Override
    public void setANRInterceptor(JagAnrInterceptor jagAnrInterceptor) {
        anrInterceptor = jagAnrInterceptor;
    }

    private class AnrCheckHandler extends Handler {
        public static final int WHAT_START = 0;
        public static final int WHAT_INTERVAL = 1;
        private volatile long _tick;
        private long interval;

        public AnrCheckHandler(Looper looper) {
            super(looper);
            interval = watchInterval;
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case WHAT_START:
                    boolean needPost = _tick == 0;
                    _tick = interval;
                    if (needPost) {
                        _uiHandler.post(this::_ticker);
                    }
                    sendEmptyMessageDelayed(WHAT_INTERVAL, interval);
                    break;
                case WHAT_INTERVAL:
                    if (_tick != 0) {
                        if (anrInterceptor != null) {
                            interval = anrInterceptor.intercept(_tick);
                            if (interval > 0) {
                                sendEmptyMessage(WHAT_START);
                                break;
                            }
                        }

                        throw JagAnrError.NewMainOnly(_tick);
                    } else {
                        interval = watchInterval;
                    }
                    sendEmptyMessage(WHAT_START);
                    break;
            }
            super.handleMessage(msg);
        }

        private void _ticker() {
            _tick = 0;
        }
    }
}
