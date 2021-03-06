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
package xy.reflect.ui.info.type.iterable.map;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.method.InvocationData;
import xy.reflect.ui.info.type.iterable.StandardCollectionTypeInfo;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;

public class StandardMapAsListTypeInfo extends StandardCollectionTypeInfo {

	protected Class<?> keyJavaType;
	protected Class<?> valueJavaType;

	public StandardMapAsListTypeInfo(ReflectionUI reflectionUI, JavaTypeInfoSource source, Class<?> keyJavaType,
			Class<?> valueJavaType) {
		super(reflectionUI, source, reflectionUI.getTypeInfo(
				new JavaTypeInfoSource(StandardMapEntry.class, new Class<?>[] { keyJavaType, valueJavaType }, null)));
		this.keyJavaType = keyJavaType;
		this.valueJavaType = valueJavaType;
	}

	public static boolean isCompatibleWith(Class<?> javaType) {
		if (Map.class.isAssignableFrom(javaType)) {
			return true;
		}
		return false;
	}

	@Override
	public boolean canReplaceContent() {
		return true;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void replaceContent(Object listValue, Object[] array) {
		Map tmpMap = new HashMap();
		for (Object item : array) {
			StandardMapEntry entry = (StandardMapEntry) item;
			if (tmpMap.containsKey(entry.getKey())) {
				throw new ReflectionUIError(
						"Duplicate key: '" + ReflectionUIUtils.toString(reflectionUI, entry.getKey()) + "'");
			}
			tmpMap.put(entry.getKey(), entry.getValue());
		}
		Map map = (Map) listValue;
		map.clear();
		map.putAll(tmpMap);
	}

	@Override
	public boolean canInstanciateFromArray() {
		return true;
	}

	@SuppressWarnings({ "rawtypes" })
	@Override
	public Object fromArray(Object[] array) {
		IMethodInfo constructor = ReflectionUIUtils.getZeroParameterMethod(getConstructors());
		Map result = (Map) constructor.invoke(null, new InvocationData(null, constructor));
		replaceContent(result, array);
		return result;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Object[] toArray(Object listValue) {
		List<StandardMapEntry> result = new ArrayList<StandardMapEntry>();
		for (Object obj : ((Map) listValue).entrySet()) {
			Map.Entry entry = (Entry) obj;
			StandardMapEntry standardMapEntry = new StandardMapEntry(entry.getKey(), entry.getValue());
			reflectionUI.registerPrecomputedTypeInfoObject(standardMapEntry,
					new StandardMapEntryTypeInfo(reflectionUI, keyJavaType, valueJavaType));
			result.add(standardMapEntry);
		}
		return result.toArray();
	}

	@Override
	public boolean isOrdered() {
		if (Map.class.equals(getJavaType())) {
			return false;
		}
		if (HashMap.class.equals(getJavaType())) {
			return false;
		}
		if (SortedMap.class.isAssignableFrom(getJavaType())) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "StandardMapAsListTypeInfo [source=" + source + ", entryType=" + itemType + "]";
	}

}
