package xy.reflect.ui.control.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.control.swing.SwingRenderer.FieldControlPlaceHolder;
import xy.reflect.ui.control.swing.SwingRenderer.MethodControlPlaceHolder;
import xy.reflect.ui.control.swing.SwingRenderer.SwingSpecificProperty;
import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.field.FieldInfoProxy;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.custom.BooleanTypeInfo;
import xy.reflect.ui.info.type.custom.FileTypeInfo;
import xy.reflect.ui.info.type.iterable.IListTypeInfo;
import xy.reflect.ui.info.type.iterable.structure.IListStructuralInfo;
import xy.reflect.ui.info.type.iterable.structure.column.IColumnInfo;
import xy.reflect.ui.info.type.source.ITypeInfoSource;
import xy.reflect.ui.info.type.util.InfoCustomizations;
import xy.reflect.ui.info.type.util.InfoProxyGenerator;
import xy.reflect.ui.info.type.util.InfoCustomizations.ColumnCustomization;
import xy.reflect.ui.info.type.util.InfoCustomizations.FieldCustomization;
import xy.reflect.ui.info.type.util.InfoCustomizations.ListStructureCustomization;
import xy.reflect.ui.info.type.util.InfoCustomizations.MethodCustomization;
import xy.reflect.ui.info.type.util.InfoCustomizations.TypeCustomization;
import xy.reflect.ui.util.FileUtils;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.reflect.ui.util.SwingRendererUtils;
import xy.reflect.ui.util.SystemProperties;

@SuppressWarnings("unused")
public class SwingCustomizer extends SwingRenderer {

	protected CustomizationTools customizationTools;
	protected InfoCustomizations infoCustomizations;
	protected String infoCustomizationsOutputFilePath;

	public SwingCustomizer(ReflectionUI reflectionUI, InfoCustomizations infoCustomizations,
			String infoCustomizationsOutputFilePath) {
		super(reflectionUI);
		customizationTools = createCustomizationTools();
		this.infoCustomizations = infoCustomizations;
		this.infoCustomizationsOutputFilePath = infoCustomizationsOutputFilePath;
		if (infoCustomizationsOutputFilePath != null) {
			File file = new File(infoCustomizationsOutputFilePath);
			if (!file.exists()) {
				try {
					infoCustomizations.saveToFile(file);
				} catch (IOException e) {
					throw new ReflectionUIError(e);
				}
			}
		}
	}

	protected CustomizationTools createCustomizationTools() {
		return new CustomizationTools();
	}

	@Override
	public void fillForm(JPanel form, Object object, ITypeInfo type) {
		if (areCustomizationsEditable()) {
			JPanel mainCustomizationsControl = new JPanel();
			mainCustomizationsControl.setLayout(new BorderLayout());
			mainCustomizationsControl.add(customizationTools.createTypeInfoCustomizer(type.getName()),
					BorderLayout.CENTER);
			mainCustomizationsControl.add(customizationTools.createSaveControl(), BorderLayout.EAST);
			form.add(SwingRendererUtils.flowInLayout(mainCustomizationsControl, FlowLayout.CENTER), BorderLayout.NORTH);
		}
		super.fillForm(form, object, type);
	}

