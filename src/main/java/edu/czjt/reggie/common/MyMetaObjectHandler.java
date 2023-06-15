package edu.czjt.reggie.common;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Created by jinkun.tian on 2023/3/21
 */
@Component//注意:这个要记得交给spring容器管理，不然这个功能就没发用。。。。
//那么怎么确定你要添加的功能是不是要交给容器管理呢？就是你直接写了一个工具类或者是功能类，
// 需要对数据库的数据或者是数据库数据的结果产生影响的时候，你明明写了这样一个类，但是功能却没有生效，那么这个时候就要首先考虑是不是容器没有托管这个类
@Slf4j
public class MyMetaObjectHandler implements MetaObjectHandler {

    /**
     * 可以统一对这些字段进行处理，避免了重复代码
     * 插入操作，自动填充，注意这两个方法必须同时包含createTime和updateTime才可使用
     * @param metaObject
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        log.info("公共字段自动填充[insert]...");
        log.info(metaObject.toString());

        metaObject.setValue("createTime", LocalDateTime.now());
        metaObject.setValue("updateTime", LocalDateTime.now());
        metaObject.setValue("createUser", BaseContext.getCurrentId());
        metaObject.setValue("updateUser", BaseContext.getCurrentId());
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        log.info("公共字段自动填充[update]...");
        log.info(metaObject.toString());

        metaObject.setValue("updateTime", LocalDateTime.now());
        metaObject.setValue("updateUser", BaseContext.getCurrentId());

    }
}
