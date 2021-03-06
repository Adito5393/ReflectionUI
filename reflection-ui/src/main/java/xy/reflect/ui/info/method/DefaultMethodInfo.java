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
package xy.reflect.ui.info.method;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.AbstractInfo;
import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.ResourcePath;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.field.GetterFieldInfo;
import xy.reflect.ui.info.parameter.DefaultParameterInfo;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.util.Parameter;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;

public class DefaultMethodInfo extends AbstractInfo implements IMethodInfo {

	protected ReflectionUI reflectionUI;
	protected Method javaMethod;
	protected List<IParameterInfo> parameters;
	protected ITypeInfo returnValueType;
	protected boolean returnValueVoid = false;

	public DefaultMethodInfo(ReflectionUI reflectionUI, Method javaMethod) {
		this.reflectionUI = reflectionUI;
		this.javaMethod = javaMethod;
		resolveJavaReflectionModelAccessProblems();
	}

	@Override
	public String getConfirmationMessage(Object object, InvocationData invocationData) {
		return null;
	}

	@Override
	public String getParametersValidationCustomCaption() {
		return null;
	}

	@Override
	public String getSignature() {
		return ReflectionUIUtils.buildMethodSignature(this);
	}

	protected void resolveJavaReflectionModelAccessProblems() {
		javaMethod.setAccessible(true);
	}

	@Override
	public ResourcePath getIconImagePath() {
		return null;
	}

	@Override
	public boolean isNullReturnValueDistinct() {
		return false;
	}

	@Override
	public boolean isReturnValueDetached() {
		return false;
	}

	@Override
	public boolean isReturnValueIgnored() {
		return false;
	}

	@Override
	public String getCaption() {
		return ReflectionUIUtils.getDefaultMethodCaption(this);
	}

	@Override
	public ITypeInfo getReturnValueType() {
		if (returnValueVoid) {
			return null;
		}
		if (returnValueType == null) {
			if (javaMethod.getReturnType() == void.class) {
				returnValueVoid = true;
			} else {
				returnValueType = reflectionUI
						.getTypeInfo(new JavaTypeInfoSource(javaMethod.getReturnType(), javaMethod, -1, null));
			}
		}
		return returnValueType;
	}

	@Override
	public List<IParameterInfo> getParameters() {
		if (parameters == null) {
			parameters = new ArrayList<IParameterInfo>();
			Class<?>[] parameterTypes = javaMethod.getParameterTypes();
			for (int i = 0; i < parameterTypes.length; i++) {
				Parameter javaParameter = new Parameter(javaMethod, i);
				if (!DefaultParameterInfo.isCompatibleWith(javaParameter)) {
					continue;
				}
				parameters.add(new DefaultParameterInfo(reflectionUI, javaParameter));
			}
		}
		return parameters;
	}

	@Override
	public Object invoke(Object object, InvocationData invocationData) {
		Object[] args = new Object[javaMethod.getParameterTypes().length];
		for (IParameterInfo param : getParameters()) {
			args[param.getPosition()] = invocationData.getParameterValue(param.getPosition());
		}
		try {
			return javaMethod.invoke(object, args);
		} catch (IllegalAccessException e) {
			throw new ReflectionUIError(e);
		} catch (IllegalArgumentException e) {
			throw new ReflectionUIError(e);
		} catch (InvocationTargetException e) {
			throw new ReflectionUIError(e.getTargetException());
		}
	}

	public static String getDescription(IMethodInfo method) {
		StringBuilder result = new StringBuilder(method.getCaption());
		if (method.getParameters().size() > 0) {
			result.append(" - specify ");
			result.append(ReflectionUIUtils.formatParameterList(method.getParameters()));
		}
		return result.toString();
	}

	@Override
	public String getName() {
		return javaMethod.getName();
	}

	@Override
	public boolean isHidden() {
		if (Modifier.isStatic(javaMethod.getModifiers())) {
			if (javaMethod.getReturnType().equals(Void.TYPE)) {
				if ("main".equals(javaMethod.getName())) {
					if (Arrays.equals(javaMethod.getParameterTypes(), new Class<?>[] { String[].class })) {
						return true;
					}
				}
			}
		}
		return false;
	}

	@Override
	public void onControlVisibilityChange(Object object, boolean b) {
	}

	@Override
	public boolean isReadOnly() {
		return Modifier.isStatic(javaMethod.getModifiers());
	}

	@Override
	public String getNullReturnValueLabel() {
		return null;
	}

	@Override
	public ValueReturnMode getValueReturnMode() {
		return ValueReturnMode.INDETERMINATE;
	}

	public static boolean isCompatibleWith(Method javaMethod, Class<?> containingJavaClass) {
		if (javaMethod.isSynthetic()) {
			return false;
		}
		if (javaMethod.isBridge()) {
			return false;
		}
		if (GetterFieldInfo.isCompatibleWith(javaMethod, containingJavaClass)) {
			return false;
		}
		for (Method otherJavaMethod : containingJavaClass.getMethods()) {
			if (!otherJavaMethod.equals(javaMethod)) {
				if (GetterFieldInfo.isCompatibleWith(otherJavaMethod, containingJavaClass)) {
					if (javaMethod.equals(GetterFieldInfo.getValidSetterMethod(otherJavaMethod, containingJavaClass))) {
						return false;
					}
				}
			}
		}
		for (Method commonMethod : Object.class.getMethods()) {
			if (ReflectionUIUtils.isOverridenBy(commonMethod, javaMethod)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public InfoCategory getCategory() {
		return null;
	}

	@Override
	public String getOnlineHelp() {
		return null;
	}

	@Override
	public Runnable getNextInvocationUndoJob(Object object, InvocationData invocationData) {
		return null;
	}

	@Override
	public void validateParameters(Object object, InvocationData invocationData) throws Exception {
	}

	@Override
	public Map<String, Object> getSpecificProperties() {
		return Collections.emptyMap();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((javaMethod == null) ? 0 : javaMethod.hashCode());
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
		DefaultMethodInfo other = (DefaultMethodInfo) obj;
		if (javaMethod == null) {
			if (other.javaMethod != null)
				return false;
		} else if (!javaMethod.equals(other.javaMethod))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "DefaultMethodInfo [javaMethod=" + javaMethod + "]";
	}

}
