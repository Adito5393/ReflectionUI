package xy.reflect.ui.info.field;

import java.util.Map;

import xy.reflect.ui.info.AbstractInfoProxy;
import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.factory.IInfoProxyFactory;

public class FieldInfoProxy extends AbstractInfoProxy implements IFieldInfo {

	protected IFieldInfo base;

	public FieldInfoProxy(IFieldInfo base) {
		this.base = base;
	}

	public IFieldInfo getBase() {
		return base;
	}

	public double getDisplayAreaHorizontalWeight() {
		return base.getDisplayAreaHorizontalWeight();
	}

	public double getDisplayAreaVerticalWeight() {
		return base.getDisplayAreaVerticalWeight();
	}

	@Override
	public Object getValue(Object object) {
		return base.getValue(object);
	}

	public Runnable getNextUpdateCustomUndoJob(Object object, Object value) {
		return base.getNextUpdateCustomUndoJob(object, value);
	}

	@Override
	public Object[] getValueOptions(Object object) {
		return base.getValueOptions(object);
	}

	@Override
	public ITypeInfo getType() {
		return base.getType();
	}

	public IInfoProxyFactory getTypeSpecificities() {
		return base.getTypeSpecificities();
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
	public boolean isNullValueDistinct() {
		return base.isNullValueDistinct();
	}

	public String getNullValueLabel() {
		return base.getNullValueLabel();
	}

	@Override
	public boolean isGetOnly() {
		return base.isGetOnly();
	}

	@Override
	public ValueReturnMode getValueReturnMode() {
		return base.getValueReturnMode();
	}

	public boolean isFormControlMandatory() {
		return base.isFormControlMandatory();
	}

	public boolean isFormControlEmbedded() {
		return base.isFormControlEmbedded();
	}

	public IInfoFilter getFormControlFilter() {
		return base.getFormControlFilter();
	}

	@Override
	public String getName() {
		return base.getName();
	}

	@Override
	public boolean isHidden() {
		return base.isHidden();
	}

	@Override
	public InfoCategory getCategory() {
		return base.getCategory();
	}

	@Override
	public String getOnlineHelp() {
		return base.getOnlineHelp();
	}

	@Override
	public Map<String, Object> getSpecificProperties() {
		return base.getSpecificProperties();
	}

	public long getAutoUpdatePeriodMilliseconds() {
		return base.getAutoUpdatePeriodMilliseconds();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((base == null) ? 0 : base.hashCode());
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
		FieldInfoProxy other = (FieldInfoProxy) obj;
		if (base == null) {
			if (other.base != null)
				return false;
		} else if (!base.equals(other.base))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "FieldInfoProxy [name=" + getName() + ", base=" + base + "]";
	}

}
