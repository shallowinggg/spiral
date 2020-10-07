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

import static org.springframework.util.ReflectionUtils.findField;
import static org.springframework.util.ReflectionUtils.getField;
import static org.springframework.util.ReflectionUtils.makeAccessible;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Map;
import javax.annotation.Nullable;

/**
 * @author ding shimin
 * @since 0.1
 */
public final class SpiralReflectionUtils {

	private SpiralReflectionUtils() {
	}

	/**
	 * Set the member hold by specified {@link Annotation annotation object} to the specified
	 * {@code value}. Note: This implementation depends on the underlying JVM.
	 *
	 * @param annotation the annotation object which to set member
	 * @param member the name of the annotation member
	 * @param value the value to set (may be {@code null})
	 */
	public static void setAnnotationMember(Annotation annotation, String member,
			@Nullable Object value) {
		InvocationHandler handler = Proxy.getInvocationHandler(annotation);
		Field memberValueField = findField(handler.getClass(), "memberValues");
		assert memberValueField != null;
		makeAccessible(memberValueField);
		@SuppressWarnings("unchecked")
		Map<String, Object> memberValues =
				(Map<String, Object>) getField(memberValueField, handler);
		if (memberValues != null) {
			memberValues.put(member, value);
		}
	}

}