	@Override
	public FieldControlPlaceHolder createFieldControlPlaceHolder(Object object, IFieldInfo field) {
		return new FieldControlPlaceHolder(object, field) {
			private static final long serialVersionUID = 1L;
			protected Component infoCustomizationsComponent;

			@Override
			public void refreshUI(boolean recreate) {
				if (areCustomizationsEditable()) {
					refreshInfoCustomizationsControl();
				}
				super.refreshUI(recreate);
			}

			protected void refreshInfoCustomizationsControl() {
				if (infoCustomizationsComponent == null) {
					ITypeInfo customizedType = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(object));
					infoCustomizationsComponent = customizationTools.createFieldInfoCustomizer(customizedType,
							field.getName());
					add(infoCustomizationsComponent, BorderLayout.EAST);
					handleComponentSizeChange(this);
				} else {
					remove(infoCustomizationsComponent);
					infoCustomizationsComponent = null;
					refreshInfoCustomizationsControl();
				}
			}

		};
	}

	@Override
	public MethodControlPlaceHolder createMethodControlPlaceHolder(Object object, IMethodInfo method) {
		return new MethodControlPlaceHolder(object, method) {
			private static final long serialVersionUID = 1L;
			protected Component infoCustomizationsComponent;

			@Override
			public void refreshUI(boolean recreate) {
				if (areCustomizationsEditable()) {
					refreshInfoCustomizationsControl();
				}
				super.refreshUI(recreate);
			}

			protected void refreshInfoCustomizationsControl() {
				if (infoCustomizationsComponent == null) {
					ITypeInfo customizedType = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(object));
					infoCustomizationsComponent = customizationTools.createMethodInfoCustomizer(customizedType,
							ReflectionUIUtils.getMethodInfoSignature(method));
					add(infoCustomizationsComponent, BorderLayout.WEST);
					handleComponentSizeChange(this);
				} else {
					remove(infoCustomizationsComponent);
					infoCustomizationsComponent = null;
					refreshInfoCustomizationsControl();
				}
			}
		};
	}

	protected ImageIcon getCustomizationIcon() {
		return SwingRendererUtils.CUSTOMIZATION_ICON;
	}

	protected boolean areCustomizationsEditable() {
		return SystemProperties.areInfoCustomizationToolsAuthorized() && (infoCustomizationsOutputFilePath != null);
	}

	protected class CustomizationTools {
		protected SwingRenderer customizationToolsRenderer;
		protected ReflectionUI customizationToolsUI;
		protected InfoCustomizations customizationToolsCustomizations;

		public CustomizationTools() {
			customizationToolsCustomizations = new InfoCustomizations();
			URL url = ReflectionUI.class.getResource("resource/customizations-tools.icu");
			try {
				File customizationsFile = FileUtils.getStreamAsFile(url.openStream());
				String customizationsFilePath = customizationsFile.getPath();
				customizationToolsCustomizations.loadFromFile(new File(customizationsFilePath));
			} catch (IOException e) {
				throw new ReflectionUIError(e);
			}
			customizationToolsUI = createCustomizationToolsUI();
			customizationToolsRenderer = createCustomizationToolsRenderer();

		}

		protected SwingRenderer createCustomizationToolsRenderer() {
			if (SystemProperties.isInfoCustomizationToolsCustomizationAllowed()) {
				String customizationToolsCustomizationsOutputFilePath = System
						.getProperty(SystemProperties.INFO_CUSTOMIZATION_TOOLS_CUSTOMIZATIONS_FILE_PATH);
				return new SwingCustomizer(customizationToolsUI, customizationToolsCustomizations,
						customizationToolsCustomizationsOutputFilePath) {

					@Override
					protected CustomizationTools createCustomizationTools() {
						return new CustomizationTools() {

							@Override
							protected SwingRenderer createCustomizationToolsRenderer() {
								return new SwingRenderer(this.customizationToolsUI);
							}

						};
					}

				};
			} else {
				return new SwingRenderer(customizationToolsUI);
			}
		}

		protected ReflectionUI createCustomizationToolsUI() {
			return new ReflectionUI() {

				ReflectionUI thisReflectionUI = this;

				@Override
				public ITypeInfo getTypeInfo(ITypeInfoSource typeSource) {
					ITypeInfo result = super.getTypeInfo(typeSource);
					result = new InfoProxyGenerator() {
						@Override
						protected List<IFieldInfo> getFields(ITypeInfo type) {
							if (type.getName().equals(TypeCustomization.class.getName())) {
								List<IFieldInfo> result = new ArrayList<IFieldInfo>(super.getFields(type));
								result.add(getTypeIconImageFileField());
								return result;
							} else if (type.getName().equals(FieldCustomization.class.getName())) {
								List<IFieldInfo> result = new ArrayList<IFieldInfo>(super.getFields(type));
								result.add(getEmbeddedFormCreationField());
								return result;
							} else {
								return super.getFields(type);
							}
						}

					}.get(result);
					result = customizationToolsCustomizations.get(thisReflectionUI, result);
					return result;
				}

			};
		}

		protected IFieldInfo getEmbeddedFormCreationField() {
			return new IFieldInfo() {

				@Override
				public String getName() {
					return "expandSubForm";
				}

				@Override
				public String getCaption() {
					return "Expand Sub-form";
				}

				@Override
				public String getOnlineHelp() {
					return null;
				}

				@Override
				public Map<String, Object> getSpecificProperties() {
					return Collections.emptyMap();
				}

				@Override
				public ITypeInfo getType() {
					return new BooleanTypeInfo(customizationToolsRenderer.getReflectionUI(), boolean.class);
				}

				@Override
				public Object getValue(Object object) {
					FieldCustomization f = (FieldCustomization) object;
					Map<String, Object> properties = f.getSpecificProperties();
					if (properties == null) {
						return false;
					}
					return Boolean.TRUE.equals(properties.get(SwingSpecificProperty.CREATE_EMBEDDED_FORM));
				}

				@Override
				public Object[] getValueOptions(Object object) {
					return null;
				}

				@Override
				public void setValue(Object object, Object value) {
					FieldCustomization f = (FieldCustomization) object;
					Map<String, Object> properties = f.getSpecificProperties();
					if (properties == null) {
						properties = new HashMap<String, Object>();
						f.setSpecificProperties(properties);
					}
					properties.put(SwingSpecificProperty.CREATE_EMBEDDED_FORM, true);
				}

				@Override
				public boolean isNullable() {
					return false;
				}

				@Override
				public boolean isGetOnly() {
					return false;
				}

				@Override
				public InfoCategory getCategory() {
					return null;
				}

				@Override
				public String toString() {
					return getCaption();
				}

			};
		}

		protected IFieldInfo getTypeIconImageFileField() {
			return new IFieldInfo() {

				@Override
				public String getName() {
					return "iconImageFile";
				}

				@Override
				public String getCaption() {
					return "Icon Image File";
				}

				@Override
				public String getOnlineHelp() {
					return null;
				}

				@Override
				public Map<String, Object> getSpecificProperties() {
					return Collections.emptyMap();
				}

				@Override
				public ITypeInfo getType() {
					return new FileTypeInfo(customizationToolsRenderer.getReflectionUI());
				}

				@Override
				public Object getValue(Object object) {
					TypeCustomization t = (TypeCustomization) object;
					Map<String, Object> properties = t.getSpecificProperties();
					if (properties == null) {
						return new File("");
					}
					String filePath = (String) properties.get(SwingRenderer.SwingSpecificProperty.KEY_ICON_IMAGE_PATH);
					if (filePath == null) {
						return new File("");
					}
					String filePathKind = (String) properties
							.get(SwingRenderer.SwingSpecificProperty.KEY_ICON_IMAGE_PATH_KIND);
					if (SwingSpecificProperty.VALUE_PATH_TYPE_KIND_CLASSPATH_RESOURCE.equals(filePathKind)) {
						filePath = getClassPathResourcePrefix() + filePath;
					}
					return new File(filePath);
				}

				private String getClassPathResourcePrefix() {
					return "<class-path-resource> ";
				}

				@Override
				public Object[] getValueOptions(Object object) {
					return null;
				}

				@Override
				public void setValue(Object object, Object value) {
					TypeCustomization t = (TypeCustomization) object;
					File file = (File) value;
					String filePath = file.getPath();
					if (file == null) {
						filePath = null;
					} else {
						if (filePath.length() == 0) {
							filePath = null;
						}
					}
					String filePathKind = null;
					if (filePath != null) {
						if (filePath.startsWith(getClassPathResourcePrefix())) {
							filePath = filePath.substring(getClassPathResourcePrefix().length());
							filePathKind = SwingSpecificProperty.VALUE_PATH_TYPE_KIND_CLASSPATH_RESOURCE;
						} else {
							List<PathKindOption> pathKindOptions = getPathKindOptions(filePath);
							PathKindOption chosenPathKindOption;
							if (pathKindOptions.size() == 1) {
								chosenPathKindOption = pathKindOptions.get(0);
							} else {
								chosenPathKindOption = customizationToolsRenderer.openSelectionDialog(null,
										pathKindOptions, null, "Choose an option",
										customizationToolsRenderer.getReflectionUI().getObjectTitle(t));
								if (chosenPathKindOption == null) {
									return;
								}
							}
							filePath = chosenPathKindOption.path;
							filePathKind = chosenPathKindOption.pathKind;
						}
					}
					Map<String, Object> properties = t.getSpecificProperties();
					if (properties == null) {
						properties = new HashMap<String, Object>();
						t.setSpecificProperties(properties);
					}
					properties.put(SwingRenderer.SwingSpecificProperty.KEY_ICON_IMAGE_PATH, filePath);
					properties.put(SwingRenderer.SwingSpecificProperty.KEY_ICON_IMAGE_PATH_KIND, filePathKind);
				}

				private List<PathKindOption> getPathKindOptions(String filePath) {
					List<PathKindOption> result = new ArrayList<PathKindOption>();
					File file = new File(filePath);
					{
						File candidateResourceFile = new File(file.getAbsoluteFile().getPath());
						while (true) {
							File mostAncestorFile = candidateResourceFile.getParentFile();
							if (mostAncestorFile == null) {
								break;
							}
							while (mostAncestorFile.getParentFile() != null) {
								mostAncestorFile = mostAncestorFile.getParentFile();
							}
							candidateResourceFile = FileUtils.relativizeFile(mostAncestorFile,
									candidateResourceFile);
							String candidateResourcePath = candidateResourceFile.getPath().replaceAll("\\\\", "/");
							URL resourceURL = getClass().getClassLoader().getResource(candidateResourcePath);
							if (resourceURL != null) {
								result.add(new PathKindOption(
										SwingSpecificProperty.VALUE_PATH_TYPE_KIND_CLASSPATH_RESOURCE,
										candidateResourcePath));
								break;
							}
						}
					}
					{
						File currentDir = new File(".");
						if (FileUtils.isAncestor(currentDir, file)) {
							File relativeFile = FileUtils.relativizeFile(currentDir, file);
							result.add(new PathKindOption(SwingSpecificProperty.VALUE_PATH_TYPE_KIND_RELATIVE_FILE,
									relativeFile.getPath()));
						}
					}
					{
						result.add(new PathKindOption(SwingSpecificProperty.VALUE_PATH_TYPE_KIND_ABSOLUTE_FILE,
								file.getAbsolutePath()));
					}
					return result;
				}

				@Override
				public boolean isNullable() {
					return false;
				}

				@Override
				public boolean isGetOnly() {
					return false;
				}

				@Override
				public InfoCategory getCategory() {
					return null;
				}

				@Override
				public String toString() {
					return getCaption();
				}

				class PathKindOption {
					String path;
					String pathKind;

					public PathKindOption(String pathKind, String path) {
						super();
						this.path = path;
						this.pathKind = pathKind;
					}

					@Override
					public String toString() {
						if (SwingSpecificProperty.VALUE_PATH_TYPE_KIND_ABSOLUTE_FILE.equals(pathKind)) {
							return "<absolute-file> " + path;
						} else if (SwingSpecificProperty.VALUE_PATH_TYPE_KIND_RELATIVE_FILE.equals(pathKind)) {
							return "<relative-file> " + path;
						} else if (SwingSpecificProperty.VALUE_PATH_TYPE_KIND_CLASSPATH_RESOURCE.equals(pathKind)) {
							return getClassPathResourcePrefix() + path;
						} else {
							return "<illegal-path-kind: " + pathKind + "> " + path;
						}
					}
				}

			};
		}

		protected void openInfoCustomizationsWindow(InfoCustomizations infoCustomizations) {
			customizationToolsRenderer.openObjectFrame(infoCustomizations,
					customizationToolsRenderer.getReflectionUI().getObjectTitle(infoCustomizations),
					getCustomizationIcon().getImage());
		}

		protected JButton createSaveControl() {
			final File file = new File(infoCustomizationsOutputFilePath);
			final JButton result = new JButton(SwingRendererUtils.SAVE_ICON);
			result.setContentAreaFilled(false);
			result.setFocusable(false);
			result.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						infoCustomizations.saveToFile(file);
					} catch (IOException e1) {
						customizationToolsRenderer.handleExceptionsFromDisplayedUI(result, e1);
					}
				}
			});
			return result;
		}

		protected Component createTypeInfoCustomizer(final String typeName) {
			final JButton result = new JButton(
					customizationToolsRenderer.getReflectionUI().prepareStringToDisplay("Customizations..."),
					getCustomizationIcon());
			result.setContentAreaFilled(false);
			result.setFocusable(false);
			final TypeCustomization t = infoCustomizations.getTypeCustomization(typeName, true);
			result.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (customizationToolsRenderer.openObjectDialogAndGetConfirmation(result, t,
							customizationToolsRenderer.getReflectionUI().getObjectTitle(t),
							getCustomizationIcon().getImage(), true)) {
						SwingUtilities.invokeLater(new Runnable() {
							@Override
							public void run() {
								update(typeName);
							}
						});
					}
				}
			});
			return result;
		}

		protected Component createFieldInfoCustomizer(final ITypeInfo customizedType, final String fieldName) {
			final JButton result = new JButton(getCustomizationIcon());
			result.setPreferredSize(new Dimension(result.getPreferredSize().height, result.getPreferredSize().height));
			result.setContentAreaFilled(false);
			result.setFocusable(false);
			SwingRendererUtils.setMultilineToolTipText(result, customizationToolsRenderer.getReflectionUI()
					.prepareStringToDisplay("Customize this field display"));
			result.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					final JPopupMenu popupMenu = new JPopupMenu();
					popupMenu.add(new AbstractAction(reflectionUI.prepareStringToDisplay("Hide")) {
						private static final long serialVersionUID = 1L;

						@Override
						public void actionPerformed(ActionEvent e) {
							hideField(result, customizedType, fieldName);
						}
					});
					popupMenu.add(new AbstractAction(reflectionUI.prepareStringToDisplay("Move Up")) {
						private static final long serialVersionUID = 1L;

						@Override
						public void actionPerformed(ActionEvent e) {
							moveField(result, customizedType, fieldName, -1);
						}
					});
					popupMenu.add(new AbstractAction(reflectionUI.prepareStringToDisplay("Move Down")) {
						private static final long serialVersionUID = 1L;

						@Override
						public void actionPerformed(ActionEvent e) {
							moveField(result, customizedType, fieldName, 1);
						}
					});
					popupMenu.add(new AbstractAction(reflectionUI.prepareStringToDisplay("Move To Top")) {
						private static final long serialVersionUID = 1L;

						@Override
						public void actionPerformed(ActionEvent e) {
							moveField(result, customizedType, fieldName, Short.MIN_VALUE);
						}
					});
					popupMenu.add(new AbstractAction(reflectionUI.prepareStringToDisplay("Move To Bottom")) {
						private static final long serialVersionUID = 1L;

						@Override
						public void actionPerformed(ActionEvent e) {
							moveField(result, customizedType, fieldName, Short.MAX_VALUE);
						}
					});
					final IFieldInfo customizedField = ReflectionUIUtils.findInfoByName(customizedType.getFields(),
							fieldName);
					if (customizedField.getType() instanceof IListTypeInfo) {
						JMenu listSubMenu = new JMenu(reflectionUI.prepareStringToDisplay("List Structure"));
						{
							popupMenu.add(listSubMenu);
							listSubMenu.add(new AbstractAction(
									reflectionUI.prepareStringToDisplay("Move Columns...")) {
								private static final long serialVersionUID = 1L;

								@Override
								public void actionPerformed(ActionEvent e) {
									openListColumnsOrderDialog(result,
											(IListTypeInfo) customizedField.getType());
								}
							});
							listSubMenu.add(new AbstractAction(reflectionUI.prepareStringToDisplay("More Options...")) {
								private static final long serialVersionUID = 1L;

								@Override
								public void actionPerformed(ActionEvent e) {
									openListStructureCutomizationDialog(result,
											(IListTypeInfo) customizedField.getType());
								}
							});
						}
					}
					popupMenu.add(new AbstractAction(reflectionUI.prepareStringToDisplay("More Options...")) {
						private static final long serialVersionUID = 1L;

						@Override
						public void actionPerformed(ActionEvent e) {
							openFieldCutomizationDialog(result, customizedType, fieldName);
						}
					});
					popupMenu.show(result, result.getWidth(), result.getHeight());

				}
			});
			return result;
		}

		protected void hideMethod(Component activatorComponent, ITypeInfo customizedType, String methodSignature) {
			MethodCustomization mc = infoCustomizations.getMethodCustomization(customizedType.getName(),
					methodSignature, true);
			mc.setHidden(true);
			update(customizedType.getName());
		}

		protected void hideField(Component activatorComponent, ITypeInfo customizedType, String fieldName) {
			FieldCustomization fc = infoCustomizations.getFieldCustomization(customizedType.getName(), fieldName, true);
			fc.setHidden(true);
			update(customizedType.getName());
		}

		protected void moveField(Component activatorComponent, ITypeInfo customizedType, String fieldName, int offset) {
			TypeCustomization tc = infoCustomizations.getTypeCustomization(customizedType.getName(), true);
			try {
				tc.moveField(customizedType.getFields(), fieldName, offset);
			} catch (Throwable t) {
				handleExceptionsFromDisplayedUI(activatorComponent, t);
			}
			update(customizedType.getName());
		}

		protected void moveMethod(Component activatorComponent, ITypeInfo customizedType, String methodSignature,
				int offset) {
			TypeCustomization tc = infoCustomizations.getTypeCustomization(customizedType.getName(), true);
			try {
				tc.moveMethod(customizedType.getMethods(), methodSignature, offset);
			} catch (Throwable t) {
				handleExceptionsFromDisplayedUI(activatorComponent, t);
			}
			update(customizedType.getName());
		}

		protected void openListColumnsOrderDialog(Component activatorComponent,
				final IListTypeInfo customizedListType) {
			ITypeInfo customizedItemType = customizedListType.getItemType();
			String itemTypeName = (customizedItemType == null) ? null : customizedItemType.getName();
			ListStructureCustomization lc = infoCustomizations
					.getListStructureCustomization(customizedListType.getName(), itemTypeName, true);
			IListStructuralInfo customizedListStructure = customizedListType.getStructuralInfo();
			class ColumnOrderItem {
				IColumnInfo columnInfo;

				public ColumnOrderItem(IColumnInfo columnInfo, ColumnCustomization columnCustomization) {
					super();
					this.columnInfo = columnInfo;
				}

				public IColumnInfo getColumnInfo() {
					return columnInfo;
				}

				@Override
				public String toString() {
					return columnInfo.getCaption();
				}

			}
			List<ColumnOrderItem> columnOrder = new ArrayList<ColumnOrderItem>();
			for (final IColumnInfo c : customizedListStructure.getColumns()) {
				ColumnOrderItem orderItem = new ColumnOrderItem(c, lc.getColumnCustomization(c.getName()));
				columnOrder.add(orderItem);
			}
			if (customizationToolsRenderer.openObjectDialogAndGetConfirmation(activatorComponent, columnOrder,
					customizationToolsRenderer.getReflectionUI().prepareStringToDisplay("Columns Order"),
					getCustomizationIcon().getImage(), true)) {
				List<String> newOrder = new ArrayList<String>();
				for (ColumnOrderItem item : columnOrder) {
					newOrder.add(item.getColumnInfo().getName());
				}
				lc.setColumnsCustomOrder(newOrder);
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						update(customizedListType.getName());
					}
				});
			}
		}

		protected void openListStructureCutomizationDialog(Component activatorComponent,
				final IListTypeInfo customizedListType) {
			ITypeInfo customizedItemType = customizedListType.getItemType();
			String itemTypeName = (customizedItemType == null) ? null : customizedItemType.getName();
			ListStructureCustomization lc = infoCustomizations
					.getListStructureCustomization(customizedListType.getName(), itemTypeName, true);
			if (customizationToolsRenderer.openObjectDialogAndGetConfirmation(activatorComponent, lc,
					customizationToolsRenderer.getReflectionUI().getObjectTitle(lc), getCustomizationIcon().getImage(),
					true)) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						update(customizedListType.getName());
					}
				});
			}
		}

		protected void openFieldCutomizationDialog(Component activatorComponent, final ITypeInfo customoizedType,
				String fieldName) {
			FieldCustomization fc = infoCustomizations.getFieldCustomization(customoizedType.getName(), fieldName,
					true);
			if (customizationToolsRenderer.openObjectDialogAndGetConfirmation(activatorComponent, fc,
					customizationToolsRenderer.getReflectionUI().getObjectTitle(fc), getCustomizationIcon().getImage(),
					true)) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						update(customoizedType.getName());
					}
				});
			}
		}

		protected void openMethodCutomizationDialog(Component activatorComponent, final ITypeInfo customizedType,
				String methodSignature) {
			MethodCustomization mc = infoCustomizations.getMethodCustomization(customizedType.getName(),
					methodSignature, true);
			if (customizationToolsRenderer.openObjectDialogAndGetConfirmation(activatorComponent, mc,
					customizationToolsRenderer.getReflectionUI().getObjectTitle(mc), getCustomizationIcon().getImage(),
					true)) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						update(customizedType.getName());
					}
				});
			}
		}

		protected Component createMethodInfoCustomizer(final ITypeInfo customizedType, final String methodSignature) {
			final JButton result = new JButton(getCustomizationIcon());
			result.setPreferredSize(new Dimension(result.getPreferredSize().height, result.getPreferredSize().height));
			result.setContentAreaFilled(false);
			result.setFocusable(false);
			SwingRendererUtils.setMultilineToolTipText(result, customizationToolsRenderer.getReflectionUI()
					.prepareStringToDisplay("Customize this method display"));
			result.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					final JPopupMenu popupMenu = new JPopupMenu();
					popupMenu.add(new AbstractAction(reflectionUI.prepareStringToDisplay("Hide")) {
						private static final long serialVersionUID = 1L;

						@Override
						public void actionPerformed(ActionEvent e) {
							hideMethod(result, customizedType, methodSignature);
						}
					});
					popupMenu.add(new AbstractAction(reflectionUI.prepareStringToDisplay("Move Left")) {
						private static final long serialVersionUID = 1L;

						@Override
						public void actionPerformed(ActionEvent e) {
							moveMethod(result, customizedType, methodSignature, -1);
						}
					});
					popupMenu.add(new AbstractAction(reflectionUI.prepareStringToDisplay("Move Right")) {
						private static final long serialVersionUID = 1L;

						@Override
						public void actionPerformed(ActionEvent e) {
							moveMethod(result, customizedType, methodSignature, 1);
						}
					});
					popupMenu.add(new AbstractAction(reflectionUI.prepareStringToDisplay("More Options...")) {
						private static final long serialVersionUID = 1L;

						@Override
						public void actionPerformed(ActionEvent e) {
							openMethodCutomizationDialog(result, customizedType, methodSignature);
						}
					});
					popupMenu.show(result, result.getWidth(), result.getHeight());
				}
			});
			return result;
		}

		protected void update(String typeName) {
			for (Map.Entry<JPanel, Object> entry : getObjectByForm().entrySet()) {
				Object object = entry.getValue();
				ITypeInfo objectType = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(object));
				if (typeName.equals(objectType.getName())) {
					for (JPanel form : getForms(object)) {
						recreateFormContent(form);
					}
				}
				JPanel form = entry.getKey();
				for (FieldControlPlaceHolder placeHolder : getAllFieldControlPlaceHolders(form)) {
					IFieldInfo field = placeHolder.getField();
					if (typeName.equals(field.getType().getName())) {
						refreshFieldControlsByName(form, field.getName(), true);
					}
				}
			}
			TypeCustomization t = infoCustomizations.getTypeCustomization(typeName);
			for (JPanel form : customizationToolsRenderer.getForms(t)) {
				customizationToolsRenderer.refreshAllFieldControls(form, false);
			}
			for (JPanel form : customizationToolsRenderer.getForms(infoCustomizations)) {
				customizationToolsRenderer.refreshAllFieldControls(form, false);
			}
		}

	}

}