/*
 *    Copyright 2020 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
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
    public void registerBeanDefinitions(AnnotationMetadata annotationMetadata,
                                        BeanDefinitionRegistry beanDefinitionRegistry) {
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
