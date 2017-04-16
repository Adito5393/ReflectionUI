package xy.reflect.ui.info.field;

import java.util.Collections;
import java.util.Map;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.IInfo;
import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.type.DefaultTypeInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.util.ITypeInfoProxyFactory;

public interface IFieldInfo extends IInfo {
	public IFieldInfo NULL_FIELD_INFO = new IFieldInfo() {

		@Override
		public String getName() {
			return "NULL_FIELD_INFO";
		}

		@Override
		public String getOnlineHelp() {
			return null;
		}

		@Override
		public String getCaption() {
			return "";
		}

		@Override
		public void setValue(Object object, Object value) {
		}

		@Override
		public boolean isGetOnly() {
			return true;
		}

		@Override
		public boolean isValueNullable() {
			return true;
		}

		@Override
		public String getNullValueLabel() {
			return null;
		}

		@Override
		public ValueReturnMode getValueReturnMode() {
			return ValueReturnMode.INDETERMINATE;
		}

		@Override
		public Object getValue(Object object) {
			return null;
		}

		@Override
		public Runnable getCustomUndoUpdateJob(Object object, Object value) {
			return null;
		}

		@Override
		public Object[] getValueOptions(Object object) {
			return null;
		}

		@Override
		public ITypeInfo getType() {
			return new DefaultTypeInfo(new ReflectionUI(), Object.class);
		}

		@Override
		public ITypeInfoProxyFactory getTypeSpecificities() {
			return null;
		}

		@Override
		public InfoCategory getCategory() {
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
		public String toString() {
			return "NULL_FIELD_INFO";
		}

	};

	ITypeInfo getType();

	ITypeInfoProxyFactory getTypeSpecificities();

	Object getValue(Object object);

	Object[] getValueOptions(Object object);

	void setValue(Object object, Object value);

	Runnable getCustomUndoUpdateJob(Object object, Object value);

	boolean isValueNullable();

	boolean isGetOnly();

	String getNullValueLabel();

	ValueReturnMode getValueReturnMode();

	InfoCategory getCategory();
	
	boolean isFormControlMandatory();
	
	boolean isFormControlEmbedded();
	
	IInfoFilter getFormControlFilter();

}
