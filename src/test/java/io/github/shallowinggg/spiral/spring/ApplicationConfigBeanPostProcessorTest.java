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

import com.alibaba.dubbo.config.ApplicationConfig;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import io.github.shallowinggg.spiral.config.ApplicationTestConfig;
import io.github.shallowinggg.spiral.config.SpiralConstant;

/**
 * @author ding shimin
 */
public class ApplicationConfigBeanPostProcessorTest {

	@Test
	public void testPostProcess() {
		ApplicationConfig applicationConfig = new ApplicationConfig("demo-provider");
		ApplicationConfigBeanPostProcessor postProcessor = new ApplicationConfigBeanPostProcessor();
		postProcessor.setTag("red");
		postProcessor.postProcessAfterInitialization(applicationConfig, "applicationConfig");
		Assert.assertEquals("demo-provider-red", applicationConfig.getName());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInvalidTag() {
		ApplicationConfigBeanPostProcessor postProcessor = new ApplicationConfigBeanPostProcessor();
		postProcessor.setTag("#read");
	}

	@Test
	public void testStartWithApplicationContext() {
		System.setProperty(SpiralConstant.ENABLE_PROPERTY, "on");
		System.setProperty(SpiralConstant.TAG_PROPERTY, "red");
		AnnotationConfigApplicationContext ac =
				new AnnotationConfigApplicationContext(ApplicationTestConfig.class);
		ApplicationConfig config = ac.getBean(ApplicationConfig.class);
		Assert.assertEquals("demo-provider-red", config.getName());
	}
}
