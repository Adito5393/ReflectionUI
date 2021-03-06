package xy.reflect.ui.example;

import java.io.IOException;

import xy.reflect.ui.CustomizedUI;
import xy.reflect.ui.control.swing.customizer.SwingCustomizer;
import xy.reflect.ui.util.MoreSystemProperties;

/**
 * Message sending screen GUI generated with the XML declarative customizations.
 * 
 * @author olitank
 *
 */
public class SaySomething2 {

	public static void main(String[] args) throws IOException {
		System.out.println("Set the following system property to disable the design mode:\n-D"
				+ MoreSystemProperties.HIDE_INFO_CUSTOMIZATIONS_TOOLS + "=true");

		CustomizedUI reflectionUI = new CustomizedUI();
		SwingCustomizer renderer = new SwingCustomizer(reflectionUI, "saySomething2.icu");
		renderer.openObjectFrame(new SaySomething2());
	}

	private String title;
	private String emailAddress;
	private String message;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public void send() {
		throw new UnsupportedOperationException();
	}

}
