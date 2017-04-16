package xy.reflect.ui.info.field;

import java.util.Map;

import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.method.InvocationData;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.util.ITypeInfoProxyFactory;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;

public class MethodAsField implements IFieldInfo {

	protected IMethodInfo method;
	protected String fieldName;

	public MethodAsField(IMethodInfo method, String fieldName) {
		if (method.getParameters().size() > 0) {
			throw new ReflectionUIError("Cannot create field from method having parameters");
		}
		if (method.getReturnValueType() == null) {
			throw new ReflectionUIError("Cannot create field from method having void return type");
		}
		this.method = method;
		this.fieldName = fieldName;
	}

	@Override
	public String getName() {
		return fieldName;
	}

	@Override
	public String getCaption() {
		return ReflectionUIUtils.getDefaultFieldCaption(this);
	}

	@Override
	public String getOnlineHelp() {
		return method.getOnlineHelp();
	}

	@Override
	public Map<String, Object> getSpecificProperties() {
		return method.getSpecificProperties();
	}

	@Override
	public ITypeInfo getType() {
		return method.getReturnValueType();
	}

	@Override
	public ITypeInfoProxyFactory getTypeSpecificities() {
		return null;
	}

	@Override
	public Object getValue(Object object) {
		return method.invoke(object, new InvocationData());
	}

	@Override
	public Object[] getValueOptions(Object object) {
		return null;
	}

	@Override
	public void setValue(Object object, Object value) {
	}

	@Override
	public Runnable getCustomUndoUpdateJob(Object object, Object value) {
		return method.getUndoJob(object, new InvocationData());
	}

	@Override
	public boolean isValueNullable() {
		return !getType().isPrimitive();
	}

	@Override
	public boolean isGetOnly() {
		return true;
	}

	@Override
	public String getNullValueLabel() {
		return method.getNullReturnValueLabel();
	}

	@Override
	public ValueReturnMode getValueReturnMode() {
		return method.getValueReturnMode();
	}

	@Override
	public InfoCategory getCategory() {
		return method.getCategory();
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
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((method == null) ? 0 : method.hashCode());
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
		MethodAsField other = (MethodAsField) obj;
		if (method == null) {
			if (other.method != null)
				return false;
		} else if (!method.equals(other.method))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "MethodAsField [method=" + method + "]";
	}

}
