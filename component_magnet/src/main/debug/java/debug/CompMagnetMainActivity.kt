package debug

import android.Manifest
import android.os.Bundle
import android.os.Environment
import com.billy.cc.core.component.CC
import com.tbruyelle.rxpermissions2.RxPermissions
import com.wlqq.phantom.communication.PhantomServiceManager
import com.wlqq.phantom.library.PhantomCore
import com.wlqq.phantom.library.proxy.PluginContext
import io.reactivex.BackpressureStrategy
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.comp_magnet_activity_main.*
import me.jbusdriver.base.JBusManager
import me.jbusdriver.base.KLog
import me.jbusdriver.base.common.BaseActivity
import me.jbusdriver.base.common.C
import me.jbusdriver.base.phantom.installAssetsPlugins
import me.jbusdriver.base.phantom.installFromPathDir
import me.jbusdriver.base.toast
import me.jbusdriver.component.magnet.MagnetPluginHelper
import me.jbusdriver.component.magnet.MagnetPluginHelper.MagnetService
import me.jbusdriver.component.magnet.MagnetPluginHelper.PluginMagnetPackage
import me.jbusdriver.component.magnet.R
import java.io.File
import kotlin.concurrent.thread


class CompMagnetMainActivity : BaseActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.comp_magnet_activity_main)
        comp_magnet_tv_go_search.setOnClickListener {
            CC.obtainBuilder(C.Components.Magnet)
                .setActionName("show")
                .addParam("keyword", et_keyword.text.toString())
                .build().callAsync { cc, result ->
                    KLog.d("install result $result")
                }
        }
        //"allKeys" , "config.save" , "config.getKeys"
        comp_magnet_tv_get_all.setOnClickListener {
            CC.obtainBuilder(C.Components.Magnet)
                .setActionName("allKeys")
                .build().callAsync()

            CC.obtainBuilder(C.Components.Magnet)
                .setActionName("config.save")
                .addParam("keys", MagnetPluginHelper.getLoaderKeys())
                .build().call()
        }

        comp_magnet_tv_config_get.setOnClickListener {
            CC.obtainBuilder(C.Components.Magnet)
                .setActionName("config.getKeys")
                .build().call()
        }

        comp_magnet_tv_config_save.setOnClickListener {
            val res = CC.obtainBuilder(C.Components.Magnet)
                .setActionName("allKeys")
                .build().call()
            if (res.isSuccess) {
                val allKeys = res.getDataItem<List<String>>("keys")
                CC.obtainBuilder(C.Components.Magnet)
                    .setActionName("config.save")
                    .addParam(
                        "keys",
                        allKeys.shuffled().take((Math.random() * allKeys.size).toInt().coerceIn(1..allKeys.size))
                    )
                    .build().call()
            } else {
                KLog.w("error get all key : ${res.errorMessage}")
            }

        }
        iv_install_plugin.setOnClickListener {
            val pluginsDir = "plugins"
            installAssetsPlugins(assets, pluginsDir).subscribe({
                KLog.d("all plugin $it")
                toast("插件已经安装 ${it.joinToString { it.packageName }}")
            }, {

                KLog.w("erorr $it")
            }).addTo(rxManager)
        }
        iv_test_plugin.setOnClickListener {
            val pluginInfo = PhantomCore.getInstance().findPluginInfoByPackageName(PluginMagnetPackage)
            pluginInfo?.let {
                val pluginContext = PluginContext(this, pluginInfo).createContext()
                PhantomServiceManager.getService(MagnetService)
                // 插件 Phantom Service 代理对象
                val service = PhantomServiceManager.getService(PluginMagnetPackage, MagnetService)
                if (service == null) {

                    KLog.w("not find service ")
                    return@let
                }
                try {
                    val res = service.call("pluginToast", pluginContext)
                    KLog.d("result $res")
                } catch (e: Exception) {
                    KLog.w("service.call error $e")
                }


            } ?: kotlin.run {
                KLog.w("not find plugin info")
            }
        }

        iv_test_loader_keys.setOnClickListener {
            val keys = MagnetPluginHelper.getLoaderKeys()
            KLog.d("keys $keys")
        }

        iv_test_load_pag1.setOnClickListener {
            thread {
                MagnetPluginHelper.getMagnets("btdigg", et_keyword.text.toString(), 1)
            }

        }

        iv_test_has_next.setOnClickListener {
            MagnetPluginHelper.hasNext("btdigg")
        }


        iv_test_update.setOnClickListener {
            RxPermissions(this).request(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
                .toFlowable(BackpressureStrategy.LATEST)
                .flatMap {
                    return@flatMap installFromPathDir(File(Environment.getExternalStorageDirectory().absolutePath + File.separator + JBusManager.context.packageName + File.separator + "plugins"))
                }.subscribe({
                    KLog.d("all plugin $it")
                    toast("插件已经安装 ${it.joinToString { it.packageName }}")
                }, {
                    KLog.w("erorr $it")
                }).addTo(rxManager)


        }

        val callBack = { p: Int -> KLog.d("progress ---> $p") }
        iv_test_callback.setOnClickListener {
            CC.obtainBuilder(C.Components.PluginManager)
                .setActionName("callback")
                .setParamWithNoKey(callBack)
                .build().callAsync()
        }

        MagnetPluginHelper.init()
    }


}
