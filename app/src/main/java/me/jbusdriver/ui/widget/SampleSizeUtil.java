package me.jbusdriver.ui.widget;

import android.opengl.GLES10;

public class SampleSizeUtil {

    private static int textureSize = 0;

    //存在第二次拿拿不到的情况，所以把拿到的数据用一个static变量保存下来
    public static final int getTextureSize() {
        if (textureSize > 0) {
            return textureSize;
        }

        int[] params = new int[1];
        GLES10.glGetIntegerv(GLES10.GL_MAX_TEXTURE_SIZE, params, 0);
        textureSize = params[0];

        return textureSize;
    }

    // 将x向上对齐到2的幂指数
    private static int roundup2n(int x) {
        if ((x & (x - 1)) == 0) {
            return x;
        }
        int pos = 0;
        while (x > 0) {
            x >>= 1;
            ++pos;
        }
        return 1 << pos;
    }
}
