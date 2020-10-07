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
import io.github.shallowinggg.spiral.config.TestConfig;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * @author ding shimin
 */
public class SpiralApplicationConfigRegistrarTest {

	@Test
	public void testRegister() {
		System.setProperty(SpiralConstant.ENABLE_PROPERTY, "on");
		System.setProperty(SpiralConstant.TAG_PROPERTY, "red");
		ApplicationContext ac = new AnnotationConfigApplicationContext(TestConfig.class);
		Assert.assertTrue(ac.containsBeanDefinition(
				"io.github.shallowinggg.spiral.spring.ApplicationConfigBeanPostProcessor"));
		Assert.assertTrue(ac.containsBeanDefinition(
				"io.github.shallowinggg.spiral.spring.ReferenceConfigBeanPostProcessor"));
	}
}
