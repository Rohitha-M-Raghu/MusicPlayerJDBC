//$Id$
package ui;

import java.io.IOException;
import java.util.Scanner;

import authentication.UserAuthenticationAPIImpl;
import queue.SongQueue;
import songdata.SongData;

public class MusicPlayerUserAuthentication {
	public static void main(String[] args) {
		int choice;	
		
		// write songs
		boolean isAddSongsFromFileNeeded = false;
		if(isAddSongsFromFileNeeded) {
			try {
				SongData.getInstance().populateSongDetailsFromFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		SongQueue.getInstance().start();
		do {
			choice = displaySignInPage();
			switch(choice) {
				case -1:
					System.out.println("Invalid Input... Try Again");
					break;
				case 1: 
					UserAuthenticationAPIImpl.getInstance().manageSignIn();
					break;
				case 2: 
					UserAuthenticationAPIImpl.getInstance().manageSignUp();
					break;
				case 3:
					UserAuthenticationAPIImpl.getInstance().exitApplication();
					System.out.println("Exiting application...");
					break;
				default:
					System.out.println("Something went wrong... Try Again");	
					break;
			}

		}while(choice != 3);
	}
	
	private static int displaySignInPage() {
		Scanner inputeReader = new Scanner(System.in);
		System.out.println("Welcome to Music Player");
		System.out.println("--------------------------");
		System.out.println("1. Sign in");
		System.out.println("2. Sign up");
		System.out.println("3. Exit");
		System.out.println("Enter your choice: ");
		try {
			return inputeReader.nextInt();
		} catch(Exception e) {
			return -1;
		}
	}
}

