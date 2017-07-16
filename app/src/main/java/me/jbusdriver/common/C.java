package me.jbusdriver.common;

/**
 * Created by Administrator on 2017/4/9.
 */

public class C {
    public static class Cache {
        public static final int DAY = ACache.TIME_DAY ; //缓存的固定时间
        public static final int WEEK = ACache.TIME_DAY * 7; //缓存的固定时间

        public static final String ANNOUNCEURL = "announceUrl"; //发布的网站

        public static final String BUS_URLS = "bus_urls"; //发布的网站
        public static final String USER_COLLECT = "user_collect"; //用户收藏数据
    }


    public static class SavedInstanceState {
        public static final String RECREATION_SAVED_STATE = "RECREATION_SAVED_STATE";
        public static final String LOADER_ID_SAVED_STATE = "LOADER_ID_SAVED_STATE";
    }

    public static class Cookie {
        public static final String AddCookie = "AddCookie";
    }

    public static class BundleKey {
        public static final String Key_1 = "Key_1";
        public static final String Key_2 = "Key_2";
    }
}
