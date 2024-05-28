//$Id$
package dbmanager;

import java.sql.SQLException;

public interface UserPreferenceDBAPI {
	public int getCurrentUserSettings() throws SQLException;
	
	public boolean updateSettings(int valueToUpdate) throws SQLException;
	
	public String getUserTheme();
	
	public boolean setUserTheme(String theme);
	
	public boolean changeUserName(String newUserName) throws SQLException;
}
