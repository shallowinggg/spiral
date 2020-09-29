package io.github.shallowinggg.spiral.config;

/**
 * @author ding shimin
 */
public final class SpiralConstant {

    /**
     * System property used to enable spiral.
     * You can use {@code "true"} or {@code "yes"} or {@code "on"} or
     * {@code "1"} to enable it.
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
