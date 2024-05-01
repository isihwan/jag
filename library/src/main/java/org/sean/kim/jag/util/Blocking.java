package org.sean.kim.jag.util;

import android.content.Context;

import org.sean.kim.jag.C;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class Blocking {
    private static Logger logger = new Logger(C.TAG, "Blocking");
    static public Boolean fileBlockingLongExecution(Context context) throws IOException, InterruptedException {
        logger.i("enter fileBlockingExecution");
        File dir = context.getFilesDir();
        if (dir != null) {
            File file = new File(dir, "test.txt");
            for (int i = 0; i < 15; i++) {
                FileOutputStream out = new FileOutputStream(file);
                out.write("Hello\n".getBytes());
                out.close();
                Thread.sleep(1000);
            }
            logger.i("fileBlockingExecution done");
            return true;
        }
        logger.w("fileBlockingExecution failed");
        return false;
    }
}
