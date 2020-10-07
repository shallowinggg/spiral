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

package io.github.shallowinggg.spiral.config;

import com.alibaba.dubbo.config.ApplicationConfig;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.shallowinggg.spiral.spring.EnableSpiral;

/**
 * @author ding shimin
 */
@Configuration
@EnableSpiral
public class ApplicationTestConfig {

	@Bean
	public ApplicationConfig applicationConfig() {
		return new ApplicationConfig("demo-provider");
	}
}
