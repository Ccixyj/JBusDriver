package me.jbusdriver.plugin.magnet.loaders

import android.util.Log
import me.jbusdriver.plugin.magnet.IMagnetLoader
import org.json.JSONObject

class CiLiDaoLoaderImpl : IMagnetLoader {

    private val searchUrl = "https://cilidao.org/query?word=%s&page=%s&sort=rele"

    private val Tag = "cilidao"
    override var hasNexPage: Boolean = false



    override fun loadMagnets(key: String, page: Int): List<JSONObject> {
        return try {
            val url = searchUrl.format( EncodeHelper.utf8Encode(key),page).trim()
            val doc = IMagnetLoader.safeJsoupGet(url) ?: return emptyList()
            Log.d(Tag, "search :$url")
            hasNexPage = true
            return doc.select(".Search_results  li").map { item ->
                Log.i(Tag, "find item : $item")
                val txtBloc = item.select(".SearchListTitle_list_title")
                val title = txtBloc.text()
                val url = "https://cilidao.org" + txtBloc.select("a").last().attr("href")
                val infos = item.select(".Search_result_type").text().split(" ")
                val split = infos.size / 2
                JSONObject().apply {
                    put("name", title)
                    put("size", infos.take(2).joinToString(" "))
                    put("date", infos.takeLast(infos.size - split).joinToString(" "))
                    put("link", url)
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }


    }

    override fun fetchMagnetLink(url: String): String {
        Log.d(Tag, "fetchMagnetLink :$url")
        return IMagnetLoader.safeJsoupGet(url)?.select(".infohash-box span")?.text().orEmpty()
    }

}
