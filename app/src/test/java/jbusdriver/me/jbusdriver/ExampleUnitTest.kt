package jbusdriver.me.jbusdriver

import io.reactivex.Flowable
import io.reactivex.subscribers.ResourceSubscriber
import org.junit.Test



/**
 * Example local unit test, which will execute on the development machine (host).

 * @see [Testing documentation](http://d.android.com/tools/testing)
 */
class ExampleUnitTest {
    val TAG = ExampleUnitTest::class.java.simpleName
    @Test
    @Throws(Exception::class)
    fun addition_isCorrect() {
        Flowable.range(1, 3).subscribeWith(object : ResourceSubscriber<Int>() {

            override fun onStart() {
                super.onStart()
                println("OnSubscribe start")
                println("OnSubscribe end")
            }

            override fun onNext(v: Int?) {
                println(v)
                if (v ==2) dispose()
            }

            override fun onError(e: Throwable) {
                e.printStackTrace()
            }

            override fun onComplete() {
                println("Done")
            }
        })
    }
}