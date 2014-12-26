package xy.reflect.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;

import org.jdesktop.swingx.JXBusyLabel;

import xy.reflect.ui.control.ICanDisplayErrorControl;
import xy.reflect.ui.control.ICanShowCaptionControl;
import xy.reflect.ui.control.IRefreshableControl;
import xy.reflect.ui.control.MethodControl;
import xy.reflect.ui.control.ModificationStack;
import xy.reflect.ui.control.ModificationStack.IModification;
import xy.reflect.ui.info.IInfoCollectionSettings;
import xy.reflect.ui.info.field.FieldInfoProxy;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.field.InfoCategory;
import xy.reflect.ui.info.field.MultiSubListField.VirtualItem;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.method.MethodInfoProxy;
import xy.reflect.ui.info.type.ArrayTypeInfo;
import xy.reflect.ui.info.type.DefaultBooleanTypeInfo;
import xy.reflect.ui.info.type.DefaultTextualTypeInfo;
import xy.reflect.ui.info.type.DefaultTypeInfo;
import xy.reflect.ui.info.type.FileTypeInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.ITypeInfoSource;
import xy.reflect.ui.info.type.JavaTypeInfoSource;
import xy.reflect.ui.info.type.MethodParametersAsTypeInfo;
import xy.reflect.ui.info.type.PrecomputedTypeInfoInstanceWrapper;
import xy.reflect.ui.info.type.PrecomputedTypeInfoSource;
import xy.reflect.ui.info.type.StandardCollectionTypeInfo;
import xy.reflect.ui.info.type.StandardEnumerationTypeInfo;
import xy.reflect.ui.info.type.StandardMapListTypeInfo;
import xy.reflect.ui.info.type.StandardMapListTypeInfo.StandardMapEntry;
import xy.reflect.ui.util.Accessor;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.reflect.ui.util.component.ScrollPaneOptions;
import xy.reflect.ui.util.component.SimpleLayout;
import xy.reflect.ui.util.component.SimpleLayout.Kind;
import xy.reflect.ui.util.component.WrapLayout;

import com.google.common.collect.MapMaker;

public class ReflectionUI {

	protected Map<JPanel, Object> objectByForm = new MapMaker().weakValues()
			.makeMap();
	protected Map<JPanel, ModificationStack> modificationStackByForm = new MapMaker()
			.weakKeys().makeMap();
	protected Map<JPanel, Map<String, FielControlPlaceHolder>> controlPlaceHolderByFieldNameByForm = new MapMaker()
			.weakKeys().makeMap();

	public static void main(String[] args) {
		ReflectionUI reflectionUI = new ReflectionUI();
		Object object = reflectionUI.onTypeInstanciationRequest(null,
				reflectionUI.getTypeInfo(new JavaTypeInfoSource(Object.class)),
				false);
		if (object == null) {
			return;
		}
		reflectionUI.openObjectFrame(object,
				reflectionUI.getObjectKind(object),
				reflectionUI.getObjectIconImage(object));
	}

	public Map<JPanel, Object> getObjectByForm() {
		return objectByForm;
	}

	public Map<JPanel, ModificationStack> getModificationStackByForm() {
		return modificationStackByForm;
	}

	public boolean canCopy(Object object) {
		if (object == null) {
			return true;
		}
		if (object instanceof Serializable) {
			return true;
		}
		return false;
	}

