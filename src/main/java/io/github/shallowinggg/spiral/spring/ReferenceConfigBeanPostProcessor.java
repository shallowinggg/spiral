package io.github.shallowinggg.spiral.spring;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.utils.ConcurrentHashSet;
import com.alibaba.dubbo.config.annotation.Reference;
import io.github.shallowinggg.spiral.config.SpiralConstant;
import io.github.shallowinggg.spiral.util.SpiralReflectionUtils;
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
import java.util.Set;

import static org.springframework.core.BridgeMethodResolver.findBridgedMethod;
import static org.springframework.core.BridgeMethodResolver.isVisibilityBridgeMethodPair;
import static org.springframework.core.annotation.AnnotationUtils.findAnnotation;
import static org.springframework.core.annotation.AnnotationUtils.getAnnotation;

/**
 * {@link BeanPostProcessor} implementation used to record the load balance for dubbo reference interfaces.
 * <p>
 * When spring context refreshed successfully, the records will be set into system property with
 * the key {@link SpiralConstant#LB_FALLBACK_PROPERTY}. The representation for record is
 * {@code <interface_name1>:<load_balance_name1>,<interface_name2>:<load_balance_name2>,...}.
 * <p>
 * This records is used by {@link io.github.shallowinggg.spiral.loadbalance.SpiralLoadBalance}. See it
 * for more information.
 * <p>
 * Note: method level configuration for load balance is not supported.
 *
 * @author ding shimin
 * @see io.github.shallowinggg.spiral.loadbalance.SpiralLoadBalance
 */
public class ReferenceConfigBeanPostProcessor
        implements MergedBeanDefinitionPostProcessor, ApplicationListener<ContextRefreshedEvent>, PriorityOrdered {

    private final StringBuffer lbFallbackBuilder = new StringBuffer();

    private final Set<String> handledBean = new ConcurrentHashSet<>(256);

    @Override
    public int getOrder() {
        // this order need to be higher than com.alibaba.dubbo.config.spring.beans.factory.annotation.ReferenceAnnotationBeanPostProcessor
        // luckily, its order is the lowest until now.
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
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public void postProcessMergedBeanDefinition(RootBeanDefinition beanDefinition, Class<?> beanType, String beanName) {
        interceptReferenceMetadata(beanName, beanType);
    }

    private void interceptReferenceMetadata(String beanName, Class<?> clazz) {
        // Fall back to class name as cache key, for backwards compatibility with custom callers.
        String cacheKey = (StringUtils.hasLength(beanName) ? beanName : clazz.getName());
        // Quick check on the concurrent map first, with minimal locking.
        if (!handledBean.contains(cacheKey)) {
            synchronized (handledBean) {
                if (!handledBean.contains(cacheKey)) {
                    interceptFieldReferenceMetadata(clazz);
                    interceptMethodReferenceMetadata(clazz);
                    handledBean.add(cacheKey);
                }
            }
        }
    }

    /**
     * Find annotated {@link Reference @Reference} fields and use {@link SpiralConstant#SPIRAL_LOAD_BALANCE_NAME}
     * to override its original load balance setting.
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

                String lbName = reference.loadbalance();
                String beanClassName = beanClass.getName();
                String interfaceName = field.getType().getName();
                String entry = beanClassName + "#" + interfaceName + ":" + lbName + ",";
                lbFallbackBuilder.append(entry);
                // use spiral load balance instead
                SpiralReflectionUtils.setAnnotationMember(reference, Constants.LOADBALANCE_KEY,
                        SpiralConstant.SPIRAL_LOAD_BALANCE_NAME);
            }
        });

    }

    /**
     * Find annotated {@link Reference @Reference} methods and use {@link SpiralConstant#SPIRAL_LOAD_BALANCE_NAME}
     * to override its original load balance setting.
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
            if (reference != null && method.equals(ClassUtils.getMostSpecificMethod(method, beanClass))) {
                if (Modifier.isStatic(method.getModifiers())) {
                    return;
                }

                PropertyDescriptor pd = BeanUtils.findPropertyForMethod(bridgedMethod, beanClass);
                if (pd != null) {
                    String lbName = reference.loadbalance();
                    String beanClassName = beanClass.getName();
                    String interfaceName = pd.getPropertyType().getName();
                    String entry = beanClassName + "#" + interfaceName + ":" + lbName + ",";
                    lbFallbackBuilder.append(entry);
                    // use spiral load balance instead
                    SpiralReflectionUtils.setAnnotationMember(reference, Constants.LOADBALANCE_KEY,
                            SpiralConstant.SPIRAL_LOAD_BALANCE_NAME);
                }
            }
        });

    }
}