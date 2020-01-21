package com.guangl.gateway.config;

import com.google.common.base.Strings;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
@AutoConfigureBefore(NacosConfiguration.class)
public class LaunchConfiguration {

    private final static Logger logger = LoggerFactory.getLogger(LaunchConfiguration.class);

    public final static String NACOS_NAME_SPACE = System.getProperty("nacos.namespace");

    static {
        // NACOS_NAME_SPACE = System.getProperty("nacos.namespace");
        logger.info(Strings.lenientFormat("【NACOS-NAMESPACE-LAUNCH-INIT】：CONTENT：%s SUCCESS！", NACOS_NAME_SPACE));
    }

}
