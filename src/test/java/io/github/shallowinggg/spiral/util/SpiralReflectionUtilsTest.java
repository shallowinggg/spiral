package io.github.shallowinggg.spiral.util;

import io.github.shallowinggg.spiral.annotation.AB;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author ding shimin
 */
public class SpiralReflectionUtilsTest {

    @Test
    public void testSetAnnotationValue() {
        AB annotation = AnnotatedClass.class.getAnnotation(AB.class);
        Assert.assertNotNull(annotation);
        Assert.assertEquals("1", annotation.val());
        SpiralReflectionUtils.setAnnotationMember(annotation, "val", "2");
        Assert.assertEquals("2", annotation.val());
    }

    @AB(val = "1")
    private static class AnnotatedClass {
    }
}
