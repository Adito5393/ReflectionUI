package xy.reflect.ui.control.swing.plugin;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import xy.reflect.ui.control.FieldControlDataProxy;
import xy.reflect.ui.control.FieldControlInputProxy;
import xy.reflect.ui.control.IFieldControlData;
import xy.reflect.ui.control.IFieldControlInput;
import xy.reflect.ui.control.plugin.AbstractSimpleCustomizableFieldControlPlugin;
import xy.reflect.ui.control.swing.DialogAccessControl;
import xy.reflect.ui.control.swing.DialogBuilder;
import xy.reflect.ui.control.swing.IAdvancedFieldControl;
import xy.reflect.ui.control.swing.TextControl;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.info.method.AbstractConstructorInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.method.InvocationData;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.DefaultTypeInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.factory.InfoProxyFactory;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.util.ClassUtils;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;

public class FileBrowserPlugin extends AbstractSimpleCustomizableFieldControlPlugin {

	protected static File lastDirectory = new File(".").getAbsoluteFile();

	@Override
	public String getControlTitle() {
		return "File Browser";
	}

	@Override
	protected boolean handles(Class<?> javaType) {
		return File.class.isAssignableFrom(javaType);
	}

	@Override
	public boolean canDisplayDistinctNullValue() {
		return false;
	}

	@Override
	public AbstractConfiguration getDefaultControlCustomization() {
		return new FileBrowserConfiguration();
	}

	@Override
	public FileBrowser createControl(Object renderer, IFieldControlInput input) {
		return new FileBrowser((SwingRenderer) renderer, input);
	}

	@Override
	public IFieldControlData filterDistinctNullValueControlData(IFieldControlData controlData) {
		return new FieldControlDataProxy(controlData) {

			@Override
			public ITypeInfo getType() {
				return new FileTypeInfoProxyFactory().wrapTypeInfo(super.getType());
			}

		};
	}

	protected static class FileTypeInfoProxyFactory extends InfoProxyFactory {

		@Override
		protected List<IMethodInfo> getConstructors(ITypeInfo type) {
			if (FileConstructor.isCompatibleWith(type)) {
				List<IMethodInfo> result = new ArrayList<IMethodInfo>();
				result.add(new FileConstructor(type));
				return result;
			}
			return super.getConstructors(type);
		}

		@Override
		protected boolean isConcrete(ITypeInfo type) {
			if (FileConstructor.isCompatibleWith(type)) {
				return true;
			}
			return super.isConcrete(type);
		}

	}

	protected static class FileConstructor extends AbstractConstructorInfo {

		private ITypeInfo type;

		public FileConstructor(ITypeInfo type) {
			this.type = type;
		}

		@Override
		public ITypeInfo getReturnValueType() {
			return type;
		}

		@Override
		public List<IParameterInfo> getParameters() {
			return Collections.emptyList();
		}

		@Override
		public Object invoke(Object parentObject, InvocationData invocationData) {
			return new File("");
		}

		public static boolean isCompatibleWith(ITypeInfo type) {
			Class<?> fileClass;
			try {
				fileClass = ClassUtils.getCachedClassforName(type.getName());
			} catch (ClassNotFoundException e) {
				return false;
			}
			return File.class.isAssignableFrom(fileClass);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + ((type == null) ? 0 : type.hashCode());
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
			FileConstructor other = (FileConstructor) obj;
			if (type == null) {
				if (other.type != null)
					return false;
			} else if (!type.equals(other.type))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "FileConstructor [type=" + type + "]";
		}

	}

	public static class FileBrowserConfiguration extends AbstractConfiguration {
		private static final long serialVersionUID = 1L;

		public List<FileNameFilterConfiguration> fileNameFilters = new ArrayList<FileNameFilterConfiguration>();
		public String actionTitle = "Select";
		public SelectionModeConfiguration selectionMode = SelectionModeConfiguration.FILES_AND_DIRECTORIES;

		@Override
		public String toString() {
			return "FilecontrolConfiguration [fileNameFilters=" + fileNameFilters + ", actionTitle=" + actionTitle
					+ ", selectionMode=" + selectionMode + "]";
		}

	}

	public static class FileNameFilterConfiguration extends AbstractConfiguration {
		private static final long serialVersionUID = 1L;

		public String description = "";
		public List<String> extensions = new ArrayList<String>();

		public void validate() {
			if (description.length() == 0) {
				throw new ReflectionUIError("Description is mandatory");
			}
			if (extensions.size() == 0) {
				throw new ReflectionUIError("At least 1 extension is mandatory");
			}
		}

		@Override
		public String toString() {
			return "FileNameFilter [description=" + description + ", extensions=" + extensions + "]";
		}
	}

	public static enum SelectionModeConfiguration {
		FILES_AND_DIRECTORIES, FILES_ONLY, DIRECTORIES_ONLY

	}

	public class FileBrowser extends DialogAccessControl implements IAdvancedFieldControl {

		protected static final long serialVersionUID = 1L;

