package kr.sean.javademo;

import android.os.Bundle;
import android.os.HandlerThread;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;


import com.github.anrwatchdog.ANRWatchDog;

import org.sean.kim.jag.C;
import org.sean.kim.jag.Jag;
import org.sean.kim.jag.JagDefaultWatchDog;
import org.sean.kim.jag.JagResult;
import org.sean.kim.jag.util.Logger;
import org.sean.kim.jag.util.Blocking;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private Logger logger = new Logger(C.TAG, "Demo");
    private Button button1;
    private Button button2;
    private Jag jag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        HandlerThread th = new HandlerThread("JagWatchDog");
        th.start();
        jag = new Jag(this);
        //jag = new Jag(this, new ANRWatchDog(4500));
        //jag = new Jag(this, new JagDefaultWatchDog(4500));
        button1 = findViewById(R.id.button1);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                test0();
            }
        });
        button2 = findViewById(R.id.button2);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                test1();
            }
        });
        findViewById(R.id.button3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                test2();
            }
        });
    }

    @Override
    protected void onDestroy() {
        jag.release();
        super.onDestroy();
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return super.onKeyUp(keyCode, event);
    }

    private void test0() {
        JagResult<Boolean> result = jag.request(()->Blocking.fileBlockingLongExecution(this));
        if (result.get(false)) button1.setText("Success");
        else button1.setText("Failed");
    }

    private void test1() {
        JagResult<Boolean> result = jag.request(()->Blocking.fileBlockingLongExecution(this));
        result.onComplete((Boolean success)->{
            logger.i("test1: %s", success ? "Success" : "Failed");
            button2.setText(success ? "Success" : "Failed");
        });
        logger.i("waiting...");
    }

    private void test2() {
        try {
            Blocking.fileBlockingLongExecution(this);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}