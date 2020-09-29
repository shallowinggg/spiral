package io.github.shallowinggg.spiral.spring;

import io.github.shallowinggg.spiral.config.SpiralConstant;
import io.github.shallowinggg.spiral.util.SystemPropertyUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

/**
 * {@link ApplicationConfigBeanPostProcessor} bean registrar.
 *
 * @author ding shimin
 */
public class SpiralApplicationConfigRegistrar implements ImportBeanDefinitionRegistrar {

    private static final String APPLICATION_CONFIG_BEAN_PROCESSOR_NAME = "applicationConfigBeanPostProcessor";

    private static final String REFERENCE_CONFIG_BEAN_PROCESSOR_NAME = "referenceConfigBeanPostProcessor";

    @Override
    public void registerBeanDefinitions(AnnotationMetadata annotationMetadata, BeanDefinitionRegistry beanDefinitionRegistry) {
        if (SystemPropertyUtils.getBoolean(SpiralConstant.ENABLE_PROPERTY, false)) {
            registerApplicationConfigBeanPostProcessor(beanDefinitionRegistry);
            registerReferenceConfigBeanPostProcessor(beanDefinitionRegistry);
        }
    }

    private void registerApplicationConfigBeanPostProcessor(BeanDefinitionRegistry registry) {
        if (!registry.containsBeanDefinition(APPLICATION_CONFIG_BEAN_PROCESSOR_NAME)) {
            RootBeanDefinition beanDefinition = new RootBeanDefinition(ApplicationConfigBeanPostProcessor.class);
            beanDefinition.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
            registry.registerBeanDefinition(APPLICATION_CONFIG_BEAN_PROCESSOR_NAME, beanDefinition);
        }
    }

    private void registerReferenceConfigBeanPostProcessor(BeanDefinitionRegistry registry) {
        if (!registry.containsBeanDefinition(REFERENCE_CONFIG_BEAN_PROCESSOR_NAME)) {
            RootBeanDefinition beanDefinition = new RootBeanDefinition(ReferenceConfigBeanPostProcessor.class);
            beanDefinition.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
            registry.registerBeanDefinition(REFERENCE_CONFIG_BEAN_PROCESSOR_NAME, beanDefinition);
        }
    }
}
