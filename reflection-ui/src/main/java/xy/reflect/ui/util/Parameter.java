/*******************************************************************************
 * Copyright (C) 2018 OTK Software
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * The license allows developers and companies to use and integrate a software 
 * component released under the LGPL into their own (even proprietary) software 
 * without being required by the terms of a strong copyleft license to release the 
 * source code of their own components. However, any developer who modifies 
 * an LGPL-covered component is required to make their modified version 
 * available under the same LGPL license. For proprietary software, code under 
 * the LGPL is usually used in the form of a shared library, so that there is a clear 
 * separation between the proprietary and LGPL components.
 * 
 * The GNU Lesser General Public License allows you also to freely redistribute the 
 * libraries under the same license, if you provide the terms of the GNU Lesser 
 * General Public License with them and add the following copyright notice at the 
 * appropriate place (with a link to http://javacollection.net/reflectionui/ web site 
 * when possible).
 ******************************************************************************/
package xy.reflect.ui.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

public class Parameter extends AccessibleObject {

	public static final String NO_NAME = "";
	
	private final Member invokable;
	private final int position;
	private Class<?>[] invokableParameterTypes;
	private Annotation[][] invokableParameterAnnotations;
	
	public Parameter(Member invokable, int position) {
		this.invokable = invokable;
		this.position = position;
		if (invokable instanceof Method) {
			Method method = (Method) invokable;
			this.invokableParameterTypes = method.getParameterTypes();
			this.invokableParameterAnnotations = method.getParameterAnnotations();
		} else if (invokable instanceof Constructor) {
			Constructor<?> constructor = (Constructor<?>) invokable;
			this.invokableParameterTypes = constructor.getParameterTypes();
			this.invokableParameterAnnotations = constructor.getParameterAnnotations();
		} else {
			throw new ReflectionUIError();
		}
	}

	public Class<?> getType() {
		return invokableParameterTypes[position];
	}

	public Member getDeclaringInvokable() {
		return invokable;
	}

	public int getPosition() {
		return position;
	}

	public Class<?>[] getDeclaringInvokableParameterTypes() {
		return invokableParameterTypes;
	}

	public Annotation[][] getDeclaringInvokableParameterAnnotations() {
		return invokableParameterAnnotations;
	}

	public String getName() {
		return NO_NAME;
	}

	@Override
	public boolean isAnnotationPresent(Class<? extends Annotation> annotationType) {
		return getAnnotation(annotationType) != null;
	}

	@Override
	public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
		for (Annotation annotation : invokableParameterAnnotations[position]) {
			if (annotationType.isInstance(annotation)) {
				return annotationType.cast(annotation);
			}
		}
		return null;
	}

	@Override
	public Annotation[] getAnnotations() {
		return getDeclaredAnnotations();
	}

	@Override
	public Annotation[] getDeclaredAnnotations() {
		return invokableParameterAnnotations[position];
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((invokable == null) ? 0 : invokable.hashCode());
		result = prime * result + position;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Parameter other = (Parameter) obj;
		if (invokable == null) {
			if (other.invokable != null)
				return false;
		} else if (!invokable.equals(other.invokable))
			return false;
		if (position != other.position)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getType() + " arg" + position;
	}

}
