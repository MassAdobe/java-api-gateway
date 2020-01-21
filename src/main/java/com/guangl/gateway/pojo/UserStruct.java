package com.guangl.gateway.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Date;
import java.util.List;

/**
 * @ClassName: UserStruct
 * @Author: MassAdobe
 * @Email: massadobe8@gmail.com
 * @Description: 网关和SSO专用的结构体(redis)
 * @Date: Created in 2020-01-10 15:57
 * @Version: 1.0.0
 * @param: * @param null
 */
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class UserStruct {

    // api接口的权限
    private List<String> admissions;
    // 用户对应的系统Salt值
    private String salt;
    // 用户可用系统时间
    private Date date;
}
