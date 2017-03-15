package xy.reflect.ui.control.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import xy.reflect.ui.control.input.ControlDataProxy;
import xy.reflect.ui.control.input.IControlData;
import xy.reflect.ui.control.input.IControlInput;
import xy.reflect.ui.info.DesktopSpecificProperty;
import xy.reflect.ui.info.IInfo;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.custom.TextualTypeInfo;
import xy.reflect.ui.info.type.util.EncapsulatedObjectFactory;
import xy.reflect.ui.undo.IModification;
import xy.reflect.ui.undo.ModificationStack;
import xy.reflect.ui.undo.ControlDataValueModification;
import xy.reflect.ui.util.Accessor;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.reflect.ui.util.SwingRendererUtils;

@SuppressWarnings("unused")
public class DialogAccessControl extends JPanel implements IAdvancedFieldControl {

	protected static final long serialVersionUID = 1L;
	protected SwingRenderer swingRenderer;
	protected IControlData data;

	protected Component statusControl;
	protected Component iconControl;
	protected Component button;
	protected IControlInput input;

	public DialogAccessControl(final SwingRenderer swingRenderer, IControlInput input) {
		this.swingRenderer = swingRenderer;
		this.input = input;
		this.data = input.getControlData();
		setLayout(new BorderLayout());

		statusControl = createStatusControl(input);
		button = createButton();
		iconControl = createIconControl();

		if (button != null) {
			add(SwingRendererUtils.flowInLayout(button, GridBagConstraints.CENTER), BorderLayout.WEST);
		}
		if (statusControl != null) {
			JPanel centerPanel = new JPanel();
			{
				add(centerPanel, BorderLayout.CENTER);
				centerPanel.setLayout(new GridBagLayout());
				GridBagConstraints c = new GridBagConstraints();
				c.fill = GridBagConstraints.HORIZONTAL;
				c.weightx = 1.0;
				centerPanel.add(statusControl, c);
			}
		}
		if (iconControl != null) {
			add(iconControl, BorderLayout.EAST);
		}

		updateControls();
	}

	@Override
	public void requestFocus() {
		button.requestFocus();
	}

	protected Component createIconControl() {
		return new JLabel() {

			private static final long serialVersionUID = 1L;

			@Override
			public Dimension getPreferredSize() {
				Dimension result = super.getPreferredSize();
				if (result != null) {
					if (statusControl != null) {
						Dimension textControlPreferredSize = statusControl.getPreferredSize();
						if (textControlPreferredSize != null) {
							result.height = textControlPreferredSize.height;
						}
					}
				}
				return result;
			}

		};
	}

	protected Component createButton() {
		final JButton result = new JButton("...") {

			private static final long serialVersionUID = 1L;

			@Override
			public Dimension getPreferredSize() {
				Dimension result = super.getPreferredSize();
				if (result != null) {
					result.width = result.height;
				}
				return result;
			}

		};
		result.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					openDialog();
				} catch (Throwable t) {
					swingRenderer.handleExceptionsFromDisplayedUI(result, t);
				}
			}
		});
		return result;
	}

	protected Component createStatusControl(IControlInput input) {
		return new TextControl(swingRenderer, input) {

			private static final long serialVersionUID = 1L;

			@Override
			protected IControlData retrieveData() {
				return new ControlDataProxy(IControlData.NULL_CONTROL_DATA) {

					@Override
					public Object getValue() {
						Object fieldValue = DialogAccessControl.this.data.getValue();
						return ReflectionUIUtils.toString(swingRenderer.getReflectionUI(), fieldValue);
					}

					@Override
					public ITypeInfo getType() {
						return new TextualTypeInfo(swingRenderer.getReflectionUI(), String.class);
					}

				};
			}

		};
	}

	protected void openDialog() {
		Object oldValue = data.getValue();

		EncapsulatedObjectFactory encapsulation = new EncapsulatedObjectFactory(swingRenderer.getReflectionUI(),
				data.getType());
		encapsulation.setFieldGetOnly(data.isGetOnly());
		encapsulation.setFieldNullable(false);
		encapsulation.setFieldValueReturnMode(data.getValueReturnMode());
		encapsulation.setFieldCaption("");
		Map<String, Object> properties = new HashMap<String, Object>();
		{
			DesktopSpecificProperty.setSubFormExpanded(properties, true);
			encapsulation.setFieldSpecificProperties(properties);
		}
		encapsulation.setTypeCaption(data.getType().getCaption());
		encapsulation.setTypeModificationStackAccessible(ReflectionUIUtils.canPotentiallyIntegrateSubModifications(
				input.getField().getValueReturnMode(), !input.getField().isGetOnly()));
		
		Accessor<Object> encapsulatedValueAccessor = Accessor.returning(data.getValue(), true);
		Object encapsulated = encapsulation.getInstance(encapsulatedValueAccessor);
		
		ObjectDialogBuilder dialogBuilder = new ObjectDialogBuilder(swingRenderer, this, encapsulated);
		swingRenderer.showDialog(dialogBuilder.build(), true);

		ModificationStack parentModifStack = input.getModificationStack();
		ModificationStack childModifStack = dialogBuilder.getModificationStack();
		String childModifTitle = ControlDataValueModification.getTitle(input.getField());
		IInfo childModifTarget = input.getField();
		IModification commitModif;
		if (input.getField().isGetOnly()) {
			commitModif = null;
		} else {
			commitModif = new ControlDataValueModification(data, encapsulatedValueAccessor.get(), childModifTarget);
		}
		ValueReturnMode childValueReturnMode = input.getField().getValueReturnMode();
		boolean childModifAccepted = (!dialogBuilder.isCancellable()) || dialogBuilder.isOkPressed();
		boolean childValueNew = dialogBuilder.isValueNew();
		if (ReflectionUIUtils.integrateSubModifications(swingRenderer.getReflectionUI(), parentModifStack,
				childModifStack, childModifAccepted, childValueReturnMode, childValueNew, commitModif, childModifTarget,
				childModifTitle)) {
			updateControls();
		}
	}

	protected void updateControls() {
		updateStatusControl();
		updateIconControl();
	}

	protected void updateIconControl() {
		((JLabel) iconControl).setIcon(SwingRendererUtils.getControlDataIcon(swingRenderer, data));
		iconControl.setVisible(((JLabel) iconControl).getIcon() != null);
	}

	protected void updateStatusControl() {
		((TextControl) statusControl).refreshUI();
	}

	@Override
	public boolean displayError(String msg) {
		return false;
	}

	@Override
	public boolean showCaption() {
		return false;
	}

	@Override
	public boolean refreshUI() {
		return false;
	}

	@Override
	public boolean handlesModificationStackUpdate() {
		return true;
	}

	@Override
	public Object getFocusDetails() {
		return null;
	}

	@Override
	public void requestDetailedFocus(Object focusDetails) {
	}

	@Override
	public void validateSubForm() throws Exception {
	}

}
