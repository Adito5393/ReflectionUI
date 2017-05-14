package xy.reflect.ui.control.swing;

import java.awt.BorderLayout;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import xy.reflect.ui.control.CustomContext;
import xy.reflect.ui.control.FieldControlDataProxy;
import xy.reflect.ui.control.IContext;
import xy.reflect.ui.control.IFieldControlData;
import xy.reflect.ui.control.IFieldControlInput;
import xy.reflect.ui.control.swing.editor.AbstractEditFormBuilder;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.info.IInfo;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.menu.MenuModel;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.factory.PolymorphicTypeOptionsFactory;
import xy.reflect.ui.undo.ControlDataValueModification;
import xy.reflect.ui.undo.IModification;
import xy.reflect.ui.undo.ModificationProxy;
import xy.reflect.ui.undo.ModificationStack;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.reflect.ui.util.SwingRendererUtils;

public class PolymorphicControl extends JPanel implements IAdvancedFieldControl {

	protected static final long serialVersionUID = 1L;
	protected SwingRenderer swingRenderer;
	protected IFieldControlData data;

	protected ITypeInfo polymorphicType;
	protected PolymorphicTypeOptionsFactory typeOptionsFactory;

	protected AbstractEditFormBuilder typeEnumerationControlBuilder;
	protected AbstractEditFormBuilder dynamicControlBuilder;
	protected JPanel dynamicControl;
	protected JPanel typeEnumerationControl;

	protected ITypeInfo lastInstanceType;
	protected IFieldControlInput input;

	public PolymorphicControl(final SwingRenderer swingRenderer, IFieldControlInput input) {
		this.swingRenderer = swingRenderer;
		this.input = input;
		this.data = input.getControlData();
		this.polymorphicType = input.getControlData().getType();
		this.typeOptionsFactory = new PolymorphicTypeOptionsFactory(swingRenderer.getReflectionUI(), polymorphicType);

		setLayout(new BorderLayout());
		if (data.getCaption().length() > 0) {
			setBorder(BorderFactory.createTitledBorder(swingRenderer.prepareStringToDisplay(data.getCaption())));
		}
		refreshUI();
	}

	protected JPanel createTypeEnumerationControl() {
		typeEnumerationControlBuilder = new AbstractEditFormBuilder() {

			ITypeInfo enumType = swingRenderer.getReflectionUI()
					.getTypeInfo(typeOptionsFactory.getInstanceTypeInfoSource());
			Map<ITypeInfo, Object> instanceByEnumerationValueCache = new HashMap<ITypeInfo, Object>();

			@Override
			public IContext getContext() {
				return input.getContext();
			}

			@Override
			public IContext getSubContext() {
				return null;
			}

			@Override
			public boolean isObjectFormExpanded() {
				return false;
			}

			@Override
			public boolean isObjectNullValueDistinct() {
				return data.isNullValueDistinct();
			}

			@Override
			public Object getInitialObjectValue() {
				Object instance = data.getValue();
				if (instance == null) {
					return null;
				}
				ITypeInfo selectedType = ReflectionUIUtils.getFirstKeyFromValue(instanceByEnumerationValueCache,
						instance);
				if (selectedType == null) {
					selectedType = typeOptionsFactory.guessSubType(instance);
					instanceByEnumerationValueCache.put(selectedType, instance);
				}
				return typeOptionsFactory.getInstance(selectedType);
			}

			@Override
			public ITypeInfo getObjectDeclaredType() {
				return enumType;
			}

			@Override
			public ValueReturnMode getObjectValueReturnMode() {
				return ValueReturnMode.CALCULATED;
			}

			@Override
			public boolean canCommit() {
				return !data.isGetOnly();
			}

			@Override
			protected boolean shouldAcceptNewObjectValue(Object value) {
				Object instance;
				if (value != null) {
					ITypeInfo selectedSubType = (ITypeInfo) typeOptionsFactory.unwrapInstance(value);
					instance = instanceByEnumerationValueCache.get(selectedSubType);
					if (instance == null) {
						try {
							instance = swingRenderer.onTypeInstanciationRequest(PolymorphicControl.this,
									selectedSubType);
						} catch (Throwable t) {
							instance = null;
						}
						if (instance == null) {
							SwingUtilities.invokeLater(new Runnable() {
								@Override
								public void run() {
									refreshTypeEnumerationControl();
								}
							});
							return false;
						}
						instanceByEnumerationValueCache.put(selectedSubType, instance);
					}
				}
				return true;
			}

			@Override
			public IModification createCommitModification(final Object value) {

				return new ModificationProxy(IModification.NULL_MODIFICATION) {

					@Override
					public String toString() {
						return "CommitModification [editor=PolymorphicControlEnumeration, data=" + data + "]";
					}

					@Override
					public IModification applyAndGetOpposite() {
						Object instance;
						if (value == null) {
							instance = null;
							instanceByEnumerationValueCache.clear();
						} else {
							ITypeInfo selectedSubType = (ITypeInfo) typeOptionsFactory.unwrapInstance(value);
							instance = instanceByEnumerationValueCache.get(selectedSubType);
							if (instance == null) {
								throw new ReflectionUIError();
							}
						}
						return new ControlDataValueModification(new FieldControlDataProxy(data) {
							@Override
							public void setValue(Object value) {
								try {
									super.setValue(value);
								} finally {
									SwingUtilities.invokeLater(new Runnable() {										
										@Override
										public void run() {
											refreshDynamicControl();
										}
									});
								}
							}
						}, instance, input.getModificationsTarget()).applyAndGetOpposite();
					}
				};
			}

			@Override
			public SwingRenderer getSwingRenderer() {
				return swingRenderer;
			}

			@Override
			public String getCumulatedModificationsTitle() {
				return ControlDataValueModification.getTitle(input.getModificationsTarget());
			}

			@Override
			public IInfo getCumulatedModificationsTarget() {
				return input.getModificationsTarget();
			}

			@Override
			public IInfoFilter getObjectFormFilter() {
				return IInfoFilter.DEFAULT;
			}

			@Override
			public ModificationStack getParentObjectModificationStack() {
				return input.getModificationStack();
			}

		};
		return typeEnumerationControlBuilder.createForm(true);
	}

