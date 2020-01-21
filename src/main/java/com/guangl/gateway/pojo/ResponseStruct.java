package com.guangl.gateway.pojo;

import com.guangl.gateway.enums.ErrorCodeMsg;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName: ResponseStruct
 * @Author: MassAdobe
 * @Email: massadobe8@gmail.com
 * @Description: TODO
 * @Date: Created in 2019-12-18 14:04
 * @Version: 1.0.0
 * @param: * @param null
 */
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ResponseStruct {
    private final static String SUCCESS_DESC = "成功";

    private int code;
    private String msg;
    private Object data;
    private List<String> permission;

    public static ResponseStruct success() {
        return new ResponseStruct(ErrorCodeMsg.SUCCESS.getCode(), SUCCESS_DESC, "", new ArrayList<>());
    }

    public static ResponseStruct success(Object data) {
        return new ResponseStruct(ErrorCodeMsg.SUCCESS.getCode(), SUCCESS_DESC, data, new ArrayList<>());
    }

    public static ResponseStruct failure(int code, String msg) {
        return new ResponseStruct(code, msg, "", new ArrayList<>());
    }

    public static ResponseStruct failure(int code, String msg, Object data) {
        return new ResponseStruct(code, msg, data, new ArrayList<>());
    }

}