		protected boolean textChangedByUser = true;

		public FileBrowser(SwingRenderer swingRenderer, IFieldControlInput input) {
			super(swingRenderer, input);
		}

		@Override
		protected Component createStatusControl(IFieldControlInput input) {
			return new TextControl(swingRenderer, new FieldControlInputProxy(input) {

				@Override
				public IFieldControlData getControlData() {
					return new FieldControlDataProxy(super.getControlData()) {

						@Override
						public void setValue(Object value) {
							if (value != null) {
								value = new File((String) value);
							}
							base.setValue(value);
						}

						@Override
						public Object getValue() {
							File currentFile = (File) base.getValue();
							if (currentFile == null) {
								return null;
							}
							return currentFile.getPath();
						}

						@Override
						public ITypeInfo getType() {
							return new DefaultTypeInfo(swingRenderer.getReflectionUI(),
									new JavaTypeInfoSource(String.class, null));
						}
					};
				}

			});
		}

		@Override
		protected Component createChangeControl() {
			Component result = super.createChangeControl();
			if (data.isGetOnly()) {
				result.setEnabled(false);
			}
			return result;
		}

		protected void configureFileChooser(JFileChooser fileChooser, File currentFile) {
			FileBrowserConfiguration controlConfiguration = (FileBrowserConfiguration) loadControlCustomization(input);
			if (currentFile != null) {
				fileChooser.setSelectedFile(currentFile.getAbsoluteFile());
			}
			if (controlConfiguration.selectionMode == SelectionModeConfiguration.FILES_AND_DIRECTORIES) {
				fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
			} else if (controlConfiguration.selectionMode == SelectionModeConfiguration.FILES_ONLY) {
				fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			} else if (controlConfiguration.selectionMode == SelectionModeConfiguration.DIRECTORIES_ONLY) {
				fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			} else {
				throw new ReflectionUIError();
			}
			int i = 0;
			for (FileNameFilterConfiguration filter : controlConfiguration.fileNameFilters) {
				String swingFilterDescription = filter.description + "(*."
						+ ReflectionUIUtils.stringJoin(filter.extensions, ", *.") + ")";
				String[] swingFilterExtensions = filter.extensions.toArray(new String[filter.extensions.size()]);
				FileNameExtensionFilter newFileFilter = new FileNameExtensionFilter(swingFilterDescription,
						swingFilterExtensions);
				fileChooser.addChoosableFileFilter(newFileFilter);
				if (i == 0) {
					fileChooser.setFileFilter(newFileFilter);
				}
				i++;
			}
		}

		protected String getDialogTitle() {
			FileBrowserConfiguration controlConfiguration = (FileBrowserConfiguration) loadControlCustomization(input);
			return controlConfiguration.actionTitle;
		}

		@Override
		public void openDialog(Component owner) {
			final JFileChooser fileChooser = new JFileChooser();
			File currentFile = (File) data.getValue();
			fileChooser.setCurrentDirectory(lastDirectory);
			configureFileChooser(fileChooser, currentFile);

			final DialogBuilder dialogBuilder = swingRenderer.getDialogBuilder(owner);
			dialogBuilder.setTitle(getDialogTitle());
			fileChooser.setApproveButtonText(swingRenderer.prepareStringToDisplay("OK"));
			fileChooser.rescanCurrentDirectory();
			final boolean[] ok = new boolean[] { false };
			fileChooser.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (e.getActionCommand().equals(javax.swing.JFileChooser.APPROVE_SELECTION)) {
						ok[0] = true;
						dialogBuilder.getCreatedDialog().dispose();
					} else if (e.getActionCommand().equals(javax.swing.JFileChooser.CANCEL_SELECTION)) {
						dialogBuilder.getCreatedDialog().dispose();
					}
				}
			});
			dialogBuilder.setContentComponent(fileChooser);
			swingRenderer.showDialog(dialogBuilder.createDialog(), true);
			if (!ok[0]) {
				return;
			}

			lastDirectory = fileChooser.getCurrentDirectory();
			File file = fileChooser.getSelectedFile();
			if (fileChooser.getFileFilter() != null) {
				if (fileChooser.getFileFilter() instanceof FileNameExtensionFilter) {
					FileNameExtensionFilter extensionFilter = (FileNameExtensionFilter) fileChooser.getFileFilter();
					if (extensionFilter.getExtensions().length == 1) {
						String extension = extensionFilter.getExtensions()[0];
						if (!file.getName().toLowerCase().endsWith("." + extension.toLowerCase())) {
							file = new File(file.getPath() + "." + extension);
						}
					}
				}
			}
			data.setValue(file);
			refreshUI(true);
		}

		@Override
		public boolean displayError(String msg) {
			return false;
		}

		@Override
		public boolean showsCaption() {
			return false;
		}

		@Override
		public boolean handlesModificationStackAndStress() {
			return false;
		}

		@Override
		public String toString() {
			return "FileBrowser [data=" + data + "]";
		}

	}

}