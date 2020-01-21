package com.guangl.gateway.config;

import com.alibaba.nacos.api.annotation.NacosProperties;
import com.alibaba.nacos.api.config.ConfigType;
import com.alibaba.nacos.spring.context.annotation.config.EnableNacosConfig;
import com.alibaba.nacos.spring.context.annotation.config.NacosPropertySource;
import com.guangl.gateway.constants.ConstantsConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableNacosConfig(globalProperties = @NacosProperties(serverAddr = ConstantsConfig.NACOS_ADDRS))
@NacosPropertySource(dataId = ConstantsConfig.NACOS_FILE_NAME, groupId = ConstantsConfig.NACOS_GROUP, autoRefreshed = ConstantsConfig.NACOS_REFRESH, type = ConfigType.YAML)
public class NacosConfiguration {

    private final static Logger logger = LoggerFactory.getLogger(NacosConfiguration.class);

}
