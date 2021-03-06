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
package xy.reflect.ui.info.field;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.AbstractInfo;
import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.method.DefaultMethodInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.method.InvocationData;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.info.type.source.SpecificitiesIdentifier;
import xy.reflect.ui.info.type.source.TypeInfoSourceProxy;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;

public class GetterFieldInfo extends AbstractInfo implements IFieldInfo {

	public static final Pattern GETTER_PATTERN = Pattern.compile("^(?:get|is|has)([A-Z].*)");

	protected ReflectionUI reflectionUI;
	protected Method javaGetterMethod;
	protected Class<?> containingJavaClass;
	protected ITypeInfo type;
	protected IMethodInfo setterMethodInfo;
	protected String name;

	public GetterFieldInfo(ReflectionUI reflectionUI, Method javaGetterMethod, Class<?> containingJavaClass) {
		this.reflectionUI = reflectionUI;
		this.javaGetterMethod = javaGetterMethod;
		this.containingJavaClass = containingJavaClass;
		resolveJavaReflectionModelAccessProblems();
	}

	public static String getFieldName(String getterMethodName) {
		Matcher m = GETTER_PATTERN.matcher(getterMethodName);
		if (!m.matches()) {
			return null;
		}
		String result = m.group(1);
		if (result != null) {
			result = ReflectionUIUtils.changeCase(result, false, 0, 1);
		}
		return result;
	}

	public static Method getValidSetterMethod(Method javaGetterMethod, Class<?> containingJavaClass) {
		String fieldName = getFieldName(javaGetterMethod.getName());
		String setterMethodName = "set" + ReflectionUIUtils.changeCase(fieldName, true, 0, 1);
		Method result;
		try {
			result = containingJavaClass.getMethod(setterMethodName, new Class[] { javaGetterMethod.getReturnType() });
		} catch (NoSuchMethodException e) {
			return null;
		} catch (SecurityException e) {
			throw new ReflectionUIError(e);
		}
		return result;
	}

	public static boolean isCompatibleWith(Method javaMethod, Class<?> containingJavaClass) {
		if (javaMethod.isSynthetic()) {
			return false;
		}
		if (javaMethod.isBridge()) {
			return false;
		}
		String fieldName = GetterFieldInfo.getFieldName(javaMethod.getName());
		if (fieldName == null) {
			return false;
		}
		for (Field siblingField : containingJavaClass.getFields()) {
			if (PublicFieldInfo.isCompatibleWith(siblingField)) {
				if (siblingField.getName().equals(fieldName)) {
					return false;
				}
			}
		}
		if (javaMethod.getParameterTypes().length > 0) {
			return false;
		}
		for (Method commonMethod : Object.class.getMethods()) {
			if (ReflectionUIUtils.isOverridenBy(commonMethod, javaMethod)) {
				return false;
			}
		}
		return true;
	}

	protected void resolveJavaReflectionModelAccessProblems() {
		javaGetterMethod.setAccessible(true);
	}

	public IMethodInfo getGetterMethodInfo() {
		return new DefaultMethodInfo(reflectionUI, javaGetterMethod);
	}

	protected IMethodInfo getSetterMethodInfo() {
		if (setterMethodInfo == null) {
			Method javaSetterMethod = GetterFieldInfo.getValidSetterMethod(javaGetterMethod, containingJavaClass);
			if (javaSetterMethod == null) {
				setterMethodInfo = IMethodInfo.NULL_METHOD_INFO;
			} else {
				setterMethodInfo = new DefaultMethodInfo(reflectionUI, javaSetterMethod);
			}
		}
		if (setterMethodInfo == IMethodInfo.NULL_METHOD_INFO) {
			return null;
		}
		return setterMethodInfo;
	}

	@Override
	public String getName() {
		if (name == null) {
			name = GetterFieldInfo.getFieldName(javaGetterMethod.getName());
		}
		return name;
	}

	@Override
	public boolean isHidden() {
		return false;
	}

	@Override
	public double getDisplayAreaHorizontalWeight() {
		return 1.0;
	}

	@Override
	public double getDisplayAreaVerticalWeight() {
		return 1.0;
	}

	@Override
	public ITypeInfo getType() {
		if (type == null) {
			type = reflectionUI
					.getTypeInfo(new TypeInfoSourceProxy(getGetterMethodInfo().getReturnValueType().getSource()) {
						@Override
						public SpecificitiesIdentifier getSpecificitiesIdentifier() {
							return new SpecificitiesIdentifier(reflectionUI
									.getTypeInfo(new JavaTypeInfoSource(containingJavaClass, null)).getName(),
									GetterFieldInfo.this.getName());
						}
					});
		}
		return type;
	}

	@Override
	public String getCaption() {
		return ReflectionUIUtils.getDefaultFieldCaption(this);
	}

	@Override
	public Object getValue(Object object) {
		IMethodInfo getter = getGetterMethodInfo();
		return getter.invoke(object, new InvocationData(object, getter));
	}

	@Override
	public Runnable getNextUpdateCustomUndoJob(Object object, Object value) {
		return null;
	}

	@Override
	public boolean hasValueOptions(Object object) {
		return false;
	}

	@Override
	public Object[] getValueOptions(Object object) {
		return null;
	}

	@Override
	public void setValue(Object object, Object value) {
		IMethodInfo setter = getSetterMethodInfo();
		setter.invoke(object, new InvocationData(object, setter, value));
	}

	@Override
	public boolean isNullValueDistinct() {
		return false;
	}

	@Override
	public String getNullValueLabel() {
		return null;
	}

	@Override
	public boolean isGetOnly() {
		return getSetterMethodInfo() == null;
	}

	@Override
	public boolean isTransient() {
		return (getSetterMethodInfo() != null) && (getSetterMethodInfo().isReadOnly());
	}

	@Override
	public ValueReturnMode getValueReturnMode() {
		return ValueReturnMode.INDETERMINATE;
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
	public boolean isFormControlMandatory() {
		return false;
	}

	@Override
	public boolean isFormControlEmbedded() {
		return false;
	}

	@Override
	public IInfoFilter getFormControlFilter() {
		return IInfoFilter.DEFAULT;
	}

	@Override
	public Map<String, Object> getSpecificProperties() {
		return Collections.emptyMap();
	}

	@Override
	public long getAutoUpdatePeriodMilliseconds() {
		return -1;
	}

	@Override
	public void onControlVisibilityChange(Object object, boolean visible) {
	}

	@Override
	public int hashCode() {
		return javaGetterMethod.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		if (!getClass().equals(obj.getClass())) {
			return false;
		}
		return javaGetterMethod.equals(((GetterFieldInfo) obj).javaGetterMethod);
	}

	@Override
	public String toString() {
		return "GetterFieldInfo [javaGetterMethod=" + javaGetterMethod + "]";
	}

}
