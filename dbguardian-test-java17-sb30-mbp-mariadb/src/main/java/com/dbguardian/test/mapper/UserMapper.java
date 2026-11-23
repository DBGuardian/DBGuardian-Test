package com.dbguardian.test.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dbguardian.test.entity.User;
import org.apache.ibatis.annotations.Select;

public interface UserMapper extends BaseMapper<User> {

    @Select("SELECT @@GLOBAL.server_id")
    long selectServerId();
}