	public Object copy(Object object) {
		if (object == null) {
			return null;
		}
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(object);
			ByteArrayInputStream bais = new ByteArrayInputStream(
					baos.toByteArray());
			ObjectInputStream ois = new ObjectInputStream(bais);
			Object copy = ois.readObject();
			return copy;
		} catch (Throwable t) {
			throw new ReflectionUIError("Could not copy object: "
					+ t.toString());
		}
	}

	public void openObjectFrame(Object object, String title, Image iconImage) {
		JPanel form = createObjectForm(object);
		JFrame frame = createFrame(form, title, iconImage,
				createCommonToolbarControls(form));
		frame.setVisible(true);
	}

	public List<Component> createCommonToolbarControls(final JPanel form) {
		final ModificationStack stack = getModificationStackByForm().get(form);
		if (stack == null) {
			return null;
		}
		List<Component> result = new ArrayList<Component>();
		result.addAll(stack.createControls(this));
		return result;
	}

	public JFrame createFrame(Component content, String title, Image iconImage,
			List<? extends Component> toolbarControls) {
		final JFrame frame = new JFrame();
		frame.setTitle(title);
		if (iconImage == null) {
			frame.setIconImage(new BufferedImage(1, 1,
					BufferedImage.TYPE_INT_ARGB));
		} else {
			frame.setIconImage(iconImage);
		}
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		frame.getContentPane().setLayout(new BorderLayout());

		if (content != null) {
			JScrollPane scrollPane = new JScrollPane(new ScrollPaneOptions(
					content, true, false));
			frame.getContentPane().add(scrollPane, BorderLayout.CENTER);
		}

		if (toolbarControls != null) {
			JPanel toolbar = new JPanel();
			toolbar.setBorder(BorderFactory.createRaisedBevelBorder());
			toolbar.setLayout(new FlowLayout(FlowLayout.CENTER));
			for (Component tool : toolbarControls) {
				toolbar.add(tool);
			}
			frame.getContentPane().add(toolbar, BorderLayout.SOUTH);
		}
		frame.pack();
		frame.setLocationRelativeTo(null);
		adjustWindowBounds(frame);
		return frame;
	}

	public JPanel createObjectForm(Object object) {
		return createObjectForm(object, IInfoCollectionSettings.DEFAULT);
	}

	public JPanel createObjectForm(Object object,
			IInfoCollectionSettings settings) {
		JPanel result = new JPanel();
		getObjectByForm().put(result, object);
		getModificationStackByForm().put(result,
				new ModificationStack(getObjectKind(object)));
		fillForm(object, result, settings);
		return result;
	}

	public void fillForm(Object object, JPanel form,
			IInfoCollectionSettings settings) {
		ITypeInfo type = getTypeInfo(getTypeInfoSource(object));

		Map<InfoCategory, List<FielControlPlaceHolder>> fieldControlPlaceHoldersByCategory = new HashMap<InfoCategory, List<FielControlPlaceHolder>>();
		List<IFieldInfo> fields = type.getFields();
		for (IFieldInfo field : fields) {
			if (settings.excludeField(field)) {
				continue;
			}
			if (!field.isReadOnly()) {
				field = makeFieldModificationsUndoable(field, form);
				field = refreshOtherFieldsAfterFieldModification(field, form);
			}
			FielControlPlaceHolder fieldControlPlaceHolder = createFieldControlPlaceHolder(
					object, field, form);
			{
				Map<String, FielControlPlaceHolder> controlPlaceHolderByFieldName = controlPlaceHolderByFieldNameByForm
						.get(form);
				if (controlPlaceHolderByFieldName == null) {
					controlPlaceHolderByFieldName = new HashMap<String, FielControlPlaceHolder>();
					controlPlaceHolderByFieldNameByForm.put(form,
							controlPlaceHolderByFieldName);
				}
				controlPlaceHolderByFieldName.put(field.getName(),
						fieldControlPlaceHolder);
			}
			{
				InfoCategory category = field.getCategory();
				if (category == null) {
					category = getNullInfoCategory();
				}
				List<FielControlPlaceHolder> fieldControlPlaceHolders = fieldControlPlaceHoldersByCategory
						.get(category);
				if (fieldControlPlaceHolders == null) {
					fieldControlPlaceHolders = new ArrayList<ReflectionUI.FielControlPlaceHolder>();
					fieldControlPlaceHoldersByCategory.put(category,
							fieldControlPlaceHolders);
				}
				fieldControlPlaceHolders.add(fieldControlPlaceHolder);
			}
		}

		Map<InfoCategory, List<Component>> methodControlsByCategory = new HashMap<InfoCategory, List<Component>>();
		List<IMethodInfo> methods = type.getMethods();
		for (IMethodInfo method : methods) {
			if (settings.excludeMethod(method)) {
				continue;
			}
			if (settings.allReadOnly()) {
				if (!method.isReadOnly()) {
					continue;
				}
			} else {
				if (!method.isReadOnly()) {
					method = makeMethodModificationsUndoable(method, form);
					method = refreshFieldsAfterMethodModifications(method, form);
				}
			}
			Component methodControl = createMethodControl(object, method);
			{
				InfoCategory category = method.getCategory();
				if (category == null) {
					category = getNullInfoCategory();
				}
				List<Component> methodControls = methodControlsByCategory
						.get(category);
				if (methodControls == null) {
					methodControls = new ArrayList<Component>();
					methodControlsByCategory.put(category, methodControls);
				}
				methodControls.add(methodControl);
			}
		}

		SortedSet<InfoCategory> allCategories = new TreeSet<InfoCategory>();
		allCategories.addAll(fieldControlPlaceHoldersByCategory.keySet());
		allCategories.addAll(methodControlsByCategory.keySet());
		if (allCategories.size() == 1) {
			List<FielControlPlaceHolder> fieldControlPlaceHolders = fieldControlPlaceHoldersByCategory
					.get(allCategories.first());
			if (fieldControlPlaceHolders == null) {
				fieldControlPlaceHolders = Collections.emptyList();
			}
			List<Component> methodControls = methodControlsByCategory
					.get(allCategories.first());
			if (methodControls == null) {
				methodControls = Collections.emptyList();
			}
			layoutControls(fieldControlPlaceHolders, methodControls, form);
		} else if (allCategories.size() > 1) {
			form.setLayout(new BorderLayout());
			form.add(
					createMultipleInfoCategoriesComponent(allCategories,
							fieldControlPlaceHoldersByCategory,
							methodControlsByCategory), BorderLayout.CENTER);
		}
	}

	public Component createMultipleInfoCategoriesComponent(
			final SortedSet<InfoCategory> allCategories,
			Map<InfoCategory, List<FielControlPlaceHolder>> fieldControlPlaceHoldersByCategory,
			Map<InfoCategory, List<Component>> methodControlsByCategory) {
		final JTabbedPane tabbedPane = new JTabbedPane();
		for (final InfoCategory category : allCategories) {
			List<FielControlPlaceHolder> fieldControlPlaceHolders = fieldControlPlaceHoldersByCategory
					.get(category);
			if (fieldControlPlaceHolders == null) {
				fieldControlPlaceHolders = Collections.emptyList();
			}
			List<Component> methodControls = methodControlsByCategory
					.get(category);
			if (methodControls == null) {
				methodControls = Collections.emptyList();
			}

			JPanel tab = new JPanel();
			tabbedPane.addTab(translateUIString(category.getCaption()), tab);
			tab.setLayout(new BorderLayout());

			JPanel tabContent = new JPanel();
			tab.add(tabContent, BorderLayout.NORTH);
			layoutControls(fieldControlPlaceHolders, methodControls, tabContent);

			JPanel buttonsPanel = new JPanel();
			tab.add(buttonsPanel, BorderLayout.SOUTH);
			buttonsPanel.setLayout(new BorderLayout());
			buttonsPanel.setBorder(BorderFactory.createTitledBorder(""));

			ArrayList<InfoCategory> allCategoriesAsList = new ArrayList<InfoCategory>(
					allCategories);
			final int tabIndex = allCategoriesAsList.indexOf(category);
			int tabCount = allCategoriesAsList.size();

			if (tabIndex > 0) {
				JButton previousCategoryButton = new JButton(
						translateUIString("<"));
				buttonsPanel.add(previousCategoryButton, BorderLayout.WEST);
				previousCategoryButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						tabbedPane.setSelectedIndex(tabIndex - 1);
					}
				});
			}

			if (tabIndex < (tabCount - 1)) {
				JButton nextCategoryButton = new JButton(translateUIString(">"));
				buttonsPanel.add(nextCategoryButton, BorderLayout.EAST);
				nextCategoryButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						tabbedPane.setSelectedIndex(tabIndex + 1);
					}
				});
			}

		}
		return tabbedPane;
	}

	public InfoCategory getNullInfoCategory() {
		return new InfoCategory("General", -1);
	}

	public ITypeInfoSource getTypeInfoSource(Object object) {
		if (object instanceof PrecomputedTypeInfoInstanceWrapper) {
			return ((PrecomputedTypeInfoInstanceWrapper) object)
					.getPrecomputedTypeInfoSource();
		} else if (object instanceof StandardMapEntry) {
			return new PrecomputedTypeInfoSource(
					((StandardMapEntry<?, ?>) object).getTypeInfo());
		} else {
			return new JavaTypeInfoSource(object.getClass());
		}
	}

	public FielControlPlaceHolder createFieldControlPlaceHolder(Object object,
			IFieldInfo field, JPanel form) {
		return new FielControlPlaceHolder(object, field, form);
	}

	public IFieldInfo makeFieldModificationsUndoable(final IFieldInfo field,
			final JPanel form) {
		return new FieldInfoProxy(field) {
			@Override
			public void setValue(Object object, Object newValue) {
				ModificationStack stack = getModificationStackByForm()
						.get(form);
				stack.apply(new ModificationStack.SetFieldValueModification(
						ReflectionUI.this, object, field, newValue), false);
			}
		};
	}

	public IMethodInfo makeMethodModificationsUndoable(
			final IMethodInfo method, final JPanel form) {
		return new MethodInfoProxy(method) {

			@Override
			public Object invoke(Object object,
					Map<String, Object> valueByParameterName) {
				ITypeInfo type = getTypeInfo(getTypeInfoSource(object));
				Map<String, Object> fieldValueCopyByFieldName = new HashMap<String, Object>();
				Object COULD_NOT_COPY = new Object();
				for (IFieldInfo field : type.getFields()) {
					Object fieldValue = field.getValue(object);
					Object fieldValueCopy;
					if (!canCopy(fieldValue)) {
						fieldValueCopy = COULD_NOT_COPY;
					} else {
						try {
							fieldValueCopy = copy(fieldValue);
						} catch (Throwable t) {
							fieldValueCopy = COULD_NOT_COPY;
						}
					}
					fieldValueCopyByFieldName.put(field.getName(),
							fieldValueCopy);

				}

				Object result = super.invoke(object, valueByParameterName);

				List<IModification> undoModifs = new ArrayList<ModificationStack.IModification>();
				for (IFieldInfo field : type.getFields()) {
					Object fieldValueCopy = fieldValueCopyByFieldName.get(field
							.getName());
					if (fieldValueCopy == COULD_NOT_COPY) {
						continue;
					}
					Object fieldValue = field.getValue(object);
					if (!ReflectionUIUtils.equalsOrBothNull(fieldValue,
							fieldValueCopy)) {
						if (!field.isReadOnly()) {
							undoModifs
									.add(new ModificationStack.SetFieldValueModification(
											ReflectionUI.this, object, field,
											fieldValueCopy));
						}
					}
				}
				if (undoModifs.size() > 0) {
					ModificationStack stack = getModificationStackByForm().get(
							form);
					stack.pushUndo(new ModificationStack.CompositeModification(
							ModificationStack.getUndoTitle("execution of '"
									+ method.getCaption() + "'"),
							ModificationStack.Order.FIFO, undoModifs));
				}

				return result;
			}

		};
	}

	public IFieldInfo refreshOtherFieldsAfterFieldModification(
			final IFieldInfo field, final JPanel form) {
		return new FieldInfoProxy(field) {
			@Override
			public void setValue(final Object object, Object newValue) {
				super.setValue(object, newValue);
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						ITypeInfo type = getTypeInfo(getTypeInfoSource(object));
						for (IFieldInfo fieldToRefresh : type.getFields()) {
							if (field.equals(fieldToRefresh)) {
								continue;
							}
							refreshFieldControl(form, fieldToRefresh.getName());
						}
					}
				});
			}
		};
	}

	public IMethodInfo refreshFieldsAfterMethodModifications(
			final IMethodInfo method, final JPanel form) {
		return new MethodInfoProxy(method) {
			@Override
			public Object invoke(Object object,
					Map<String, Object> valueByParameterName) {
				Object result = super.invoke(object, valueByParameterName);
				ITypeInfo type = getTypeInfo(getTypeInfoSource(object));
				for (IFieldInfo field : type.getFields()) {
					refreshFieldControl(form, field.getName());
				}
				return result;
			}

		};
	}

	public void refreshFieldControl(JPanel form, String fieldName) {
		Object object = getObjectByForm().get(form);
		ITypeInfo type = getTypeInfo(getTypeInfoSource(object));
		IFieldInfo field = ReflectionUIUtils.findInfoByName(type.getFields(),
				fieldName);
		if (field == null) {
			return;
		}
		Map<String, FielControlPlaceHolder> controlPlaceHolderByFieldName = controlPlaceHolderByFieldNameByForm
				.get(form);
		FielControlPlaceHolder fieldControlPlaceHolder = controlPlaceHolderByFieldName
				.get(field.getName());
		fieldControlPlaceHolder.refreshUI();
	}

	public void layoutControls(
			List<FielControlPlaceHolder> fielControlPlaceHolders,
			final List<Component> methodControls, JPanel parentForm) {
		parentForm.setLayout(new SimpleLayout(Kind.COLUMN));

		JPanel fieldsPanel = new JPanel();
		fieldsPanel.setLayout(new SimpleLayout(Kind.COLUMN));
		for (int i = 0; i < fielControlPlaceHolders.size(); i++) {
			FielControlPlaceHolder fielControlPlaceHolder = fielControlPlaceHolders
					.get(i);
			fielControlPlaceHolder.showCaption();
			SimpleLayout.add(fieldsPanel, fielControlPlaceHolder);
		}

		JPanel methodsPanel = new JPanel();
		methodsPanel.setLayout(new WrapLayout(WrapLayout.CENTER));
		for (final Component methodControl : methodControls) {
			JPanel methodControlContainer = new JPanel() {
				private static final long serialVersionUID = 1L;

				@Override
				public Dimension getPreferredSize() {
					Dimension result = super.getPreferredSize();
					if (result == null) {
						return super.getPreferredSize();
					}
					int maxMethodControlWidth = 0;
					for (final Component methodControl : methodControls) {
						Dimension controlPreferredSize = methodControl
								.getPreferredSize();
						if (controlPreferredSize != null) {
							maxMethodControlWidth = Math.max(
									maxMethodControlWidth,
									controlPreferredSize.width);
						}
					}
					result.width = maxMethodControlWidth;
					return result;
				}
			};
			methodControlContainer.setLayout(new BorderLayout());
			methodControlContainer.add(methodControl, BorderLayout.CENTER);
			methodsPanel.add(methodControlContainer);
		}

		SimpleLayout.add(parentForm, fieldsPanel);
		SimpleLayout.add(parentForm, methodsPanel);
	}

	public void setFieldControlCaption(Component fieldControl, String caption) {
		TitledBorder titledBorder;
		if (caption == null) {
			titledBorder = null;
		} else {
			titledBorder = BorderFactory
					.createTitledBorder(translateUIString(caption));
		}
		((JComponent) fieldControl).setBorder(titledBorder);
	}

	public void handleExceptionsFromDisplayedUI(Component activatorComponent,
			final Throwable t) {
		t.printStackTrace();
		openExceptionDialog(activatorComponent,
				translateUIString("An Error Occured"), t);
	}

	public void openExceptionDialog(Component activatorComponent, String title,
			final Throwable t) {
		JTextArea textArea = new JTextArea(t.toString());
		textArea.setEditable(false);
		textArea.setMargin(new Insets(5, 5, 5, 5));
		textArea.setBorder(BorderFactory.createTitledBorder(""));
		Component errorComponent = new JOptionPane(textArea,
				JOptionPane.ERROR_MESSAGE, JOptionPane.DEFAULT_OPTION, null,
				new Object[] {});

		JDialog[] dialogArray = new JDialog[1];
		dialogArray[0] = createDialog(
				activatorComponent,
				errorComponent,
				title,
				null,
				new ArrayList<Component>(createDialogOkCancelButtons(
						dialogArray, null, null, null, false)), null);
		openDialog(dialogArray[0], true);

	}

	public ITypeInfo getTypeInfo(ITypeInfoSource typeSource) {
		if (typeSource instanceof JavaTypeInfoSource) {
			JavaTypeInfoSource javaTypeSource = (JavaTypeInfoSource) typeSource;
			if (StandardCollectionTypeInfo.isCompatibleWith(javaTypeSource
					.getJavaType())) {
				Class<?> itemType = ReflectionUIUtils.getJavaTypeParameter(
						javaTypeSource.getJavaType(),
						javaTypeSource.ofMember(), Collection.class, 0);
				return new StandardCollectionTypeInfo(this,
						javaTypeSource.getJavaType(), itemType);
			} else if (StandardMapListTypeInfo.isCompatibleWith(javaTypeSource
					.getJavaType())) {
				Class<?> keyType = ReflectionUIUtils.getJavaTypeParameter(
						javaTypeSource.getJavaType(),
						javaTypeSource.ofMember(), Map.class, 0);
				Class<?> valueType = ReflectionUIUtils.getJavaTypeParameter(
						javaTypeSource.getJavaType(),
						javaTypeSource.ofMember(), Map.class, 1);
				return new StandardMapListTypeInfo(this,
						javaTypeSource.getJavaType(), keyType, valueType);
			} else if (javaTypeSource.getJavaType().isArray()) {
				Class<?> itemType = javaTypeSource.getJavaType()
						.getComponentType();
				return new ArrayTypeInfo(this, javaTypeSource.getJavaType(),
						itemType);
			} else if (javaTypeSource.getJavaType().isEnum()) {
				return new StandardEnumerationTypeInfo(this,
						javaTypeSource.getJavaType());
			} else if (DefaultBooleanTypeInfo.isCompatibleWith(javaTypeSource
					.getJavaType())) {
				return new DefaultBooleanTypeInfo(this,
						javaTypeSource.getJavaType());
			} else if (DefaultTextualTypeInfo.isCompatibleWith(javaTypeSource
					.getJavaType())) {
				return new DefaultTextualTypeInfo(this,
						javaTypeSource.getJavaType());
			} else if (FileTypeInfo.isCompatibleWith(javaTypeSource
					.getJavaType())) {
				return new FileTypeInfo(this);
			} else {
				return new DefaultTypeInfo(this, javaTypeSource.getJavaType());
			}
		} else if (typeSource instanceof PrecomputedTypeInfoSource) {
			return ((PrecomputedTypeInfoSource) typeSource)
					.getPrecomputedType();
		} else {
			throw new ReflectionUIError();
		}
	}

	public Component createMethodControl(final Object object,
			final IMethodInfo method) {
		return new MethodControl(this, object, method);
	}

	public String getMethodTitle(Object object, IMethodInfo method,
			Object returnValue, String context) {
		String result = method.getCaption();
		if (object != null) {
			result = composeTitle(getObjectKind(object), result);
		}
		if (returnValue != null) {
			result = composeTitle(result, getObjectKind(returnValue));
		}
		if (context != null) {
			result = composeTitle(result, context);
		}
		return result;
	}

	public String composeTitle(String contextTitle, String localTitle) {
		if (contextTitle == null) {
			return localTitle;
		}
		return contextTitle + " - " + localTitle;
	}

	public boolean onMethodInvocationRequest(
			final Component activatorComponent, final Object object,
			final IMethodInfo method, Object[] returnValueArray,
			boolean displayReturnValue) {
		if (returnValueArray == null) {
			returnValueArray = new Object[1];
		}
		if (method.getParameters().size() > 0) {
			if (!openMethoExecutionSettingDialog(activatorComponent, object,
					method, returnValueArray)) {
				return false;
			}
		} else {
			final Object[] finalReturnValueArray = returnValueArray;
			showBusyDialogWhile(activatorComponent, new Runnable() {
				@Override
				public void run() {
					finalReturnValueArray[0] = method.invoke(object,
							Collections.<String, Object> emptyMap());
				}
			}, getMethodTitle(object, method, null, "Execution"));
		}
		if (displayReturnValue) {
			if (method.getReturnValueType() != null) {
				openMethodReturnValueWindow(activatorComponent, object, method,
						returnValueArray[0]);
			}
		}
		return true;
	}

	public void showBusyDialogWhile(final Component ownerComponent,
			final Runnable runnable, String title) {
		final JXBusyLabel busyLabel = new JXBusyLabel();
		busyLabel.setHorizontalAlignment(SwingConstants.CENTER);
		busyLabel.setText("Please wait...");
		busyLabel.setVerticalTextPosition(SwingConstants.TOP);
		busyLabel.setHorizontalTextPosition(SwingConstants.CENTER);
		final JDialog dialog = createDialog(ownerComponent, busyLabel, title,
				null, null, null);
		busyLabel.setBusy(true);
		new Thread(title) {
			@Override
			public void run() {
				try {
					runnable.run();
				} catch (Throwable t) {
					handleExceptionsFromDisplayedUI(dialog, t);
				} finally {
					busyLabel.setBusy(false);
					dialog.dispose();
				}
			}
		}.start();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			throw new ReflectionUIError(e);
		}
		if (busyLabel.isBusy()) {
			openDialog(dialog, true);
		}
	}

	public boolean openMethoExecutionSettingDialog(
			final Component activatorComponent, final Object object,
			final IMethodInfo method, final Object[] returnValueArray) {
		final Map<String, Object> valueByParameterName = new HashMap<String, Object>();
		JPanel methodForm = createObjectForm(new PrecomputedTypeInfoInstanceWrapper(
				valueByParameterName, new MethodParametersAsTypeInfo(this,
						method)));

		final boolean[] invokedStatusArray = new boolean[] { false };
		final JDialog[] methodDialogArray = new JDialog[1];

		List<Component> toolbarControls = new ArrayList<Component>(
				createDialogOkCancelButtons(methodDialogArray,
						invokedStatusArray, method.getCaption(),
						new Runnable() {
							@Override
							public void run() {
								showBusyDialogWhile(
										activatorComponent,
										new Runnable() {
											@Override
											public void run() {
												returnValueArray[0] = method
														.invoke(object,
																valueByParameterName);
											}
										},
										getMethodTitle(object, method, null,
												"Execution"));
							}

						}, true));

		methodDialogArray[0] = createDialog(activatorComponent, methodForm,
				getMethodTitle(object, method, null, "Setting"), null,
				toolbarControls, null);

		openDialog(methodDialogArray[0], true);
		return invokedStatusArray[0];
	}

	public void openMethodReturnValueWindow(Component activatorComponent,
			Object object, IMethodInfo method, Object returnValue) {
		if (returnValue == null) {
			String msg = "'" + method.getCaption()
					+ "' excution returned no result!";
			showMessageDialog(activatorComponent, msg,
					getMethodTitle(object, method, null, "Result"));
		} else {
			JPanel returnValueControl = createValueForm(
					new Object[] { returnValue },
					IInfoCollectionSettings.DEFAULT);
			JFrame frame = createFrame(
					returnValueControl,
					getMethodTitle(object, method, returnValue,
							"Execution Result"),
					getObjectIconImage(returnValue),
					createCommonToolbarControls(returnValueControl));
			frame.setVisible(true);
		}
	}

	public void showMessageDialog(Component activatorComponent, String msg,
			String title) {
		JDialog[] dialogArray = new JDialog[1];
		openDialog(
				dialogArray[0] = createDialog(
						activatorComponent,
						new JLabel("<HTML><BR>" + msg + "<BR><BR><HTML>",
								SwingConstants.CENTER),
						title,
						null,
						createDialogOkCancelButtons(dialogArray, null, null,
								null, false), null), true);
	}

	public void openObjectDialog(Component parent, Object object, String title,
			Image iconImage, boolean modal) {
		JPanel form = createObjectForm(object);
		JDialog dialog = createDialog(parent, form, title, iconImage,
				createCommonToolbarControls(form), null);
		openDialog(dialog, modal);
	}

	public void openDialog(JDialog dialog, boolean modal) {
		if (modal) {
			dialog.setModalityType(ModalityType.DOCUMENT_MODAL);
			dialog.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
			dialog.setVisible(true);
			dialog.dispose();
		} else {
			dialog.setModalityType(ModalityType.MODELESS);
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		}
	}

	public JDialog createDialog(Component ownerComponent, Component content,
			String title, Image iconImage,
			List<? extends Component> toolbarControls,
			final Runnable whenClosing) {
		Window owner = ReflectionUIUtils
				.getWindowAncestorOrSelf(ownerComponent);
		JDialog dialog = new JDialog(owner, title) {
			protected static final long serialVersionUID = 1L;
			protected boolean disposed = false;

			@Override
			public void dispose() {
				if (disposed) {
					return;
				}
				super.dispose();
				if (whenClosing != null) {
					try {
						whenClosing.run();
					} catch (Throwable t) {
						handleExceptionsFromDisplayedUI(this, t);
					}
				}
				disposed = true;
			}
		};
		dialog.getContentPane().setLayout(new BorderLayout());
		if (content != null) {
			JScrollPane scrollPane = new JScrollPane(content);
			dialog.getContentPane().add(scrollPane, BorderLayout.CENTER);
		}
		if (toolbarControls != null) {
			JPanel toolbar = new JPanel();
			toolbar.setBorder(BorderFactory.createRaisedBevelBorder());
			toolbar.setLayout(new FlowLayout(FlowLayout.CENTER));
			for (Component tool : toolbarControls) {
				toolbar.add(tool);
			}
			dialog.getContentPane().add(toolbar, BorderLayout.SOUTH);
		}
		dialog.pack();
		dialog.setLocationRelativeTo(null);
		dialog.setResizable(true);
		adjustWindowBounds(dialog);
		if (iconImage == null) {
			dialog.setIconImage(new BufferedImage(1, 1,
					BufferedImage.TYPE_INT_ARGB));
		} else {
			dialog.setIconImage(iconImage);
		}
		return dialog;
	}

	public ReflectionUI getSubReflectionUI() {
		return this;
	}

	public String toString(Object object) {
		String result;
		if (object == null) {
			result = null;
		} else {
			result = object.toString();
		}
		return result;
	}

	public String translateUIString(String string) {
		return string;
	}

	public Object onTypeInstanciationRequest(Component activatorComponent,
			ITypeInfo type, boolean silent) {
		List<ITypeInfo> polyTypes = type.getPolymorphicInstanceSubTypes();
		if ((polyTypes != null) && (polyTypes.size() > 0)) {
			if (polyTypes.size() == 1) {
				type = polyTypes.get(0);
			} else {
				if (silent) {
					type = polyTypes.get(0);
				} else {
					type = openSelectionDialog(activatorComponent, polyTypes,
							null, "Choose the type of '" + type.getCaption()
									+ "':", null);
					if (type == null) {
						return null;
					}
				}
			}
		}

		List<IMethodInfo> constructors = type.getConstructors();
		if (constructors.size() == 0) {
			if (type.isConcrete() || silent) {
				throw new ReflectionUIError("Cannot create an object of type '"
						+ type + "': No accessible constructor found");
			} else {
				type = openConcreteClassSelectionDialog(activatorComponent,
						type);
				if (type == null) {
					return null;
				} else {
					return onTypeInstanciationRequest(activatorComponent, type,
							silent);
				}
			}
		}

		constructors = new ArrayList<IMethodInfo>(constructors);
		Collections.sort(constructors, new Comparator<IMethodInfo>() {
			@Override
			public int compare(IMethodInfo o1, IMethodInfo o2) {
				return new Integer(o1.getParameters().size())
						.compareTo(new Integer(o2.getParameters().size()));
			}
		});

		IMethodInfo smallerConstructor = constructors.get(0);
		if (silent) {
			return smallerConstructor.invoke(null,
					Collections.<String, Object> emptyMap());
		} else {
			Object[] returnValueArray = new Object[1];
			onMethodInvocationRequest(activatorComponent, null,
					smallerConstructor, returnValueArray, false);
			return returnValueArray[0];
		}
	}

	@SuppressWarnings("unchecked")
	public <T> T openSelectionDialog(Component parentComponent,
			List<T> choices, T initialSelection, String message, String title) {
		return (T) JOptionPane.showInputDialog(parentComponent,
				translateUIString(message), translateUIString(title),
				JOptionPane.QUESTION_MESSAGE, null, choices.toArray(),
				initialSelection);
	}

	public ITypeInfo openConcreteClassSelectionDialog(
			Component parentComponent, ITypeInfo type) {
		String className = JOptionPane.showInputDialog(parentComponent,
				translateUIString("Class name of the '" + type.getCaption()
						+ "' you want to create:"));
		if (className == null) {
			return null;
		}
		try {
			return getTypeInfo(new JavaTypeInfoSource(Class.forName(className)));
		} catch (ClassNotFoundException e) {
			throw new ReflectionUIError(e);
		}
	}

	public String getObjectKind(Object object) {
		if (object == null) {
			return "(Not found)";
		}
		if (object instanceof VirtualItem) {
			return ((VirtualItem) object).toString();
		}
		if (object instanceof StandardMapEntry<?, ?>) {
			Object key = ((StandardMapEntry<?, ?>) object).getKey();
			return (key == null) ? null : key.toString();
		}
		return getTypeInfo(getTypeInfoSource(object)).getCaption();
	}

	public Image getObjectIconImage(Object item) {
		return null;
	}

	public boolean openValueDialog(Component activatorComponent,
			final Object object, Accessor<Object> valueAccessor,
			IInfoCollectionSettings settings,
			ModificationStack parentModificationStack, String title) {
		boolean[] okPressedArray = new boolean[] { false };
		JDialog dialog = createValueDialog(activatorComponent, object,
				valueAccessor, okPressedArray, settings,
				parentModificationStack, title);
		openDialog(dialog, true);
		return okPressedArray[0];
	}

	public JDialog createValueDialog(final Component activatorComponent,
			final Object object, final Accessor<Object> valueAccessor,
			final boolean[] okPressedArray, IInfoCollectionSettings settings,
			final ModificationStack parentModificationStack, final String title) {

		final Object[] valueArray = new Object[] { valueAccessor.get() };
		final JPanel valueForm = createValueForm(valueArray, settings);

		final JDialog[] dialogArray = new JDialog[1];
		List<Component> toolbarControls = new ArrayList<Component>();
		Image iconImage = null;
		iconImage = getObjectIconImage(valueArray[0]);
		List<Component> commonToolbarControls = createCommonToolbarControls(valueForm);
		if (commonToolbarControls != null) {
			toolbarControls.addAll(commonToolbarControls);
		}
		Runnable whenClosingDialog = new Runnable() {
			@Override
			public void run() {
				if (okPressedArray[0]) {
					Object oldValue = valueAccessor.get();
					if (!oldValue.equals(valueArray[0])) {
						valueAccessor.set(valueArray[0]);
					} else {
						ModificationStack valueModifications = getModificationStackByForm()
								.get(valueForm);
						if (valueModifications != null) {
							List<IModification> undoModifications = new ArrayList<ModificationStack.IModification>();
							undoModifications
									.addAll(Arrays.asList(valueModifications
											.getUndoModifications(ModificationStack.Order.LIFO)));
							parentModificationStack
									.pushUndo(new ModificationStack.CompositeModification(
											ModificationStack
													.getUndoTitle(title),
											ModificationStack.Order.LIFO,
											undoModifications));
						}
					}
				} else {
					ModificationStack stack = getModificationStackByForm().get(
							valueForm);
					if (stack != null) {
						stack.undoAll(false);
					}
				}
			}
		};
		toolbarControls.addAll(createDialogOkCancelButtons(dialogArray,
				okPressedArray, "OK", null, true));

		dialogArray[0] = createDialog(activatorComponent, valueForm, title,
				iconImage, toolbarControls, whenClosingDialog);

		return dialogArray[0];
	}

	public List<JButton> createDialogOkCancelButtons(
			final JDialog[] dialogArray, final boolean[] ok, String okCaption,
			final Runnable okAction, boolean createCancelButton) {
		List<JButton> result = new ArrayList<JButton>();

		final JButton okButton = new JButton(
				translateUIString((okCaption != null) ? okCaption : "OK"));
		result.add(okButton);
		if (ok != null) {
			ok[0] = false;
		}
		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					if (okAction != null) {
						okAction.run();
					}
					if (ok != null) {
						ok[0] = true;
					}
				} catch (Throwable t) {
					handleExceptionsFromDisplayedUI(okButton, t);
				} finally {
					dialogArray[0].dispose();
				}
			}
		});

		if (createCancelButton) {
			final JButton cancelButton = new JButton(
					translateUIString("Cancel"));
			result.add(cancelButton);
			cancelButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (ok != null) {
						ok[0] = false;
					}
					dialogArray[0].dispose();
				}
			});
		}

		return result;
	}

	public JPanel createValueForm(final Object[] valueArray,
			final IInfoCollectionSettings settings) {
		final JPanel result;
		if (!getTypeInfo(getTypeInfoSource(valueArray[0]))
				.hasCustomFieldControl()) {
			result = getSubReflectionUI().createObjectForm(valueArray[0],
					settings);
		} else {
			IFieldInfo virtualField = new IFieldInfo() {

				@Override
				public void setValue(Object object, Object value) {
					valueArray[0] = value;
				}

				@Override
				public boolean isReadOnly() {
					return settings.allReadOnly();
				}

				@Override
				public boolean isNullable() {
					return false;
				}

				@Override
				public Object getValue(Object object) {
					return valueArray[0];
				}

				@Override
				public ITypeInfo getType() {
					return getTypeInfo(getTypeInfoSource(valueArray[0]));
				}

				@Override
				public String getName() {
					return "";
				}

				@Override
				public String getCaption() {
					return "Value";
				}

				@Override
				public InfoCategory getCategory() {
					return null;
				}
			};
			Component fieldControl = virtualField.getType().createFieldControl(
					null, virtualField);
			result = new JPanel();
			result.setLayout(new BorderLayout());
			result.add(fieldControl, BorderLayout.CENTER);
		}
		return result;
	}

	public String getFieldTitle(Object object, IFieldInfo field) {
		return composeTitle(
				composeTitle(getObjectKind(object), field.getCaption()),
				getObjectKind(field.getValue(object)));
	}

	public void adjustWindowBounds(Window window) {
		Rectangle bounds = window.getBounds();
		Rectangle maxBounds = GraphicsEnvironment.getLocalGraphicsEnvironment()
				.getMaximumWindowBounds();
		if (bounds.width < maxBounds.width / 3) {
			bounds.grow((maxBounds.width / 3 - bounds.width) / 2, 0);
		}
		bounds = maxBounds.intersection(bounds);
		window.setBounds(bounds);
	}

	protected class FielControlPlaceHolder extends JPanel implements
			IRefreshableControl, ICanShowCaptionControl,
			ICanDisplayErrorControl {

		protected static final long serialVersionUID = 1L;
		protected Object object;
		protected IFieldInfo field;
		protected Component fieldControl;
		protected boolean captionShown = true;

		public FielControlPlaceHolder(Object object, IFieldInfo field,
				JPanel form) {
			super();
			this.object = object;
			field = handleValueChangeErrors(field);
			this.field = field;
			setLayout(new BorderLayout());
			refreshUI();
		}

		protected IFieldInfo handleValueChangeErrors(IFieldInfo field) {
			return new FieldInfoProxy(field) {

				@Override
				public void setValue(Object object, Object value) {
					try {
						super.setValue(object, value);
						displayError(null);
					} catch (final Throwable t) {
						SwingUtilities.invokeLater(new Runnable() {
							@Override
							public void run() {
								displayError(t.toString());
							}
						});

					}
				}

			};
		}

		@Override
		public void refreshUI() {
			boolean shouldUpdateCaption = false;
			if (fieldControl == null) {
				fieldControl = field.getType()
						.createFieldControl(object, field);
				add(fieldControl, BorderLayout.CENTER);
				if (captionShown) {
					shouldUpdateCaption = true;
				}
			} else {
				if (fieldControl instanceof IRefreshableControl) {
					((IRefreshableControl) fieldControl).refreshUI();
				} else {
					remove(fieldControl);
					fieldControl = null;
					refreshUI();
				}
			}

			if (shouldUpdateCaption) {
				updateCaption();
			}

			ReflectionUIUtils.updateLayout(this);
		}

		@Override
		public void showCaption() {
			updateCaption();
			captionShown = true;
		}

		protected void updateCaption() {
			if (fieldControl instanceof ICanShowCaptionControl) {
				((ICanShowCaptionControl) fieldControl).showCaption();
			} else {
				setFieldControlCaption(fieldControl, field.getCaption());
			}
			captionShown = true;
		}

		@Override
		public void displayError(String error) {
			if (fieldControl instanceof ICanDisplayErrorControl) {
				((ICanDisplayErrorControl) fieldControl).displayError(error);
			} else {
				if (error != null) {
					handleExceptionsFromDisplayedUI(fieldControl,
							new ReflectionUIError(error));
					refreshUI();
				}
			}
		}

	}

}