	protected void refreshTypeEnumerationControl() {
		if (typeEnumerationControl != null) {
			typeEnumerationControlBuilder.refreshEditForm(typeEnumerationControl);
		} else {
			add(typeEnumerationControl = createTypeEnumerationControl(), BorderLayout.NORTH);
			SwingRendererUtils.handleComponentSizeChange(this);
		}
	}

	protected String getEnumerationValueCaption(ITypeInfo actualFieldValueType) {
		return actualFieldValueType.getCaption();
	}

	protected JPanel createDynamicControl(final ITypeInfo instanceType) {
		dynamicControlBuilder = new AbstractEditFormBuilder() {

			@Override
			public IContext getContext() {
				return input.getContext();
			}

			@Override
			public IContext getSubContext() {
				return new CustomContext("PolymorphicInstance");
			}

			@Override
			public boolean isObjectFormExpanded() {
				return false;
			}

			@Override
			public boolean isObjectNullValueDistinct() {
				return false;
			}

			@Override
			public boolean canCommit() {
				return !data.isGetOnly();
			}

			@Override
			public IModification createCommitModification(Object newObjectValue) {
				return new ControlDataValueModification(data, newObjectValue, input.getModificationsTarget());
			}

			@Override
			public SwingRenderer getSwingRenderer() {
				return swingRenderer;
			}

			@Override
			public ValueReturnMode getObjectValueReturnMode() {
				return data.getValueReturnMode();
			}

			@Override
			public String getCumulatedModificationsTitle() {
				return ControlDataValueModification.getTitle(input.getModificationsTarget());
			}

			@Override
			public IInfo getCumulatedModificationsTarget() {
				return input.getModificationsTarget();
			}

			@Override
			public IInfoFilter getObjectFormFilter() {
				IInfoFilter result = data.getFormControlFilter();
				if (result == null) {
					result = IInfoFilter.DEFAULT;
				}
				return result;
			}

			@Override
			public ITypeInfo getObjectDeclaredType() {
				return instanceType;
			}

			@Override
			public ModificationStack getParentObjectModificationStack() {
				return input.getModificationStack();
			}

			@Override
			public Object getInitialObjectValue() {
				return data.getValue();
			}

		};
		return dynamicControlBuilder.createForm(true);
	}

	protected void refreshDynamicControl() {
		ITypeInfo instanceType = (ITypeInfo) typeOptionsFactory
				.unwrapInstance(typeEnumerationControlBuilder.getCurrentObjectValue());
		if ((lastInstanceType == null) && (instanceType == null)) {
			return;
		} else if ((lastInstanceType != null) && (instanceType == null)) {
			remove(dynamicControl);
			dynamicControl = null;
			SwingRendererUtils.handleComponentSizeChange(this);
		} else if ((lastInstanceType == null) && (instanceType != null)) {
			dynamicControl = createDynamicControl(instanceType);
			add(dynamicControl, BorderLayout.CENTER);
			SwingRendererUtils.handleComponentSizeChange(this);
		} else {
			if (lastInstanceType.equals(instanceType)) {
				dynamicControlBuilder.refreshEditForm(dynamicControl);
			} else {
				remove(dynamicControl);
				dynamicControl = null;
				dynamicControl = createDynamicControl(instanceType);
				add(dynamicControl, BorderLayout.CENTER);
				SwingRendererUtils.handleComponentSizeChange(this);
			}
		}
		lastInstanceType = instanceType;
	}

	@Override
	public boolean refreshUI() {
		refreshTypeEnumerationControl();
		refreshDynamicControl();
		return true;
	}

	@Override
	public boolean showsCaption() {
		return true;
	}

	@Override
	public boolean displayError(String msg) {
		return false;
	}

	@Override
	public boolean handlesModificationStackUpdate() {
		return true;
	}

	@Override
	public boolean requestCustomFocus() {
		return SwingRendererUtils.requestAnyComponentFocus(typeEnumerationControl, swingRenderer);
	}

	@Override
	public void validateSubForm() throws Exception {
		if (dynamicControl != null) {
			swingRenderer.validateForm(dynamicControl);
		}
	}

	@Override
	public void addMenuContribution(MenuModel menuModel) {
		if (dynamicControl != null) {
			swingRenderer.addFormMenuContribution(dynamicControl, menuModel);
		}
	}

	@Override
	public String toString() {
		return "PolymorphicControl [data=" + data + "]";
	}

}
