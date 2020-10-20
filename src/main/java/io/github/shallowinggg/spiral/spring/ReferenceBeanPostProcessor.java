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

import com.alibaba.dubbo.config.spring.ReferenceBean;
import io.github.shallowinggg.spiral.config.SpiralConstant;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.PriorityOrdered;
import org.springframework.util.StringUtils;

/**
 * {@link BeanPostProcessor} implementation used to record the load balance for dubbo reference
 * interfaces and replace it with spiral load balance. This implementation is used for xml
 * configuration.
 * <p>
 * When spring context refreshed successfully, the records will be set into system property with the
 * key {@link SpiralConstant#LB_FALLBACK_PROPERTY}. The representation for record is
 * {@code <interface_name1>:<load_balance_name1>,<interface_name2>:<load_balance_name2>,...}.
 * <p>
 * This records is used by {@link io.github.shallowinggg.spiral.loadbalance.SpiralLoadBalance}. See
 * it for more information.
 * <p>
 * Note: method level configuration for load balance is not supported.
 *
 * @author ding shimin
 * @see io.github.shallowinggg.spiral.loadbalance.SpiralLoadBalance
 * @see io.github.shallowinggg.spiral.spring.ReferenceConfigBeanPostProcessor
 * @since 0.1
 */
public class ReferenceBeanPostProcessor
		implements BeanPostProcessor, PriorityOrdered, ApplicationListener<ContextRefreshedEvent> {

	private final Log log = LogFactory.getLog(getClass());

	private final StringBuilder lbFallbackBuilder = new StringBuilder();

	@Override
	public int getOrder() {
		return 0;
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
		// remove the last unnecessary comma
		if (lbFallbackBuilder.length() > 0) {
			lbFallbackBuilder.setLength(lbFallbackBuilder.length() - 1);
		}

		// merge property from ReferenceConfigBeanPostProcessor
		String property = System.getProperty(SpiralConstant.LB_FALLBACK_PROPERTY);
		String lbFallback = lbFallbackBuilder.toString();
		if (!StringUtils.isEmpty(property)) {
			if (lbFallbackBuilder.length() > 0) {
				property = property + "," + lbFallback;
			}
		} else {
			property = lbFallback;
		}
		System.setProperty(SpiralConstant.LB_FALLBACK_PROPERTY, property);
		// clear
		lbFallbackBuilder.setLength(0);
	}

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName)
			throws BeansException {
		if (bean instanceof ReferenceBean<?>) {
			ReferenceBean<?> referenceBean = (ReferenceBean<?>) bean;
			String serviceInterface = referenceBean.getInterface();
			String lbName = referenceBean.getLoadbalance();
			// filter default load balance
			if (!"".equals(lbName)) {
				String entry = serviceInterface + ":" + lbName + ",";
				lbFallbackBuilder.append(entry);
			}
			referenceBean.setLoadbalance(SpiralConstant.SPIRAL_LOAD_BALANCE_NAME);

			if (log.isDebugEnabled()) {
				log.debug(String.format("Replace load balance for [%s] with spiral",
						serviceInterface));
			}
		}
		return bean;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName)
			throws BeansException {
		return bean;
	}
}
