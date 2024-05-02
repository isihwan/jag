package org.sean.kim.jag;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;

import org.sean.kim.jag.util.Logger;

public class JagDefaultWatchDog implements WatchDog {
    private final Logger logger = new Logger(C.TAG, "JagWatchDog");
    private final int watchInterval;
    private final Handler handler;
    private Thread.State state = Thread.State.NEW;
    private Handler _uiHandler = new Handler(Looper.getMainLooper());
    private JagAnrInterceptor anrInterceptor;

    public JagDefaultWatchDog(int watchInterval) {
        this.watchInterval = watchInterval;
        HandlerThread handlerThread = new HandlerThread("JagDefaultWatchDog", Thread.NORM_PRIORITY+1);
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
        handler.sendEmptyMessage(AnrCheckHandler.WHAT_CHECK);
    }

    @Override
    public void setANRInterceptor(JagAnrInterceptor jagAnrInterceptor) {
        anrInterceptor = jagAnrInterceptor;
    }

    private class AnrCheckHandler extends Handler {
        public static final int WHAT_START = 0;
        public static final int WHAT_INTERVAL = 1;
        public static final int WHAT_CHECK = 2;
        private final long LOOP_INTERVAL = 1000;
        private volatile long _tick;
        private long interval;
        private long intervalStart;

        public AnrCheckHandler(Looper looper) {
            super(looper);
            interval = watchInterval;
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case WHAT_CHECK:
                    //logger.v("WHAT_CHECK");
                    long now = System.currentTimeMillis();
                    sendEmptyMessageDelayed(WHAT_CHECK, LOOP_INTERVAL);
                    if (_tick <= 0) {
                        //logger.v("post _ticker");
                        _uiHandler.post(this::_ticker);
                        intervalStart = now;
                        _tick = interval;
                    } else {
                        if (now - intervalStart > (watchInterval - LOOP_INTERVAL)) {
                            if (anrInterceptor != null) {
                                interval = anrInterceptor.intercept(_tick);
                                if (interval > 0) {
                                    _tick = 0;
                                    break;
                                }
                            }

                            throw JagAnrError.NewMainOnly(_tick);
                        }
                    }
                    break;
                case WHAT_START:
                    boolean needPost = _tick == 0;
                    _tick = interval;
                    logger.v("send WHAT_INTERVAL");
                    sendEmptyMessageDelayed(WHAT_INTERVAL, LOOP_INTERVAL);
                    if (needPost) {
                        _uiHandler.post(this::_ticker);
                    }
                    intervalStart = System.currentTimeMillis();
                    break;
                case WHAT_INTERVAL:
                    logger.v("WHAT_INTERVAL");
                    if (_tick != 0) {
                        if (anrInterceptor != null) {
                            interval = anrInterceptor.intercept(_tick);
                            if (interval > 0) {
                                sendEmptyMessage(WHAT_START);
                                break;
                            }
                        }

                        //throw JagAnrError.NewMainOnly(_tick);
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
