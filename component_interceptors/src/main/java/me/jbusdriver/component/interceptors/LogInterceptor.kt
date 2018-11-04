package me.jbusdriver.component.interceptors

import com.billy.cc.core.component.CCResult
import com.billy.cc.core.component.Chain
import com.billy.cc.core.component.IGlobalCCInterceptor
import me.jbusdriver.base.KLog

/**
 * 示例全局拦截器：日志打印
 * @author billy.qi
 * @since 18/5/26 11:42
 */
class LogInterceptor : IGlobalCCInterceptor {
    val TAG = "LogInterceptor"
    override fun priority() = 1

    override fun intercept(chain: Chain): CCResult {
        KLog.d("LogInterceptor============log before:" + chain.cc)
        chain.cc
        val result = chain.proceed()
        KLog.d("LogInterceptor============log after:$result")
        return result
    }
}
