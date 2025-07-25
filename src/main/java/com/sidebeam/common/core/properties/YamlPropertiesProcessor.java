package com.sidebeam.common.core.properties;

import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.Resource;

import java.util.Properties;

/**
 * Simple replacement for YamlPropertiesProcessor to make PropertyUtil compile.
 */
public class YamlPropertiesProcessor {
    
    private final Resource resource;
    
    public YamlPropertiesProcessor(Resource resource) {
        this.resource = resource;
    }
    
    public Properties createProperties() {
        YamlPropertiesFactoryBean factory = new YamlPropertiesFactoryBean();
        factory.setResources(resource);
        return factory.getObject();
    }
}