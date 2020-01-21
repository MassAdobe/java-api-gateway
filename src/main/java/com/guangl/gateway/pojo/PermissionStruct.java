package com.guangl.gateway.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

/**
 * @ClassName: PermissionStruct
 * @Author: MassAdobe
 * @Email: massadobe8@gmail.com
 * @Description: permission的结构体
 * @Date: Created in 2020-01-13 14:17
 * @Version: 1.0.0
 * @param: * @param null
 */
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class PermissionStruct {

    private List<String> permissions;

}
