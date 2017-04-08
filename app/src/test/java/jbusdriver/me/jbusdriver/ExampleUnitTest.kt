package jbusdriver.me.jbusdriver

import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subscribers.ResourceSubscriber
import org.junit.Test
import java.util.concurrent.TimeUnit


/**
 * Example local unit test, which will execute on the development machine (host).

 * @see [Testing documentation](http://d.android.com/tools/testing)
 */
class ExampleUnitTest {
    val TAG = ExampleUnitTest::class.java.simpleName
    @Test
    @Throws(Exception::class)
    fun addition_isCorrect() {
        listOf(300, 100, 400, 600).map { Flowable.just(it).delay(it.toLong(), TimeUnit.MILLISECONDS, Schedulers.computation()) }.let { Flowable.concat(it) }
                .subscribeWith(object : ResourceSubscriber<Int>() {

                    override fun onStart() {
                        super.onStart()
                        println("OnSubscribe start")
                        println("OnSubscribe end")
                    }

                    override fun onNext(v: Int?) {
                        println(v)
                        if (v == 5) dispose()
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