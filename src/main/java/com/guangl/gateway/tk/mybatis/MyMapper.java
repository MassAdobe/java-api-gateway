package com.guangl.gateway.tk.mybatis;

import tk.mybatis.mapper.common.Mapper;
import tk.mybatis.mapper.common.MySqlMapper;

/**
 * @ClassName: MyMapper
 * @Author: MassAdobe
 * @Email: massadobe8@gmail.com
 * @Description: TODO
 * @Date: Created in 2019-12-13 11:55
 * @Version: 1.0.0
 * @param: * @param null
 */
public interface MyMapper<T> extends Mapper<T>, MySqlMapper<T> {
}
