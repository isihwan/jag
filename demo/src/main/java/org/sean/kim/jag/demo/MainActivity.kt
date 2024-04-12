package org.sean.kim.jag.demo

import android.os.Bundle
import android.os.Environment
import android.view.KeyEvent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlinx.coroutines.selects.select
import java.io.File

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_BACK -> {
                finish()
                return true
            }
            KeyEvent.KEYCODE_0 -> {
                test0()
                return true
            }
        }
        return super.onKeyUp(keyCode, event)
    }

    private fun test0() {
        val success = fileBlockingExecution()
    }

    private fun fileBlockingExecution(): Boolean {
        var dir = getFilesDir()
        if (dir != null) {
            val file = File(dir, "test.txt")
            if (file.exists()) {
                return file.delete()
            }
            var out = file.outputStream()
            for (i in 0..50000) {
                out.write("Hello, World!\n".toByteArray())
                Thread.sleep(1000)
            }
            return true
        }
        return false
    }
}