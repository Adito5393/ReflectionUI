package xy.reflect.ui.info.field;

import xy.reflect.ui.info.type.ITypeInfo;

public class FieldInfoProxy implements IFieldInfo {

	protected IFieldInfo base;

	public FieldInfoProxy(IFieldInfo base) {
		this.base = base;
	}

	@Override
	public Object getValue(Object object) {
		return base.getValue(object);
	}

	@Override
	public ITypeInfo getType() {
		return base.getType();
	}

	@Override
	public String getCaption() {
		return base.getCaption();
	}

	@Override
	public void setValue(Object object, Object value) {
		base.setValue(object, value);
	}

	@Override
	public boolean isNullable() {
		return base.isNullable();
	}

	@Override
	public boolean isReadOnly() {
		return base.isReadOnly();
	}
	
	@Override
	public String toString() {
		return base.toString();
	}

	@Override
	public String getName() {
		return base.getName();
	}

	@Override
	public int hashCode() {
		return base.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == null){
			return false;
		}
		if(obj == this){
			return true;
		}
		if(!getClass().equals(obj.getClass())){
			return false;
		}
		return base.equals(((FieldInfoProxy) obj).base);
	}

	@Override
	public InfoCategory getCategory() {
		return base.getCategory();
	}
	
	

}