package xy.reflect.ui.control.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import xy.reflect.ui.control.data.IControlData;
import xy.reflect.ui.control.swing.SwingRenderer.FieldControlPlaceHolder;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.util.SwingRendererUtils;

public abstract class NullableControl extends JPanel implements IAdvancedFieldControl {

	protected SwingRenderer swingRenderer;
	protected static final long serialVersionUID = 1L;
	protected IControlData data;
	protected JCheckBox nullingControl;
	protected Component subControl;
	protected FieldControlPlaceHolder placeHolder;
	protected ITypeInfo subControlValueType;

	protected abstract Component createNonNullValueControl();

	protected abstract Object getDefaultValue();

	public NullableControl(SwingRenderer swingRenderer, FieldControlPlaceHolder placeHolder) {
		this.swingRenderer = swingRenderer;
		this.placeHolder = placeHolder;
		this.data = placeHolder.getControlData();
		initialize();
	}

	protected void initialize() {
		setLayout(new BorderLayout());
		nullingControl = new JCheckBox();
		nullingControl.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					onNullingControlStateChange();
					subControl.requestFocus();
				} catch (Throwable t) {
					swingRenderer.handleExceptionsFromDisplayedUI(NullableControl.this, t);
				}
			}
		});

		if (!data.isGetOnly()) {
			add(nullingControl, BorderLayout.WEST);
		}

		refreshUI();
	}

	

	public Component getSubControl() {
		return subControl;
	}

	protected void setShouldBeNull(boolean b) {
		nullingControl.setSelected(!b);
	}

	protected boolean shoulBeNull() {
		return !nullingControl.isSelected();
	}

	@Override
	public boolean refreshUI() {
		Object value = data.getValue();
		setShouldBeNull(value == null);
		boolean hadFocus = (subControl != null) && SwingRendererUtils.hasOrContainsFocus(subControl);
		updateSubControl(value);
		if (hadFocus && (subControl != null)) {
			subControl.requestFocus();
		}
		return true;
	}

	protected void onNullingControlStateChange() {
		Object newValue;
		if (!shoulBeNull()) {
			newValue = getDefaultValue();
			if (newValue == null) {
				setShouldBeNull(true);
				return;
			}
		} else {
			newValue = null;
			remove(subControl);
			subControl = null;
		}
		data.setValue(newValue);
		refreshUI();
	}

	public void updateSubControl(Object newValue) {
		boolean updated = false;
		if (subControl instanceof IAdvancedFieldControl) {
			IAdvancedFieldControl fieldControl = (IAdvancedFieldControl) subControl;
			if (newValue != null) {
				if (fieldControl.refreshUI()) {
					updated = true;
				}
			}
		}
		if (!updated) {
			if (subControl != null) {
				remove(subControl);
			}
			if (newValue != null) {
				subControlValueType = swingRenderer.getReflectionUI()
						.getTypeInfo(swingRenderer.getReflectionUI().getTypeInfoSource(newValue));
				subControl = createNonNullValueControl();
				add(subControl, BorderLayout.CENTER);
			} else {
				subControlValueType = null;
				subControl = createNullControl();
				add(subControl, BorderLayout.CENTER);
			}
			SwingRendererUtils.handleComponentSizeChange(this);
		}
	}

	protected Component createNullControl() {
		return new NullControl(swingRenderer, placeHolder) {
			private static final long serialVersionUID = 1L;

			@Override
			public void onMousePress() {
				if (!data.isGetOnly()) {
					setShouldBeNull(false);
					onNullingControlStateChange();
					subControl.requestFocus();
				}
			}

			@Override
			protected Object getText() {
				return data.getNullValueLabel();
			}
		};
	}

	@Override
	public boolean showCaption() {
		if (subControl instanceof IAdvancedFieldControl) {
			return ((IAdvancedFieldControl) subControl).showCaption();
		} else {
			return false;
		}
	}

	@Override
	public boolean displayError(String msg) {
		if (subControl instanceof IAdvancedFieldControl) {
			return ((IAdvancedFieldControl) subControl).displayError(msg);
		} else {
			return false;
		}
	}

	@Override
	public boolean handlesModificationStackUpdate() {
		if (subControl instanceof IAdvancedFieldControl) {
			return ((IAdvancedFieldControl) subControl).handlesModificationStackUpdate();
		} else {
			return false;
		}
	}

	@Override
	public Object getFocusDetails() {
		Object subControlFocusDetails = null;
		Class<?> subControlClass = null;
		{
			if (subControl instanceof IAdvancedFieldControl) {
				subControlFocusDetails = ((IAdvancedFieldControl) subControl).getFocusDetails();
				subControlClass = subControl.getClass();
			}
		}
		if (subControlFocusDetails == null) {
			return null;
		}
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("subControlFocusDetails", subControlFocusDetails);
		result.put("subControlClass", subControlClass);
		return result;
	}

	@Override
	public void requestDetailedFocus(Object value) {
		@SuppressWarnings("unchecked")
		Map<String, Object> focusDetails = (Map<String, Object>) value;
		Object subControlFocusDetails = focusDetails.get("subControlFocusDetails");
		Class<?> subControlClass = (Class<?>) focusDetails.get("subControlClass");
		subControl.requestFocus();
		if (subControl instanceof IAdvancedFieldControl) {
			if (subControl.getClass().equals(subControlClass)) {
				((IAdvancedFieldControl) subControl).requestDetailedFocus(subControlFocusDetails);
			}
		}
	}

	@Override
	public void requestFocus() {
		if (subControl != null) {
			subControl.requestFocus();
		}
	}

	@Override
	public void validateSubForm() throws Exception {
		if (subControl instanceof IAdvancedFieldControl) {
			((IAdvancedFieldControl) subControl).validateSubForm();
		}
	}

	public ITypeInfo getSubControlValueType() {
		return subControlValueType;
	}
}