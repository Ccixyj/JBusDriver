package me.jbusdriver.base.cc;

import com.billy.cc.core.component.IParamJsonConverter;

import me.jbusdriver.base.GobalKt;

/**
 * 用Gson来进行跨app调用时的json转换
 * @author billy.qi
 * @since 18/5/28 19:48
 */
public class GsonParamConverter implements IParamJsonConverter {


    @Override
    public <T> T json2Object(String input, Class<T> clazz) {
        return GobalKt.getGSON().fromJson(input, clazz);
    }

    @Override
    public String object2Json(Object instance) {
        return GobalKt.getGSON().toJson(instance);
    }
}
