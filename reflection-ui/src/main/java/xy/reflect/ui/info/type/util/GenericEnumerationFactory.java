package xy.reflect.ui.info.type.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.method.AbstractConstructorInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.method.InvocationData;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.enumeration.IEnumerationItemInfo;
import xy.reflect.ui.info.type.enumeration.IEnumerationTypeInfo;
import xy.reflect.ui.info.type.source.ITypeInfoSource;
import xy.reflect.ui.info.type.source.PrecomputedTypeInfoSource;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;

public class GenericEnumerationFactory {
	protected ReflectionUI reflectionUI;
	protected Iterable<?> iterable;
	protected String enumerationTypeName;
	protected String typeCaption;
	protected boolean dynamicEnumeration;

	public GenericEnumerationFactory(ReflectionUI reflectionUI, Iterable<?> iterable, String enumerationTypeName,
			String typeCaption, boolean dynamicEnumeration) {
		super();
		this.reflectionUI = reflectionUI;
		this.iterable = iterable;
		this.enumerationTypeName = enumerationTypeName;
		this.typeCaption = typeCaption;
		this.dynamicEnumeration = dynamicEnumeration;
	}

	public GenericEnumerationFactory(ReflectionUI reflectionUI, Object[] array, String enumerationTypeName,
			String typeCaption) {
		this(reflectionUI, Arrays.asList(array), enumerationTypeName, typeCaption, false);
	}

	protected Map<String, Object> getItemSpecificProperties(Object arrayItem) {
		return Collections.emptyMap();
	}

	protected String getItemOnlineHelp(Object arrayItem) {
		return null;
	}

	protected String getItemName(Object arrayItem) {
		return "Item[value=" + arrayItem + "]";
	}

	protected String getItemCaption(Object arrayItem) {
		return ReflectionUIUtils.toString(reflectionUI, arrayItem);
	}

	public Object getInstance(Object arrayItem) {
		if (arrayItem == null) {
			return null;
		}
		Instance result = new Instance(arrayItem);
		reflectionUI.registerPrecomputedTypeInfoObject(result, new TypeInfo());
		return result;
	}

	public Object unwrapInstance(Object obj) {
		if (obj == null) {
			return null;
		}
		Instance instance = (Instance) obj;
		if (instance.getOuterType() != this) {
			throw new ReflectionUIError();
		}
		return instance.getArrayItem();
	}

	public ITypeInfoSource getInstanceTypeInfoSource() {
		return new PrecomputedTypeInfoSource(new TypeInfo());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + iterable.hashCode();
		result = prime * result + ((typeCaption == null) ? 0 : typeCaption.hashCode());
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
		GenericEnumerationFactory other = (GenericEnumerationFactory) obj;
		if (!iterable.equals(other.iterable))
			return false;
		if (typeCaption == null) {
			if (other.typeCaption != null)
				return false;
		} else if (!typeCaption.equals(other.typeCaption))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ArrayAsEnumerationFactory [enumerationTypeName=" + enumerationTypeName + ", typeCaption=" + typeCaption
				+ "]";
	}

	protected class Instance {
		protected Object arrayItem;

		public Instance(Object arrayItem) {
			super();
			this.arrayItem = arrayItem;
		}

		public Object getArrayItem() {
			return arrayItem;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((arrayItem == null) ? 0 : arrayItem.hashCode());
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
			Instance other = (Instance) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (arrayItem == null) {
				if (other.arrayItem != null)
					return false;
			} else if (!arrayItem.equals(other.arrayItem))
				return false;
			return true;
		}

		private GenericEnumerationFactory getOuterType() {
			return GenericEnumerationFactory.this;
		}

		@Override
		public String toString() {
			return arrayItem.toString();
		}

	}

	protected class TypeInfo implements IEnumerationTypeInfo {

		@Override
		public boolean isDynamicEnumeration() {
			return dynamicEnumeration;
		}

		@Override
		public boolean isPrimitive() {
			return false;
		}

		@Override
		public boolean isImmutable() {
			return true;
		}

		@Override
		public Map<String, Object> getSpecificProperties() {
			return Collections.emptyMap();
		}

		@Override
		public String getOnlineHelp() {
			return null;
		}

		@Override
		public String getName() {
			return enumerationTypeName;
		}

		@Override
		public String getCaption() {
			return typeCaption;
		}

		@Override
		public boolean isConcrete() {
			return true;
		}

		@Override
		public boolean isModificationStackAccessible() {
			return false;
		}

		@Override
		public List<ITypeInfo> getPolymorphicInstanceSubTypes() {
			return Collections.emptyList();
		}

		@Override
		public List<IMethodInfo> getMethods() {
			return Collections.emptyList();
		}

		@Override
		public List<IFieldInfo> getFields() {
			return Collections.emptyList();
		}

		@Override
		public List<IMethodInfo> getConstructors() {
			final Iterator<?> iterator = iterable.iterator();
			if (!iterator.hasNext()) {
				return Collections.emptyList();
			} else {
				return Collections.<IMethodInfo>singletonList(new AbstractConstructorInfo(TypeInfo.this) {

					@Override
					public Object invoke(Object object, InvocationData invocationData) {
						return getInstance(iterator.next());
					}

					@Override
					public List<IParameterInfo> getParameters() {
						return Collections.emptyList();
					}
				});
			}
		}

		@Override
		public Object[] getPossibleValues() {
			List<Instance> result = new ArrayList<Instance>();
			for (Object arrayItem : iterable) {
				result.add((Instance) getInstance(arrayItem));
			}
			return result.toArray();
		}

		@Override
		public IEnumerationItemInfo getValueInfo(final Object object) {
			final Object arrayItem = unwrapInstance(object);
			return new IEnumerationItemInfo() {

				@Override
				public Map<String, Object> getSpecificProperties() {
					return getItemSpecificProperties(arrayItem);
				}

				@Override
				public String getOnlineHelp() {
					return getItemOnlineHelp(arrayItem);
				}

				@Override
				public String getName() {
					return getItemName(arrayItem);
				}

				@Override
				public String getCaption() {
					return getItemCaption(arrayItem);
				}

				@Override
				public String toString() {
					return arrayItem.toString();
				}
			};
		}

		@Override
		public boolean supportsInstance(Object object) {
			return object instanceof Instance;
		}

		@Override
		public void validate(Object object) throws Exception {
			ReflectionUIUtils.checkInstance(this, object);
		}

		@Override
		public boolean canCopy(Object object) {
			ReflectionUIUtils.checkInstance(this, object);
			return false;
		}

		@Override
		public Object copy(Object object) {
			throw new ReflectionUIError();
		}

		@Override
		public String toString(Object object) {
			ReflectionUIUtils.checkInstance(this, object);
			return object.toString();
		}

		GenericEnumerationFactory getOuterType() {
			return GenericEnumerationFactory.this;
		}

		@Override
		public String toString() {
			return "TypeInfo [of=" + getOuterType() + "]";
		}

	}
}