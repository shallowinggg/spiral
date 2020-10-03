/*
 *    Copyright 2020 the original author or authors.
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
package io.github.shallowinggg.spiral.spring;

import com.alibaba.dubbo.config.ApplicationConfig;
import io.github.shallowinggg.spiral.config.SpiralConstant;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.util.Assert;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * {@link BeanPostProcessor} implementation used to intercept {@link ApplicationConfig}.
 * <p>
 * Any {@link ApplicationConfig} instance will be modified name, for appending user specified tag. For example: origin
 *
 * <pre>
 * application name: demo-provider
 * tag: red
 * ==>
 * enhanced application name: demo-provider-red
 * </pre>
 *
 * @author ding shimin
 */
public class ApplicationConfigBeanPostProcessor implements BeanPostProcessor, EnvironmentAware {

    /**
     * The rule qualification for <b>name</b>
     */
    private static final Pattern PATTERN_NAME = Pattern.compile("[\\-._0-9a-zA-Z]+");

    private final Log log = LogFactory.getLog(getClass());

    private String tag;

    @Override
    public void setEnvironment(Environment environment) {
        String tag = environment.getProperty(SpiralConstant.TAG_PROPERTY);
        setTag(tag);
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof ApplicationConfig) {
            ApplicationConfig applicationConfig = (ApplicationConfig) bean;
            String name = applicationConfig.getName();
            String enhancedName = name + "-" + tag;
            applicationConfig.setName(enhancedName);
            if (log.isDebugEnabled()) {
                log.debug(String.format("Dubbo application name '%s' is changed to '%s'", name, enhancedName));
            }
        }
        return bean;
    }

    public void setTag(String tag) {
        Assert.hasText(tag, "tag must has text");
        Matcher matcher = PATTERN_NAME.matcher(tag);
        if (!matcher.matches()) {
            throw new IllegalStateException("Invalid tag \"" + tag + "\" contains illegal "
                    + "character, only digit, letter, '-', '_' or '.' is legal.");
        }
        this.tag = tag;
    }
}
