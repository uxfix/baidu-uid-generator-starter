package com.dekux.uid.config;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.boot.autoconfigure.domain.EntityScanPackages;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * 注册自动扫描包路径
 *
 * @author yuan
 * @since 1.0
 */
public class RegisterPackageListener implements ApplicationListener<org.springframework.boot.context.event.ApplicationContextInitializedEvent> {
    @Override
    public void onApplicationEvent(org.springframework.boot.context.event.ApplicationContextInitializedEvent event) {
        ConfigurableApplicationContext applicationContext = event.getApplicationContext();
        ConfigurableListableBeanFactory beanFactory = applicationContext.getBeanFactory();
        if (beanFactory instanceof BeanDefinitionRegistry){
            BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;
            AutoConfigurationPackages.register(registry,"com.dekux.uid.worker");
            EntityScanPackages.register(registry,"com.dekux.uid.worker.entity");
        }
    }
}
