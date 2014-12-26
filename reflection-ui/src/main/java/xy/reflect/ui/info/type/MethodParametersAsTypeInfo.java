package xy.reflect.ui.info.type;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.field.InfoCategory;
import xy.reflect.ui.info.method.AbstractConstructorMethodInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.parameter.IParameterInfo;

public class MethodParametersAsTypeInfo extends DefaultTypeInfo {

	private IMethodInfo method;

	public MethodParametersAsTypeInfo(ReflectionUI reflectionUI,
			IMethodInfo method) {
		super(reflectionUI, Object.class);
		this.method = method;
	}
	


	public IMethodInfo getMethod() {
		return method;
	}

	@Override
	public boolean isConcrete() {
		return true;
	}

	@Override
	public List<IMethodInfo> getConstructors() {
		return Collections
				.<IMethodInfo> singletonList(new AbstractConstructorMethodInfo(
						this) {

					@Override
					public Object invoke(Object object,
							Map<String, Object> valueByParameterName) {
						return new PrecomputedTypeInfoInstanceWrapper(
								new HashMap<String, Object>(),
								MethodParametersAsTypeInfo.this);
					}

					@Override
					public List<IParameterInfo> getParameters() {
						return Collections.emptyList();
					}
				});
	}

	@Override
	public List<IFieldInfo> getFields() {
		List<IFieldInfo> result = new ArrayList<IFieldInfo>();
		for (IParameterInfo param : method.getParameters()) {
			result.add(getParameterAsField(param));
		}
		return result;
	}

	@Override
	public String getName() {
		return method.getName();
	}

	@Override
	public String getCaption() {
		return method.getCaption();
	}

	@Override
	public int hashCode() {
		return method.hashCode();
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
		return method.equals(((MethodParametersAsTypeInfo) obj).method);
	}

	@Override
	public String toString() {
		return getCaption();
	}



	public static IFieldInfo getParameterAsField(final IParameterInfo param) {
		return new IFieldInfo() {

			@Override
			public void setValue(Object object, Object value) {
				@SuppressWarnings("unchecked")
				Map<String, Object> valueByParameterName = (Map<String, Object>) object;
				valueByParameterName.put(param.getName(), value);
			}

			@Override
			public boolean isNullable() {
				return param.isNullable();
			}

			@Override
			public Object getValue(Object object) {
				@SuppressWarnings("unchecked")
				Map<String, Object> valueByParameterName = (Map<String, Object>) object;
				if (!valueByParameterName.containsKey(param.getName())) {
					return param.getDefaultValue();
				}
				return valueByParameterName.get(param.getName());
			}

			@Override
			public ITypeInfo getType() {
				return param.getType();
			}

			@Override
			public String getCaption() {
				return param.getCaption();
			}

			@Override
			public boolean isReadOnly() {
				return false;
			}

			@Override
			public String getName() {
				return param.getName();
			}

			@Override
			public InfoCategory getCategory() {
				return null;
			}
		};
	}

}