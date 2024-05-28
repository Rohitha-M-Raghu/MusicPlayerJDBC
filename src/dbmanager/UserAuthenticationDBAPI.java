//$Id$
package dbmanager;

import java.sql.SQLException;

public interface UserAuthenticationDBAPI {
	public boolean manageSignInUsingUserName(String userName, String password) throws SQLException;
	public String manageSignInUsingEmail(String emailId, String password) throws SQLException;
	public void manageSignUp(String userName, String emailId, String password) throws SQLException;
	public boolean passwordValidation() throws SQLException;
	public boolean changePassword(String newPassword) throws SQLException;
}
