package me.jbusdriver.plugin.magnet;

import android.content.Context;
import android.widget.Toast;

import com.wlqq.phantom.communication.PhantomService;
import com.wlqq.phantom.communication.RemoteMethod;

import java.util.HashMap;
import java.util.Map;

import me.jbusdriver.plugin.magnet.common.loader.IMagnetLoader;
import me.jbusdriver.plugin.magnet.loaderImpl.BtGiggImpl;

@PhantomService(name = BuildConfig.APPLICATION_ID + "/MagnetJavaService", version = 1)
public class MangetJavaService {

    @RemoteMethod(name = "pluginToast")
    public String pluginToast(Context context) {
        Toast.makeText(context, "hello java from plugin!", Toast.LENGTH_LONG).show();
        return "hello java from plugin!";
    }

    @RemoteMethod(name = "getLoader")
    public IMagnetLoader getLoader() {
        return MagnetLoaders.INSTANCE.getLoaders().get("BTDB");
    }


    @RemoteMethod(name = "getAllLoaders")
    public Map<String, IMagnetLoader> getAllLoaders() {
        Map<String, IMagnetLoader> s = new HashMap<>();
        s.put("bgg", new BtGiggImpl());
        return s;
    }
}
