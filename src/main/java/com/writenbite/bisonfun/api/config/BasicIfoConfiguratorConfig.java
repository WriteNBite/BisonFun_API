package com.writenbite.bisonfun.api.config;

import com.writenbite.bisonfun.api.types.builder.configurator.VideoContentBasicInfoBuilderConfigurator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class BasicIfoConfiguratorConfig {
    private final List<VideoContentBasicInfoBuilderConfigurator<?>> allConfigurators;

    public BasicIfoConfiguratorConfig(List<VideoContentBasicInfoBuilderConfigurator<?>> allConfigurators) {
        this.allConfigurators = allConfigurators;
    }

    @Bean
    public BasicInfoConfiguratorRegistry configuratorRegistry(){
        return new BasicInfoConfiguratorRegistry(allConfigurators);
    }
}
