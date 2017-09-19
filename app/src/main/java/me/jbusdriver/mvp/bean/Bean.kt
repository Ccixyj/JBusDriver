package me.jbusdriver.mvp.bean

import android.support.annotation.IdRes
import com.chad.library.adapter.base.entity.AbstractExpandableItem
import com.chad.library.adapter.base.entity.MultiItemEntity
import jbusdriver.me.jbusdriver.R
import me.jbusdriver.common.BaseFragment
import me.jbusdriver.http.JAVBusService
import me.jbusdriver.ui.data.AppConfiguration
import me.jbusdriver.ui.data.DataSourceType
import me.jbusdriver.ui.data.SearchType
import me.jbusdriver.ui.fragment.*
import java.io.Serializable

/**
 * Created by Administrator on 2017/4/9.
 */

const val Expand_Type_Head = 0
const val Expand_Type_Item = 1


interface ILink : Serializable {
    val link: String


}

val ILink.des: String
    inline get() = when (this) {
        is Header -> "$name $value"
        is Genre -> "类别 $name"
        is ActressInfo -> "演员 $name"
        is me.jbusdriver.mvp.bean.Movie -> "$code $title"
        is SearchLink -> "搜索 $query"
        is PageLink -> "$title 第 $page 页" /*${if (isAll) "全部" else "已有种子"}电影*/
        else -> error(" $this has no matched class for des")
    }

val ILink.DBtype: Int
    inline get() = when (this) {
        is Movie -> 1
        is ActressInfo -> 2
        is Header -> 3
        is Genre -> 4
        is SearchLink -> 5
        is PageLink -> 6
        else -> error(" $this has no matched class for des")
    }


data class PageLink(val page: Int, val title: String /*XX类型*/, override val link: String) : ILink

data class PageInfo(val activePage: Int = 0, val nextPage: Int = 0,
                    val activePath: String = "",
                    val nextPath: String = "",
                    val pages: List<Int> = listOf())

val PageInfo.hasNext
    inline get() = activePage < nextPage


data class SearchLink(val type: SearchType, var query: String) : ILink {

    override val link: String
        get() = "${JAVBusService.defaultFastUrl}${type.urlPathFormater.format(query)}"

}


data class UpdateBean(val versionCode: Int, val versionName: String, val url: String, val desc: String)
data class NoticeBean(val id: Int, val content: String? = null)


/*首页菜单配置化*/
data class MenuOp(@IdRes val id: Int, val name: String, val initializer: () -> BaseFragment) : MultiItemEntity {

    override fun getItemType() = Expand_Type_Item

    var isHow: Boolean = true
        get() = AppConfiguration.menuConfig[name] ?: true

    companion object {
        val Ops: List<MenuOp>
            get() = mine + nav_ma + nav_uncensore + nav_xyz + nav_other

        val mine by lazy {
            listOf(
                    MenuOp(R.id.mine_collect, "收藏夹") { MineCollectFragment.newInstance() },
                    MenuOp(R.id.mine_history, "最近") { HistoryFragment.newInstance() }
            )
        }

        val nav_ma by lazy {
            listOf(
                    MenuOp(R.id.movie_ma, "有碼") { HomeMovieListFragment.newInstance(DataSourceType.CENSORED) },
                    MenuOp(R.id.movie_ma_actress, "有碼女優") { ActressListFragment.newInstance(DataSourceType.ACTRESSES) },
                    MenuOp(R.id.movie_ma_genre, "有碼類別") { GenrePagesFragment.newInstance(DataSourceType.GENRE) }

            )
        }

        val nav_uncensore by lazy {
            listOf(
                    MenuOp(R.id.movie_uncensored, "無碼") { HomeMovieListFragment.newInstance(DataSourceType.UNCENSORED) },
                    MenuOp(R.id.movie_uncensored_actress, "無碼女優") { ActressListFragment.newInstance(DataSourceType.UNCENSORED_ACTRESSES) },
                    MenuOp(R.id.movie_uncensored_genre, "無碼類別") { GenrePagesFragment.newInstance(DataSourceType.UNCENSORED_GENRE) }

            )
        }
        val nav_xyz by lazy {
            listOf(
                    MenuOp(R.id.movie_xyz, "欧美") { HomeMovieListFragment.newInstance(DataSourceType.XYZ) },
                    MenuOp(R.id.movie_xyz_actress, "欧美演员") { ActressListFragment.newInstance(DataSourceType.XYZ_ACTRESSES) },
                    MenuOp(R.id.movie_xyz_genre, "欧美類別") { GenrePagesFragment.newInstance(DataSourceType.XYZ_GENRE) }

            )
        }

        val nav_other by lazy {
            listOf(
                    MenuOp(R.id.movie_hd, "高清") { HomeMovieListFragment.newInstance(DataSourceType.GENRE_HD) },
                    MenuOp(R.id.movie_sub, "字幕") { HomeMovieListFragment.newInstance(DataSourceType.Sub) }
            )
        }
    }
}

data class MenuOpHead(val name: String) : AbstractExpandableItem<MenuOp>(), MultiItemEntity {
    override fun getItemType(): Int = Expand_Type_Head
    override fun getLevel() = 0
}