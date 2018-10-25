package jbusdriver.me.jbusdriver

import android.content.Context
import android.support.test.InstrumentationRegistry

import org.junit.Test

import org.junit.Assert.assertEquals

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see [Testing documentation](http://d.android.com/tools/testing)
 */
class ExampleInstrumentedTest {
    @Test
    @Throws(Exception::class)
    fun useAppContext() {

        (0..100).mapIndexed() { index, i ->

            if (i %3 ==0) return@mapIndexed i * 3
            return@mapIndexed i
        }.let {
            println(it)
        }
    }
}
