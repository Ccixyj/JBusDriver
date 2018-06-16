package me.jbusdriver.mvp.bean

import android.support.annotation.IdRes
import com.chad.library.adapter.base.entity.AbstractExpandableItem
import com.chad.library.adapter.base.entity.MultiItemEntity
import jbusdriver.me.jbusdriver.R
import me.jbusdriver.base.common.BaseFragment
import me.jbusdriver.ui.data.AppConfiguration
import me.jbusdriver.ui.data.enums.DataSourceType
import me.jbusdriver.ui.fragment.*

/*首页菜单配置化*/
data class MenuOp(@IdRes val id: Int, val name: String, val initializer: () -> BaseFragment) : MultiItemEntity {

    override fun getItemType() = Expand_Type_Item

    var isHow: Boolean = true
        get() = AppConfiguration.menuConfig[name] ?: true

    companion object {
        val Ops: List<MenuOp> by lazy {  mine + nav_ma + nav_uncensore + nav_xyz + nav_other }


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