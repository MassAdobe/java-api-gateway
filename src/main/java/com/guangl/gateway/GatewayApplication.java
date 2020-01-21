package com.guangl.gateway;

import com.guangl.gateway.config.OSinfo;
import com.guangl.gateway.constants.ConstantsConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import tk.mybatis.spring.annotation.MapperScan;

import java.util.Properties;

/**
 * @ClassName: GatewayApplication
 * @Author: MassAdobe
 * @Email: massadobe8@gmail.com
 * @Description: 启动类
 * @Date: Created in 2019-12-17 11:41
 * @Version: 1.0.0
 * @param: * @param null
 */
@SpringBootApplication
@EnableDiscoveryClient
@MapperScan(basePackages = {ConstantsConfig.TK_MYBATIS_SCAN_ADDR})
public class GatewayApplication {

    public static void main(String[] args) {
        Properties properties = new Properties();
        if (OSinfo.isWindows()) {
            properties.setProperty(ConstantsConfig.LOG_CONFIG_LOCATION_NAME, ConstantsConfig.WIN_LOG_PATH);
        } else if (OSinfo.isMacOSX() || OSinfo.isMacOS()) {
            properties.setProperty(ConstantsConfig.LOG_CONFIG_LOCATION_NAME, ConstantsConfig.MAC_LOG_PATH);
        } else {
            properties.setProperty(ConstantsConfig.LOG_CONFIG_LOCATION_NAME, ConstantsConfig.LINUX_LOG_PATH);
        }
        SpringApplication app = new SpringApplication(GatewayApplication.class);
        app.setDefaultProperties(properties);
        app.run(args);
    }

}
