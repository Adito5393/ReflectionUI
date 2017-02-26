package xy.reflect.ui.control.swing;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.Border;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;

import xy.reflect.ui.control.data.IControlData;
import xy.reflect.ui.control.swing.SwingRenderer.FieldControlPlaceHolder;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.reflect.ui.util.SwingRendererUtils;

public class TextControl extends JPanel implements IAdvancedFieldControl {

	protected static final long serialVersionUID = 1L;
	protected SwingRenderer swingRenderer;
	protected IControlData data;

	protected JTextArea textComponent;
	protected boolean ignoreEditEvents = true;
	protected Border textFieldNormalBorder;
	protected JLabel iconControl;

	public TextControl(final SwingRenderer swingRenderer, FieldControlPlaceHolder placeHolder) {
		this.swingRenderer = swingRenderer;
		this.data = retrieveData(placeHolder);

		setLayout(new BorderLayout());

		textComponent = createTextComponent();
		{
			updateTextComponent();
			JScrollPane scrollPane = new JScrollPane(textComponent) {

				protected static final long serialVersionUID = 1L;

				@Override
				public Dimension getPreferredSize() {
					Dimension result = super.getPreferredSize();
					result = fixScrollPaneSizeWHenVerticalBarVisible(result);
					result.width = Math.min(result.width, Toolkit.getDefaultToolkit().getScreenSize().width / 3);
					result.height = Math.min(result.height, Toolkit.getDefaultToolkit().getScreenSize().height / 3);
					return result;
				}

				private Dimension fixScrollPaneSizeWHenVerticalBarVisible(Dimension size) {
					if (getHorizontalScrollBar().isVisible()) {
						size.height += getHorizontalScrollBar().getPreferredSize().height;
					}
					return size;
				}
			};
			textFieldNormalBorder = textComponent.getBorder();
			if (data.isGetOnly()) {
				textComponent.setEditable(false);
				textComponent.setBackground(ReflectionUIUtils.getDisabledTextBackgroundColor());
				scrollPane.setBorder(BorderFactory.createTitledBorder(""));
			} else {
				textComponent.getDocument().addUndoableEditListener(new UndoableEditListener() {

					@Override
					public void undoableEditHappened(UndoableEditEvent e) {
						if (ignoreEditEvents) {
							return;
						}
						try {
							onTextChange(textComponent.getText());
						} catch (Throwable t) {
							swingRenderer.handleExceptionsFromDisplayedUI(TextControl.this, t);
						}
					}
				});
			}
			add(scrollPane, BorderLayout.CENTER);
		}
		iconControl = createIconTrol();
		{
			updateIcon();
			add(SwingRendererUtils.flowInLayout(iconControl, FlowLayout.CENTER), BorderLayout.EAST);
		}
	}

	protected IControlData retrieveData(FieldControlPlaceHolder placeHolder) {
		return placeHolder.getControlData();
	}

	
	protected JTextArea createTextComponent() {
		return new JTextArea() {

			private static final long serialVersionUID = 1L;

			@Override
			public void replaceSelection(String content) {
				boolean wasIgnoringEditEvents = ignoreEditEvents;
				ignoreEditEvents = true;
				super.replaceSelection(content);
				ignoreEditEvents = wasIgnoringEditEvents;
				onTextChange(textComponent.getText());
			}

		};
	}

	protected void onTextChange(String newStringValue) {
		try {
			data.setValue(newStringValue);
		} catch (Throwable t) {
			displayError(ReflectionUIUtils.getPrettyErrorMessage(t));
		}
	}

	protected void updateTextComponent() {
		ignoreEditEvents = true;
		String newText = (String) data.getValue();
		if (!ReflectionUIUtils.equalsOrBothNull(textComponent.getText(), newText)) {
			int lastCaretPosition = textComponent.getCaretPosition();
			textComponent.setText(newText);
			SwingRendererUtils.handleComponentSizeChange(this);
			textComponent.setCaretPosition(Math.min(lastCaretPosition, textComponent.getText().length()));
		}
		ignoreEditEvents = false;
	}

	protected JLabel createIconTrol() {
		return new JLabel();
	}

	protected void updateIcon() {
		iconControl.setIcon(SwingRendererUtils.getControlDataIcon(swingRenderer, data));
		iconControl.setVisible(iconControl.getIcon() != null);
	}

	public static String toText(Object object) {
		return object.toString();
	}

	@Override
	public Dimension getMinimumSize() {
		return super.getPreferredSize();
	}

	@Override
	public boolean displayError(String msg) {
		boolean changed = !ReflectionUIUtils.equalsOrBothNull(msg, textComponent.getToolTipText());
		if (!changed) {
			return true;
		}
		if (msg != null) {
			swingRenderer.getReflectionUI().logError(msg);
		}
		if (msg == null) {
			setBorder(textFieldNormalBorder);
			textComponent.setToolTipText("");
			SwingRendererUtils.showTooltipNow(textComponent);
		} else {
			SwingRendererUtils.setErrorBorder(this);
			SwingRendererUtils.setMultilineToolTipText(textComponent,
					swingRenderer.prepareStringToDisplay(msg));
			SwingRendererUtils.showTooltipNow(textComponent);
		}
		return true;
	}

	@Override
	public boolean refreshUI() {
		updateTextComponent();
		updateIcon();
		displayError(null);
		SwingRendererUtils.handleComponentSizeChange(this);
		return true;
	}

	@Override
	public boolean showCaption() {
		return false;
	}

	@Override
	public boolean handlesModificationStackUpdate() {
		return false;
	}

	@Override
	public Object getFocusDetails() {
		int caretPosition = textComponent.getCaretPosition();
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("caretPosition", caretPosition);
		return result;
	}

	@Override
	public void requestDetailedFocus(Object value) {
		@SuppressWarnings("unchecked")
		Map<String, Object> focusDetails = (Map<String, Object>) value;
		int caretPosition = (Integer) focusDetails.get("caretPosition");
		textComponent.requestFocus();
		textComponent.setCaretPosition(Math.min(caretPosition, textComponent.getText().length()));
	}

	@Override
	public void requestFocus() {
		textComponent.requestFocus();
	}

	@Override
	public void validateSubForm() throws Exception {
	}
}
