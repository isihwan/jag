package org.sean.kim.jag.demo

import android.os.Bundle
import android.view.KeyEvent
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import org.sean.kim.jag.C
import org.sean.kim.jag.Jag
import org.sean.kim.jag.util.Blocking
import org.sean.kim.jag.util.Logger

class MainActivity : AppCompatActivity() {
    private val logger: Logger = Logger(C.TAG, "Demo")
    private lateinit var jag: Jag
    private lateinit var button1: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        jag = Jag(this)
        button1 = this.findViewById(R.id.button1)
        button1.setOnClickListener {
            test0()
        }
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
        jag.request { Blocking.fileBlockingLongExecution(this) }.let {
            it.get(false).let {
                logger.d("test0", "result: $it")
                if (it) button1.text = "Success"
                else button1.text = "Failed"
            }
        }
    }

}