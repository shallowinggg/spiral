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

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.config.annotation.Reference;
import io.github.shallowinggg.spiral.config.SpiralConstant;
import io.github.shallowinggg.spiral.util.SpiralReflectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.support.MergedBeanDefinitionPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.PriorityOrdered;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.springframework.core.BridgeMethodResolver.findBridgedMethod;
import static org.springframework.core.BridgeMethodResolver.isVisibilityBridgeMethodPair;
import static org.springframework.core.annotation.AnnotationUtils.findAnnotation;
import static org.springframework.core.annotation.AnnotationUtils.getAnnotation;

/**
 * {@link BeanPostProcessor} implementation used to record the load balance for dubbo reference
 * interfaces and replace it with spiral load balance. This implementation is used when dubbo
 * version is higher than {@literal 2.5.6}.
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
 * @since 0.1
 */
public class ReferenceConfigBeanPostProcessor implements MergedBeanDefinitionPostProcessor,
		ApplicationListener<ContextRefreshedEvent>, PriorityOrdered {

	private final Log log = LogFactory.getLog(getClass());

	private final StringBuilder lbFallbackBuilder = new StringBuilder();

	private final Map<String, Boolean> handledBeans = new ConcurrentHashMap<>(256);

	/**
	 * Record whether the service interface has been handled. It assumes that for same service
	 * provider, the {@link Reference} annotation should use the same load balance.
	 *
	 * The initial size is 32, for application should not import too many service dependencies.
	 */
	private final Map<String, Boolean> handledServiceInterfaces = new ConcurrentHashMap<>(32);

	@Override
	public int getOrder() {
		// this order need to be higher than
		// com.alibaba.dubbo.config.spring.beans.factory.annotation.ReferenceAnnotationBeanPostProcessor.
		// Luckily, its order is the lowest now.
		return 0;
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
		// remove the last unnecessary comma
		if (lbFallbackBuilder.length() > 0) {
			lbFallbackBuilder.setLength(lbFallbackBuilder.length() - 1);
		}
		System.setProperty(SpiralConstant.LB_FALLBACK_PROPERTY, lbFallbackBuilder.toString());
		// clear
		lbFallbackBuilder.setLength(0);
	}

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName)
			throws BeansException {
		return bean;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName)
			throws BeansException {
		return bean;
	}

	@Override
	public void postProcessMergedBeanDefinition(RootBeanDefinition beanDefinition,
			Class<?> beanType, String beanName) {
		interceptReferenceMetadata(beanName, beanType);
	}

	private void interceptReferenceMetadata(String beanName, Class<?> clazz) {
		// Fall back to class name as cache key, for backwards compatibility with custom
		// callers.
		String cacheKey = StringUtils.hasLength(beanName) ? beanName : clazz.getName();
		// Quick check on the concurrent map first, with minimal locking.
		if (!handledBeans.containsKey(cacheKey)) {
			synchronized (handledBeans) {
				if (!handledBeans.containsKey(cacheKey)) {
					interceptFieldReferenceMetadata(clazz);
					interceptMethodReferenceMetadata(clazz);
					handledBeans.put(cacheKey, Boolean.TRUE);
				}
			}
		}
	}

	/**
	 * Find annotated {@link Reference @Reference} fields and use
	 * {@link SpiralConstant#SPIRAL_LOAD_BALANCE_NAME} to override its original load balance
	 * setting.
	 *
	 * @param beanClass The {@link Class} of Bean
	 */
	private void interceptFieldReferenceMetadata(final Class<?> beanClass) {
		ReflectionUtils.doWithFields(beanClass, field -> {
			Reference reference = getAnnotation(field, Reference.class);
			if (reference != null) {
				if (Modifier.isStatic(field.getModifiers())) {
					return;
				}

				String interfaceName = field.getType().getName();
				handleReference(reference, interfaceName);
			}
		});

	}

	/**
	 * Find annotated {@link Reference @Reference} methods and use
	 * {@link SpiralConstant#SPIRAL_LOAD_BALANCE_NAME} to override its original load balance
	 * setting.
	 *
	 * @param beanClass The {@link Class} of Bean
	 */
	private void interceptMethodReferenceMetadata(final Class<?> beanClass) {
		ReflectionUtils.doWithMethods(beanClass, method -> {
			Method bridgedMethod = findBridgedMethod(method);
			if (!isVisibilityBridgeMethodPair(method, bridgedMethod)) {
				return;
			}

			Reference reference = findAnnotation(bridgedMethod, Reference.class);
			if (reference != null
					&& method.equals(ClassUtils.getMostSpecificMethod(method, beanClass))) {
				if (Modifier.isStatic(method.getModifiers())) {
					return;
				}

				PropertyDescriptor pd = BeanUtils.findPropertyForMethod(bridgedMethod, beanClass);
				if (pd != null) {
					String interfaceName = pd.getPropertyType().getName();
					handleReference(reference, interfaceName);
				}
			}
		});

	}

	/**
	 * Record the user specified load balance and replace it with spiral load balance. If use
	 * default load balance, it won't be recorded for memory cost reduction.
	 *
	 * @param reference the {@link Reference} object
	 * @param serviceInterface the name of service interface
	 * @see #handledServiceInterfaces
	 */
	private void handleReference(Reference reference, String serviceInterface) {
		Boolean val = handledServiceInterfaces.putIfAbsent(serviceInterface, Boolean.TRUE);
		String lbName = reference.loadbalance();
		// filter default load balance
		if (val == null && !"".equals(lbName)) {
			String entry = serviceInterface + ":" + lbName + ",";
			lbFallbackBuilder.append(entry);
		}

		// use spiral load balance instead
		SpiralReflectionUtils.setAnnotationMember(reference, Constants.LOADBALANCE_KEY,
				SpiralConstant.SPIRAL_LOAD_BALANCE_NAME);
		if (log.isDebugEnabled()) {
			log.debug(String.format("Replace load balance for [%s] with spiral", serviceInterface));
		}
	}

}
