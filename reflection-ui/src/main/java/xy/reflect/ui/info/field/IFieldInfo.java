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
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;

/**
 * This interface allows to specify UI-oriented field properties.
 * 
 * @author olitank
 *
 */
public interface IFieldInfo extends IInfo {

	/**
	 * Dummy instance of this class made available for utilitarian purposes.
	 */
	public IFieldInfo NULL_FIELD_INFO = new IFieldInfo() {

		ReflectionUI reflectionUI = new ReflectionUI();
		ITypeInfo type = new DefaultTypeInfo(reflectionUI, new JavaTypeInfoSource(Object.class, null));

		@Override
		public String getName() {
			return "NULL_FIELD_INFO";
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
		public boolean isHidden() {
			return false;
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
		public boolean isNullValueDistinct() {
			return false;
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
		public Runnable getNextUpdateCustomUndoJob(Object object, Object value) {
			return null;
		}

		@Override
		public Object[] getValueOptions(Object object) {
			return null;
		}

		@Override
		public ITypeInfo getType() {
			return type;
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

		@Override
		public long getAutoUpdatePeriodMilliseconds() {
			return -1;
		}

	};

	/**
	 * @return UI-oriented type properties of the current field.
	 */
	ITypeInfo getType();

	/**
	 * @param object
	 *            The object hosting the field value.
	 * @return the value of this field extracted from the given object.
	 */
	Object getValue(Object object);

	/**
	 * @param object
	 *            The object hosting the field value.
	 * @return options for value of this field or null if there is not any know
	 *         option.
	 */
	Object[] getValueOptions(Object object);

	/**
	 * Updates the given field value to the given object.
	 * 
	 * @param object
	 *            The object hosting the field value.
	 * @param value
	 *            The new field field value.
	 */
	void setValue(Object object, Object value);

	/**
	 * @param object
	 *            The object hosting the field value.
	 * @param newValue
	 *            The new field field value.
	 * @return a job that can revert the next field value update, or null if the
	 *         update cannot be reverted.
	 */
	Runnable getNextUpdateCustomUndoJob(Object object, Object newValue);

	/**
	 * @return true if and only if this field control must distinctly display and
	 *         allow to set the null value. This is usually needed if a null value
	 *         has a special meaning different from "empty/default value" for the
	 *         developer.
	 */
	boolean isNullValueDistinct();

	/**
	 * @return true if and only if this field value can be updated. Then
	 *         {@link #setValue(Object, Object)} should not be called.
	 */
	boolean isGetOnly();

	/**
	 * @return a text that should be displayed by the field control to describe the
	 *         null value.
	 */
	String getNullValueLabel();

	/**
	 * @return the value return mode of this field. It may impact the behavior of
	 *         this field control.
	 */
	ValueReturnMode getValueReturnMode();

	/**
	 * @return the category in which this field will be displayed.
	 */
	InfoCategory getCategory();

	/**
	 * @return true if this field value is forcibly displayed as a generic form. If
	 *         false is returned then a custom control may be displayed. Note that
	 *         the form is either embedded in the parent form or displayed in a
	 *         child dialog according to the return value of
	 *         {@link #isFormControlEmbedded()}.
	 */
	boolean isFormControlMandatory();

	/**
	 * @return whether this field value form is embedded in the parent form or
	 *         displayed in a child dialog. Note that this method has no impact if a
	 *         custom control is displayed instead of a generic form.
	 */
	boolean isFormControlEmbedded();

	/**
	 * @return an object used to filter out some fields and methods from this field
	 *         value form. Note that this method has no impact if a custom control
	 *         is displayed instead of a generic form.
	 */
	IInfoFilter getFormControlFilter();

	/**
	 * @return the automatic update period (in milliseconds) that this field control
	 *         will try to respect. Note that -1 means that there is no automatic
	 *         update and 0 means that the update occurs as fast as possible.
	 */
	long getAutoUpdatePeriodMilliseconds();

	/**
	 * @return true if and only if this field control is filtered out from the
	 *         display.
	 */
	boolean isHidden();

	/**
	 * @return a number that specifies how to distribute extra horizontal space
	 *         between sibling field controls. If the resulting layout is smaller
	 *         horizontally than the area it needs to fill, the extra space is
	 *         distributed to each field in proportion to its horizontal weight. A
	 *         field that has a weight of zero receives no extra space. If all the
	 *         weights are zero, all the extra space appears between the grids of
	 *         the cell and the left and right edges. It should be a non-negative
	 *         value.
	 */
	double getDisplayAreaHorizontalWeight();

	/**
	 * @return a number that specifies how to distribute extra vertical space
	 *         between sibling field controls. If the resulting layout is smaller
	 *         vertically than the area it needs to fill, the extra space is
	 *         distributed to each field in proportion to its vertical weight. A
	 *         field that has a weight of zero receives no extra space. If all the
	 *         weights are zero, all the extra space appears between the grids of
	 *         the cell and the left and right edges. It should be a non-negative
	 *         value.
	 * 
	 */
	double getDisplayAreaVerticalWeight();

}
