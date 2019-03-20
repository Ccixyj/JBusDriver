package jbusdriver.me.jbusdriver

import org.junit.Test

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

            if (i % 3 == 0) return@mapIndexed i * 3
            return@mapIndexed i
        }.let {
            println(it)
        }
    }
}
