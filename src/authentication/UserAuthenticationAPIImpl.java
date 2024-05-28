//$Id$
package authentication;

import java.sql.SQLException;
import java.util.Objects;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cache.Cache;
import dbmanager.MusicPlayerDBAPIImpl;
import queue.SongQueue;
import ui.MusicPlayerMainMenu;
import user_preference.UserPreferenceAPIImpl;

public class UserAuthenticationAPIImpl implements UserAuthenticationAPI{
	
	public static UserAuthenticationAPIImpl getInstance() {
		return UserAuthenticationManagerInstance.INSTANCE;
	}
	
	private static class UserAuthenticationManagerInstance {
		private static final UserAuthenticationAPIImpl INSTANCE = new UserAuthenticationAPIImpl();
	}
	
	@Override
	public void manageSignIn() {
		boolean isValidUser = false;
		Scanner inputeReader = new Scanner(System.in);
		System.out.println("Enter user name/email: ");
		String userName = inputeReader.nextLine();
		System.out.println("Enter password: ");
		String password = inputeReader.nextLine();
		try {
			if(checkEmail(userName)) {
				String emailId = userName;
				userName = MusicPlayerDBAPIImpl.getInstance().manageSignInUsingEmail(emailId, password);
				isValidUser = Objects.nonNull(userName);
			} else {
				isValidUser = MusicPlayerDBAPIImpl.getInstance().manageSignInUsingUserName(userName, password);				
			}
		} catch (Exception e) {
			System.out.println("Something went wrong while signing in... Please try again...");
		}
		
		if(isValidUser) {
			Cache.getInstance().cacheData("userName", userName);
			Cache.getInstance().cacheData("theme", MusicPlayerDBAPIImpl.getInstance().getUserTheme());
			UserPreferenceAPIImpl.getInstance().caCheSettings(false);
			MusicPlayerMainMenu.getInstance().mainMenu();
		} else {
			System.out.println("UserId or password is wrong... try again");
		}
	}
	
	@Override
	public void manageSignUp() {
		
		Scanner inputeReader = new Scanner(System.in);
		System.out.println("Enter user name: ");
		String userName = inputeReader.nextLine();
		System.out.println("Enter emailID: ");
		String emailId = inputeReader.next();
		if(!checkEmail(emailId)) {
			System.out.println("Invalid Email... Try again");
			return;
		}
		System.out.println("Enter password: ");
		String password = inputeReader.next();
		try {
			MusicPlayerDBAPIImpl.getInstance().manageSignUp(userName, emailId, password);
			Cache.getInstance().cacheData("userName", userName);
			Cache.getInstance().cacheData("theme", MusicPlayerDBAPIImpl.getInstance().getUserTheme());
			UserPreferenceAPIImpl.getInstance().caCheSettings(true);
			MusicPlayerMainMenu.getInstance().mainMenu();
		} catch (SQLException e) {
			if (e.getErrorCode() == 1062) {
				printErrorMessageforDuplicateUser(e.getMessage());
	        }
		} catch (Exception e) {
			System.out.println("Something went wrong while signing up... Please try again...");
		}
	}
	
	private void printErrorMessageforDuplicateUser(String errorMsg) {
        Pattern pattern = Pattern.compile("Duplicate entry '([^']+)' for key .+");
        Matcher matcher = pattern.matcher(errorMsg);
        if(matcher.find()) {
        	String duplicateData = matcher.group(1);
        	if(checkEmail(duplicateData)) {
        		System.out.println("Email ID " + duplicateData + " already exists...");
        	} else {
        		System.out.println("User " + duplicateData + " already exists...");
        	}
        }
	}
	
	@Override
	public void manageSignOut() {
		Cache.getInstance().clearCache();
		// return to sign up page
	}
	
	@Override
	public void exitApplication() {
		manageSignOut();
		MusicPlayerDBAPIImpl.getInstance().closeResources();
		System.exit(0);
	}
}
