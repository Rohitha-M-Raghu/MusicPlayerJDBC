//$Id$
package authentication;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface UserAuthenticationAPI {
	public void manageSignIn();
	public void manageSignUp();
	public void manageSignOut();
	public void exitApplication();
	
	// can provide implementation for static or default method
	// cannot be overridden by classes implementing the interface - can in Java higher versions
	
	default boolean checkEmail(String emailId) {
		String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
	    Pattern pattern = Pattern.compile(emailRegex);
	    Matcher matcher = pattern.matcher(emailId);
	    return matcher.matches();
	}
}
