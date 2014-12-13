package xy.reflect.ui.info.type;

import java.awt.Component;
import java.util.List;

import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.method.IMethodInfo;

public class SimpleTypeInfoProxy implements ITypeInfo {
	protected ITypeInfo base;

	public SimpleTypeInfoProxy(ITypeInfo base) {
		super();
		this.base = base;
	}

	public String getName() {
		return base.getName();
	}

	public String getCaption() {
		return base.getCaption();
	}

	public boolean isConcrete() {
		return base.isConcrete();
	}

	public List<IMethodInfo> getConstructors() {
		return base.getConstructors();
	}

	public List<IFieldInfo> getFields() {
		return base.getFields();
	}

	public List<IMethodInfo> getMethods() {
		return base.getMethods();
	}

	public Component createFieldControl(Object object, IFieldInfo field) {
		return base.createFieldControl(object, field);
	}

	public boolean supportsValue(Object value) {
		return base.supportsValue(value);
	};

	public List<ITypeInfo> getPolymorphicInstanceTypes() {
		return base.getPolymorphicInstanceTypes();
	}

	@Override
	public int hashCode() {
		return base.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (!getClass().equals(obj.getClass())) {
			return false;
		}
		if (!base.equals(((SimpleTypeInfoProxy) obj).base)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return base.toString();
	}

	@Override
	public boolean isImmutable() {
		return base.isImmutable();
	}

	@Override
	public boolean hasCustomFieldControl() {
		return base.hasCustomFieldControl();
	}

}
