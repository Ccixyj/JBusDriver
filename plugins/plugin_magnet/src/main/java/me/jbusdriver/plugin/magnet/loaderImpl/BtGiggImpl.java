package me.jbusdriver.plugin.magnet.loaderImpl;

import android.util.Base64;


import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.jsoup.Connection;

import java.util.ArrayList;
import java.util.List;

import kotlin.text.Charsets;
;
import me.jbusdriver.plugin.magnet.IMagnetLoader;

public class BtGiggImpl implements IMagnetLoader {
    private boolean hasNext = true;
    private String search = "https://www.btdigg.xyz/search/%s/%s/1/0.html";

    @Override
    public boolean getHasNexPage() {
        return hasNext;
    }

    @Override
    public void setHasNexPage(boolean b) {
        this.hasNext = b;
    }

    @NotNull
    @Override
    public List<JSONObject> loadMagnets(@NotNull String key, int page) {
        List<JSONObject> mags = new ArrayList<>();
        String connentUrl = String.format(search, encode(key), page);
/*
        try {
            Log.d("BtGiggImpl","connect : " + connentUrl);
            Connection c = initHeaders(Jsoup.connect(connentUrl));
            Document doc = c.get();
            setHasNexPage(doc.select(".page-split :last-child[title]").size() > 0);
            Elements elements = doc.select(".list dl");
            Log.d("BtGiggImpl","eles : " + elements);
            for (Element it : elements) {
                Elements href = it.select("dt a");
                String title = href.text();
                String url = href.attr("href");
                String realUrl = "";
                if (url.startsWith("www.")) {
                    realUrl = "https://" + url;
                } else if (url.startsWith("/magnet")) {
                    realUrl = "magnet:?xt=urn:btih:" + url.replace("/magnet/", "").replace(".html", "");
                } else {
                    realUrl = "https://www.btdigg.xyz" + url;
                }


                Elements labels = it.select(".attr span");
                mags.add(new Magnet(title, labels.get(1).text(), labels.first().text(), realUrl));
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("BtGiggImpl","error : " + e);
        }
*/
        return mags;
    }

    @NotNull
    @Override
    public String encode(@NotNull String string) {
        return Base64.encodeToString(string.getBytes(Charsets.UTF_8), Base64.NO_PADDING | Base64.URL_SAFE).trim();
    }

    @NotNull
    @Override
    public Connection initHeaders(@NotNull Connection $receiver) {
        return $receiver.userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.67 Safari/537.36").followRedirects(true)
                .header("Accept-Encoding", "gzip, deflate, sdch")
                .header("Accept-Language", "zh-CN,zh;q=0.8");
    }

    @NotNull
    @Override
    public String fetchMagnetLink(@NotNull String url) {
        return null;
    }
}
