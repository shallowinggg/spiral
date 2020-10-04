/*
 *    Copyright © 2020 the original author or authors.
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

package io.github.shallowinggg.spiral.config;

/**
 * @author ding shimin
 * @since 0.1
 */
public final class SpiralConstant {

	/**
	 * System property used to enable spiral. You can use {@code "true"} or {@code "yes"}
	 * or {@code "on"} or {@code "1"} to enable it.
	 */
	public static final String ENABLE_PROPERTY = "spiral.enable";

	/**
	 * System property used to specify the tag for dubbo application name.
	 *
	 * @see io.github.shallowinggg.spiral.spring.ApplicationConfigBeanPostProcessor
	 * @see io.github.shallowinggg.spiral.loadbalance.SpiralLoadBalance
	 */
	public static final String TAG_PROPERTY = "spiral.tag";

	/**
	 * System property used to record the original load balance name.
	 *
	 * @see io.github.shallowinggg.spiral.spring.ReferenceConfigBeanPostProcessor
	 * @see io.github.shallowinggg.spiral.loadbalance.SpiralLoadBalance
	 */
	public static final String LB_FALLBACK_PROPERTY = "spiral.lb.fallback";

	/**
	 * Name for {@link io.github.shallowinggg.spiral.loadbalance.SpiralLoadBalance}.
	 */
	public static final String SPIRAL_LOAD_BALANCE_NAME = "spiral";

}
