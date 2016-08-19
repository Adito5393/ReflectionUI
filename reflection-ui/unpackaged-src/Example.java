import java.awt.Window;
import java.io.File;
import java.io.IOException;
import java.util.Date;

import javax.swing.JOptionPane;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.control.swing.CustomizingSwingRenderer;
import xy.reflect.ui.control.swing.SwingRenderer;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.source.ITypeInfoSource;
import xy.reflect.ui.info.type.util.InfoCustomizations;
import xy.reflect.ui.info.type.util.InfoProxyGenerator;

/*
 * Read carefully the comments below. 
 */
public class Example {

	public static void main(String[] args) {

		/* The Hello world: */
		Object myObject = new Date();
		ReflectionUI reflectionUI = new ReflectionUI();
		SwingRenderer swingRenderer = new SwingRenderer(reflectionUI);
		swingRenderer.openObjectFrame(myObject);

		/* You can open a dialog instead of a frame: */
		swingRenderer.openObjectDialog(null, myObject, true);

		/* You can just create a form and then insert it in any container: */
		JOptionPane.showMessageDialog(null, swingRenderer.createObjectForm(myObject));

		/*
		 * You can customize some aspects (field labels, hide some methods, ...)
		 * of the generated UI by using an integrated customizations editor. To
		 * enable this editor create the reflectionUI and the renderer objects
		 * with the following code:
		 */
		final InfoCustomizations customizations = new InfoCustomizations();
		String customizationsFilePath = "path/to/a/customizations/file.xml";
		try {
			customizations.loadFromFile(new File(customizationsFilePath));
		} catch (IOException e) {
			throw new AssertionError(e);
		}
		reflectionUI = new ReflectionUI() {
			@Override
			public ITypeInfo getTypeInfo(ITypeInfoSource typeSource) {
				return customizations.get(this, super.getTypeInfo(typeSource));
			}
		};
		swingRenderer = new CustomizingSwingRenderer(reflectionUI, customizations, customizationsFilePath);

		/*
		 * The ReflectionUI generator assumes that the Java coding standards are
		 * respected for the instanciated classes. Otherwise adjustments can be
		 * done by overriding some methods of the ReflectionUI object:
		 */
		reflectionUI = new ReflectionUI() {

			/* if your class "equals" method is not well implemented then: */
			@Override
			public boolean equals(Object value1, Object value2) {
				if (value1 instanceof Example) {
					// TODO: replace with your code
					return false;
				} else {
					return super.equals(value1, value2);
				}
			}

			/* if your class "toString" method is not well implemented then: */
			@Override
			public String toString(Object object) {
				if (object instanceof Example) {
					// TODO: replace with your code
					return "";
				} else {
					return super.toString(object);
				}
			}

		};

		/*
		 * How to enable copy/cut/paste: By default this functionality is
		 * enabled only for Serializable objects. If your class does not
		 * implement the Serializable interface then override the following
		 * methods of the ReflectionUI object:
		 */
		reflectionUI = new ReflectionUI() {

			@Override
			public boolean canCopy(Object object) {
				// TODO: replace with your code
				return super.canCopy(object);
			}

			@Override
			public Object copy(Object object) {
				// TODO: replace with your code
				return super.copy(object);
			}

		};

		/*
		 * In order to customize the UI behavior, just override the renderer
		 * object. For instance let set any swing window size to 300x300:
		 */
		swingRenderer = new SwingRenderer(reflectionUI) {
			@Override
			public void adjustWindowBounds(Window window) {
				super.adjustWindowBounds(window);
				window.setSize(300, 300);
			}
		};
		swingRenderer.openObjectDialog(null, myObject, "300x300 window", null, true);

		/*
		 * If you specifically want to take control over the object discovery
		 * and interpretation process, then you must override the getTypeInfo()
		 * method. For instance we can uppercase all the field captions
		 */
		reflectionUI = new ReflectionUI() {
			@Override
			public ITypeInfo getTypeInfo(ITypeInfoSource typeSource) {
				return new InfoProxyGenerator() {
					@Override
					protected String getCaption(IFieldInfo field, ITypeInfo containingType) {
						return super.getCaption(field, containingType).toUpperCase();
					}
				}.get(super.getTypeInfo(typeSource));
			}

		};
		swingRenderer.openObjectDialog(null, myObject, "uppercase field captions", null, true);

	}
}
