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

import com.alibaba.dubbo.config.annotation.Reference;
import io.github.shallowinggg.spiral.config.DemoProvider;
import io.github.shallowinggg.spiral.config.DemoProvider2;
import io.github.shallowinggg.spiral.config.SpiralConstant;
import io.github.shallowinggg.spiral.config.TestConfig;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;

/**
 * @author ding shimin
 */
public class ReferenceConfigBeanPostProcessorTest {

	@Test
	public void testPostProcessMergedBeanDefinition() {
		ReferenceConfigBeanPostProcessor postProcessor = new ReferenceConfigBeanPostProcessor();
		postProcessor.postProcessMergedBeanDefinition(null, ReferenceConfig.class,
				"referenceConfig");

		Field demoProviderField = ReflectionUtils.findField(ReferenceConfig.class, "demoProvider");
		Reference reference = AnnotationUtils.getAnnotation(demoProviderField, Reference.class);
		Assert.assertEquals("", reference.loadbalance());

		Field demoProvider2Field =
				ReflectionUtils.findField(ReferenceConfig.class, "demoProvider2");
		Reference reference2 = AnnotationUtils.getAnnotation(demoProvider2Field, Reference.class);
		Assert.assertEquals(SpiralConstant.SPIRAL_LOAD_BALANCE_NAME, reference2.loadbalance());
	}

	@Test
	public void testOnApplicationEvent() {
		ApplicationContext ac = Mockito.mock(GenericApplicationContext.class);
		ReferenceConfigBeanPostProcessor postProcessor = new ReferenceConfigBeanPostProcessor();
		postProcessor.postProcessMergedBeanDefinition(null, ReferenceConfig2.class, "testConfig");
		postProcessor.onApplicationEvent(new ContextRefreshedEvent(ac));
		Assert.assertEquals(
				"io.github.shallowinggg.spiral.spring.ReferenceConfigBeanPostProcessorTest$ReferenceConfig2"
						+ "#io.github.shallowinggg.spiral.config.DemoProvider2:random",
				System.getProperty(SpiralConstant.LB_FALLBACK_PROPERTY));
	}

	@Test
	public void testStartWithApplicationContext() {
		System.setProperty(SpiralConstant.ENABLE_PROPERTY, "on");
		System.setProperty(SpiralConstant.TAG_PROPERTY, "red");
		new AnnotationConfigApplicationContext(TestConfig.class);
		Assert.assertEquals(
				"io.github.shallowinggg.spiral.config.TestConfig$ReferenceContainer"
						+ "#io.github.shallowinggg.spiral.config.DemoProvider2:random",
				System.getProperty(SpiralConstant.LB_FALLBACK_PROPERTY));
	}


	// the annotations is class-based at runtime, so for every test
	// we should create a container to store.

	static class ReferenceConfig {
		@Reference
		@SuppressWarnings("unused")
		private DemoProvider demoProvider;

		@Reference(loadbalance = "random")
		@SuppressWarnings("unused")
		private DemoProvider2 demoProvider2;
	}

	static class ReferenceConfig2 {
		@Reference
		@SuppressWarnings("unused")
		private DemoProvider demoProvider;

		@Reference(loadbalance = "random")
		@SuppressWarnings("unused")
		private DemoProvider2 demoProvider2;
	}

}
