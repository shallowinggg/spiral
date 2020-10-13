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

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.RpcInvocation;
import com.alibaba.dubbo.rpc.cluster.LoadBalance;
import com.alibaba.dubbo.rpc.protocol.InvokerWrapper;
import com.alibaba.dubbo.rpc.protocol.dubbo.DubboInvoker;
import io.github.shallowinggg.spiral.config.DemoProvider;
import io.github.shallowinggg.spiral.config.SpiralConstant;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

/**
 * @author ding shimin
 */
public class SpiralLoadBalanceTest {

	@Test
	public void testConstruction() {
		System.setProperty(SpiralConstant.TAG_PROPERTY, "red");
		System.setProperty(SpiralConstant.LB_FALLBACK_PROPERTY,
				"io.github.shallowinggg.spiral.config.DemoProvider:roundrobin");
		SpiralLoadBalance loadBalance = new SpiralLoadBalance();
		Assert.assertEquals(1, loadBalance.getFallBackLbs().size());

		System.setProperty(SpiralConstant.LB_FALLBACK_PROPERTY, "");
		loadBalance = new SpiralLoadBalance();
		Assert.assertEquals(0, loadBalance.getFallBackLbs().size());
	}

	@Test
	public void testSelect() {
		System.setProperty(SpiralConstant.TAG_PROPERTY, "red");
		System.setProperty(SpiralConstant.LB_FALLBACK_PROPERTY,
				"io.github.shallowinggg.spiral.config.DemoProvider:roundrobin");
		LoadBalance loadBalance = new SpiralLoadBalance();

		URL url = URL
				.valueOf("dubbo://127.0.0.1:9090/io.github.shallowinggg.spiral.config.DemoProvider?"
						+ "application=demo-consumer-red&"
						+ "interface=io.github.shallowinggg.spiral.config.DemoProvider");
		Invoker<DemoProvider> invoker = new InvokerDelegete<>(
				new DubboInvoker<>(DemoProvider.class, url, null), url,
				URL.valueOf(
						"dubbo://192.168.1.7:9090/io.github.shallowinggg.spiral.config.DemoProvider?"
								+ "application=demo-provider-red&"
								+ "interface=io.github.shallowinggg.spiral.config.DemoProvider"));

		Invoker<DemoProvider> invoker2 = new InvokerDelegete<>(
				new DubboInvoker<>(DemoProvider.class, url, null), url,
				URL.valueOf(
						"dubbo://192.168.1.8:9090/io.github.shallowinggg.spiral.config.DemoProvider?"
								+ "application=demo-provider-red&"
								+ "interface=io.github.shallowinggg.spiral.config.DemoProvider"));

		Invoker<DemoProvider> invoker3 = new InvokerDelegete<>(
				new DubboInvoker<>(DemoProvider.class, url, null), url,
				URL.valueOf(
						"dubbo://192.168.1.9:9090/io.github.shallowinggg.spiral.config.DemoProvider?"
								+ "application=demo-provider&"
								+ "interface=io.github.shallowinggg.spiral.config.DemoProvider"));

		List<Invoker<DemoProvider>> invokers = Arrays.asList(invoker, invoker2, invoker3);
		Invocation invocation = new RpcInvocation("foo", null, null);
		InvokerDelegete<DemoProvider> result =
				(InvokerDelegete<DemoProvider>) loadBalance.select(invokers, url, invocation);
		Assert.assertEquals("192.168.1.7", result.getProviderUrl().getHost());
		result = (InvokerDelegete<DemoProvider>) loadBalance.select(invokers, url, invocation);
		Assert.assertEquals("192.168.1.8", result.getProviderUrl().getHost());
		result = (InvokerDelegete<DemoProvider>) loadBalance.select(invokers, url, invocation);
		Assert.assertEquals("192.168.1.7", result.getProviderUrl().getHost());
		result = (InvokerDelegete<DemoProvider>) loadBalance.select(invokers, url, invocation);
		Assert.assertEquals("192.168.1.8", result.getProviderUrl().getHost());
	}


	private static class InvokerDelegete<T> extends InvokerWrapper<T> {
		private final URL providerUrl;

		public InvokerDelegete(Invoker<T> invoker, URL url, URL providerUrl) {
			super(invoker, url);
			this.providerUrl = providerUrl;
		}

		public URL getProviderUrl() {
			return this.providerUrl;
		}
	}
}
