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

package io.github.shallowinggg.spiral.loadbalance;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.extension.ExtensionLoader;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.cluster.LoadBalance;
import com.alibaba.dubbo.rpc.cluster.loadbalance.AbstractLoadBalance;
import io.github.shallowinggg.spiral.config.SpiralConstant;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * {@link LoadBalance} implementation used to filter tag-based service provider.
 * <p>
 * The implementation search the candidates which providerUrl is enhanced by
 * {@link io.github.shallowinggg.spiral.spring.ApplicationConfigBeanPostProcessor}. If no found, it
 * will use the original invokers as candidates. Then, it delegates load balance to the fallback
 * {@link LoadBalance} which set by user. If user not set, it will use default load balance provided
 * by dubbo and thus keeps consistent with the original dubbo implementation.
 * <p>
 * Note: Spiral changes the dubbo application name when starting and use it to isolate with other
 * environment instances. At meantime, when dubbo consumer makes a invocation, the url provided by
 * invoker will be override by consumer config. So we should find the original provider url with a
 * little hack (a wrapper class exists in
 * {@link com.alibaba.dubbo.registry.integration.RegistryDirectory} and provided since dubbo 2.2.0).
 * <p>
 * When apache dubbo 2.7, it provides a parameter {@code tag} in url and we can use it instead.
 *
 * @author ding shimin
 * @since 0.1
 */
public class SpiralLoadBalance extends AbstractLoadBalance {

	/**
	 * Store the fallback {@link LoadBalance}s for each
	 * {@link com.alibaba.dubbo.config.annotation.Reference} use.
	 *
	 * <pre>
	 *     key: {provider_interface_name}
	 *     value: {@link com.alibaba.dubbo.config.annotation.Reference#loadbalance()}
	 * </pre>
	 *
	 */
	private final Map<String, LoadBalance> fallBackLbs;

	/**
	 * Default {@link LoadBalance} implementation, provided by {@link Constants#DEFAULT_LOADBALANCE}
	 */
	private final LoadBalance defaultLoadBalance;

	/**
	 * The tag used to distinguish the specified instances from others. It is provided by system
	 * property {@link SpiralConstant#LB_FALLBACK_PROPERTY spiral.tag}.
	 */
	private final String tag;

	public SpiralLoadBalance() {
		this.tag = System.getProperty(SpiralConstant.TAG_PROPERTY);
		this.defaultLoadBalance = ExtensionLoader.getExtensionLoader(LoadBalance.class)
				.getExtension(Constants.DEFAULT_LOADBALANCE);
		Map<String, LoadBalance> fallBackLbs = new HashMap<>();
		String fallbackLBNames = System.getProperty(SpiralConstant.LB_FALLBACK_PROPERTY);
		if (fallbackLBNames != null && !fallbackLBNames.isEmpty()) {
			for (String fallBackLB : fallbackLBNames.split(",")) {
				String[] fallBackLBEntry = fallBackLB.split(":");
				if (fallBackLBEntry.length != 2) {
					continue;
				}

				String interfaceName = fallBackLBEntry[0];
				String fallbackLBName = fallBackLBEntry[1];
				fallBackLbs.put(interfaceName, ExtensionLoader.getExtensionLoader(LoadBalance.class)
						.getExtension(fallbackLBName));
			}
		}
		this.fallBackLbs = fallBackLbs;

	}

	@Override
	protected <T> Invoker<T> doSelect(List<Invoker<T>> invokers, URL url, Invocation invocation) {
		List<Invoker<T>> matchedInvokers = new ArrayList<>();
		for (Invoker<T> invoker : invokers) {
			// a little hack
			// see
			// com.alibaba.dubbo.registry.integration.RegistryDirectory$InvokerDelegate
			Field providerUrlField = ReflectionUtils.findField(invoker.getClass(), "providerUrl");
			if (providerUrlField == null) {
				continue;
			}
			ReflectionUtils.makeAccessible(providerUrlField);
			URL providerUrl = (URL) ReflectionUtils.getField(providerUrlField, invoker);
			if (providerUrl != null) {
				String applicationName = providerUrl.getParameter(Constants.APPLICATION_KEY);
				if (applicationName.endsWith(tag)) {
					matchedInvokers.add(invoker);
				}
			}
		}
		// no specified service provider, fallback
		if (matchedInvokers.isEmpty()) {
			matchedInvokers = invokers;
		}

		LoadBalance fallback = fallBackLbs.getOrDefault(url.getParameter(Constants.INTERFACE_KEY),
				defaultLoadBalance);
		return fallback.select(matchedInvokers, url, invocation);
	}

	/* for test */ Map<String, LoadBalance> getFallBackLbs() {
		return fallBackLbs;
	}
}
