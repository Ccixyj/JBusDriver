package debug

import com.billy.cc.core.component.CC
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import com.orhanobut.logger.PrettyFormatStrategy
import me.jbusdriver.base.cc.BaseApp

class App : BaseApp() {
    override fun onCreate() {
        super.onCreate()

        val formatStrategy = PrettyFormatStrategy.newBuilder()
                .showThreadInfo(true)  // (Optional) Whether to show thread info or not. Default true
                .methodCount(2)         // (Optional) How many method line to show. Default 2
                .methodOffset(0)        // (Optional) Hides internal method calls up to offset. Default 5
                // .logStrategy(customLog) // (Optional) Changes the log strategy to print out. Default LogCat
                .tag("comp_magnet")   // (Optional) Global tag for every log. Default PRETTY_LOGGER
                .build()

        Logger.addLogAdapter(object : AndroidLogAdapter(formatStrategy) {
            override fun isLoggable(priority: Int, tag: String?) = true
        })

        CC.enableVerboseLog(true)
        CC.enableDebug(true)
        CC.enableRemoteCC(true)

    }
}
