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
