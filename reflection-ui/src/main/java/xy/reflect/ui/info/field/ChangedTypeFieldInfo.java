package xy.reflect.ui.info.field;

import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.factory.ITypeInfoProxyFactory;
import xy.reflect.ui.util.Mapper;
import xy.reflect.ui.util.ReflectionUIError;

public class ChangedTypeFieldInfo extends FieldInfoProxy {

	protected ITypeInfo newType;
	protected Mapper<Object> conversionMethod;
	protected Mapper<Object> reverseConversionMethod;

	public ChangedTypeFieldInfo(IFieldInfo base, ITypeInfo newType, Mapper<Object> conversionMethod,
			Mapper<Object> reverseConversionMethod) {
		super(base);
		this.newType = newType;
		this.conversionMethod = conversionMethod;
		this.reverseConversionMethod = reverseConversionMethod;
	}

	protected Object convert(Object value) {
		if (conversionMethod == null) {
			return value;
		}
		if (value == null) {
			return null;
		}
		try {
			return conversionMethod.map(value);
		} catch (Exception e) {
			throw new ReflectionUIError(e);
		}
	}

	protected Object revertConversion(Object value) {
		if (reverseConversionMethod == null) {
			return value;
		}
		if (value == null) {
			return null;
		}
		try {
			return reverseConversionMethod.map(value);
		} catch (Exception e) {
			throw new ReflectionUIError(e);
		}
	}

	@Override
	public Object getValue(Object object) {
		return convert(super.getValue(object));
	}

	@Override
	public void setValue(Object object, Object value) {
		super.setValue(object, revertConversion(value));
	}

	@Override
	public Runnable getNextUpdateCustomUndoJob(final Object object, final Object newValue) {
		return super.getNextUpdateCustomUndoJob(object, revertConversion(newValue));
	}

	@Override
	public Object[] getValueOptions(Object object) {
		Object[] result = super.getValueOptions(object);
		if (result == null) {
			return null;
		}
		Object[] convertedResult = new Object[result.length];
		for (int i = 0; i < result.length; i++) {
			convertedResult[i] = convert(result[i]);
		}
		return convertedResult;
	}

	@Override
	public ITypeInfo getType() {
		return newType;
	}

	@Override
	public ITypeInfoProxyFactory getTypeSpecificities() {
		return null;
	}

	@Override
	public ValueReturnMode getValueReturnMode() {
		return ValueReturnMode.CALCULATED;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((conversionMethod == null) ? 0 : conversionMethod.hashCode());
		result = prime * result + ((newType == null) ? 0 : newType.hashCode());
		result = prime * result + ((reverseConversionMethod == null) ? 0 : reverseConversionMethod.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ChangedTypeFieldInfo other = (ChangedTypeFieldInfo) obj;
		if (conversionMethod == null) {
			if (other.conversionMethod != null)
				return false;
		} else if (!conversionMethod.equals(other.conversionMethod))
			return false;
		if (newType == null) {
			if (other.newType != null)
				return false;
		} else if (!newType.equals(other.newType))
			return false;
		if (reverseConversionMethod == null) {
			if (other.reverseConversionMethod != null)
				return false;
		} else if (!reverseConversionMethod.equals(other.reverseConversionMethod))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ChangedTypeField [newType=" + newType + ", conversionMethod=" + conversionMethod
				+ ", reverseConversionMethod=" + reverseConversionMethod + "]";
	}

}