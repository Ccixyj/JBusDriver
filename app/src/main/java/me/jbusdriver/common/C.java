package me.jbusdriver.common;

import me.jbusdriver.ui.data.DataSourceType;

/**
 * Created by Administrator on 2017/4/9.
 */

public class C {
    public static class Cache {

        public static final String ANNOUNCEURL = "announceUrl"; //发布的网站
        public static final String BUS_URLS = "bus_urls"; //发布的网站
        public static final String USER_COLLECT = "user_collect"; //用户收藏数据
        public static final String CENSORED = DataSourceType.CENSORED.getKey(); //首页数据,缓存第一页

    }


    public static class SavedInstanceState {
        public static final String RECREATION_SAVED_STATE = "RECREATION_SAVED_STATE";
        public static final String LOADER_ID_SAVED_STATE = "LOADER_ID_SAVED_STATE";
    }


}
