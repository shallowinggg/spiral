/*
 * Copyright © 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package io.github.shallowinggg.spiral.spring;

import io.github.shallowinggg.spiral.config.SpiralConstant;
import io.github.shallowinggg.spiral.util.SystemPropertyUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;

/**
 * {@link ApplicationConfigBeanPostProcessor} and {@link ReferenceConfigBeanPostProcessor} bean
 * registrar. They will only be registered when {@link SpiralConstant#ENABLE_PROPERTY} is set to
 * {@code true}.
 *
 * @author ding shimin
 * @since 0.1
 */
public class SpiralApplicationConfigRegistrar
		implements ImportBeanDefinitionRegistrar, EnvironmentAware {

	private static final String APPLICATION_CONFIG_BEAN_PROCESSOR_NAME =
			"io.github.shallowinggg.spiral.spring.ApplicationConfigBeanPostProcessor";

	private static final String REFERENCE_CONFIG_BEAN_PROCESSOR_NAME =
			"io.github.shallowinggg.spiral.spring.ReferenceConfigBeanPostProcessor";

	private static final String REFERENCE_BEAN_POST_PROCESSOR_NAME =
			"io.github.shallowinggg.spiral.spring.ReferenceBeanPostProcessor";

	private Environment environment;

	@Override
	public void setEnvironment(Environment environment) {
		this.environment = environment;
	}

	@Override
	public void registerBeanDefinitions(AnnotationMetadata annotationMetadata,
			BeanDefinitionRegistry beanDefinitionRegistry) {
		if (environment.getProperty(SpiralConstant.ENABLE_PROPERTY, Boolean.class, Boolean.FALSE)
				|| SystemPropertyUtils.getBoolean(SpiralConstant.ENABLE_PROPERTY, false)) {
			registerApplicationConfigBeanPostProcessor(beanDefinitionRegistry);
			registerReferenceConfigBeanPostProcessor(beanDefinitionRegistry);
			registerReferenceBeanPostProcessor(beanDefinitionRegistry);
		}
	}

	private void registerApplicationConfigBeanPostProcessor(BeanDefinitionRegistry registry) {
		if (!registry.containsBeanDefinition(APPLICATION_CONFIG_BEAN_PROCESSOR_NAME)) {
			RootBeanDefinition beanDefinition =
					new RootBeanDefinition(ApplicationConfigBeanPostProcessor.class);
			beanDefinition.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
			registry.registerBeanDefinition(APPLICATION_CONFIG_BEAN_PROCESSOR_NAME, beanDefinition);
		}
	}

	private void registerReferenceConfigBeanPostProcessor(BeanDefinitionRegistry registry) {
		if (!registry.containsBeanDefinition(REFERENCE_CONFIG_BEAN_PROCESSOR_NAME)) {
			RootBeanDefinition beanDefinition =
					new RootBeanDefinition(ReferenceConfigBeanPostProcessor.class);
			beanDefinition.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
			registry.registerBeanDefinition(REFERENCE_CONFIG_BEAN_PROCESSOR_NAME, beanDefinition);
		}
	}

	private void registerReferenceBeanPostProcessor(BeanDefinitionRegistry registry) {
		if (!registry.containsBeanDefinition(REFERENCE_BEAN_POST_PROCESSOR_NAME)) {
			RootBeanDefinition beanDefinition =
					new RootBeanDefinition(ReferenceBeanPostProcessor.class);
			beanDefinition.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
			registry.registerBeanDefinition(REFERENCE_BEAN_POST_PROCESSOR_NAME, beanDefinition);
		}
	}

}
