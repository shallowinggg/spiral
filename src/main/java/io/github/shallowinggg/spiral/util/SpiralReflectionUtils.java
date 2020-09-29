package io.github.shallowinggg.spiral.util;

import org.springframework.lang.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Map;

import static org.springframework.util.ReflectionUtils.findField;
import static org.springframework.util.ReflectionUtils.getField;
import static org.springframework.util.ReflectionUtils.makeAccessible;

/**
 * @author ding shimin
 */
public final class SpiralReflectionUtils {

    private SpiralReflectionUtils() {
    }

    /**
     * Set the member hold by specified {@link Annotation annotation object} to the
     * specified {@code value}.
     * Note: This implementation depends on the underlying JVM.
     *
     * @param annotation the annotation object which to set member
     * @param member     the name of the annotation member
     * @param value      the value to set (may be {@code null})
     */
    public static void setAnnotationMember(Annotation annotation, String member, @Nullable Object value) {
        InvocationHandler handler = Proxy.getInvocationHandler(annotation);
        Field memberValueField = findField(handler.getClass(), "memberValues");
        assert memberValueField != null;
        makeAccessible(memberValueField);
        @SuppressWarnings("unchecked")
        Map<String, Object> memberValues = (Map<String, Object>) getField(memberValueField, handler);
        if (memberValues != null) {
            memberValues.put(member, value);
        }
    }

}
