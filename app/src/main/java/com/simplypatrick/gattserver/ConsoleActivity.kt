package com.simplypatrick.gattserver

import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_console.*
import java.text.SimpleDateFormat
import java.util.*

open class ConsoleActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_console)
    }

    // Use Handler instead of runOnUiThread to guarantee lines are printed in absolute timing order
    private val handler = Handler()

    // SimpleDateFormat is not thread-safe. Use thread local storage to workaround it.
    private val dateFormat = object : ThreadLocal<SimpleDateFormat>() {
        override fun initialValue(): SimpleDateFormat {
            return SimpleDateFormat("MM dd, yyyy 'at' H:mm:ss:SSS", Locale.US)
        }
    }

    fun println(line: Any?) {
        val now = Calendar.getInstance().time
        val thread = Thread.currentThread()
        val annotatedLine = "${dateFormat.get().format(now)} (${"%4d".format(thread.id)}) $line\n"

        handler.post({
            kotlin.io.println(annotatedLine)
            console.append(annotatedLine)
        })
    }
}