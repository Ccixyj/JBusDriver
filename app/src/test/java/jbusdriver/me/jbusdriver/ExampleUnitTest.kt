package jbusdriver.me.jbusdriver

import io.reactivex.schedulers.Schedulers
import org.junit.Test


/**
 * Example local unit test, which will execute on the development machine (host).

 * @see [Testing documentation](http://d.android.com/tools/testing)
 */
class ExampleUnitTest {
    @Test
    @Throws(Exception::class)
    fun addition_isCorrect() {

        Schedulers.trampoline().scheduleDirect {
            println("${Thread.currentThread().name} : start")
            Thread.sleep(200)
        }
    }
}