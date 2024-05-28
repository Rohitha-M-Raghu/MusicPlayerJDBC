//$Id$
package user_preference;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import cache.Cache;
import dbmanager.MusicPlayerDBAPIImpl;
import exception.PlaylistOrSongNotFoundException;
import queue.SongQueue;
import queue.SongQueueAPIImpl;
import song.SettingsMode;
import song.Song;
import ui.DisplayColor;
import ui.MusicPlayerMainMenu;

public class UserPreferenceAPIImpl implements UserPreferenceAPI{
	
	private String errorMsg = "Something went wrong... Please try again...";
	public static UserPreferenceAPIImpl getInstance() {
		return UserPreferenceAPIImplInstance.INSTANCE;
	}
	
	private static class UserPreferenceAPIImplInstance {
		private static final UserPreferenceAPIImpl INSTANCE = new UserPreferenceAPIImpl();
	}
	
	@Override
	public void likeCurrentPlayingSong() {
		try {
			Song currentPlayingSong = MusicPlayerDBAPIImpl.getInstance().getSongDetails(SongQueue.getInstance().getCurrentPlayingSongId());
			
			String songTitle = currentPlayingSong.getSongTitle();
			String artistName = currentPlayingSong.getArtistName();
			likeASong(songTitle, artistName);
		} catch (Exception e) {
			System.out.println(errorMsg);
		}
	}
	
	@Override
	public void likeASong() {
		Scanner inputeReader = new Scanner(System.in);
		System.out.println("Enter SongTitle: ");
		String songTitle = inputeReader.nextLine();
		System.out.println("Enter ArtistName: ");
		String artistName = inputeReader.nextLine();
		// get songId
		likeASong(songTitle, artistName);
	}
	
	@Override
	public void likeASong(String songTitle, String artistName) {
		boolean isSuccess = false;

		try {
			Song song  = MusicPlayerDBAPIImpl.getInstance().getSongDetails(songTitle, artistName);
			if(song == null) {
				throw new PlaylistOrSongNotFoundException("Song not found...");
			}
			int songId = song.getSongId();
			if(MusicPlayerDBAPIImpl.getInstance().getLikedSongs().contains(songId)) {
				System.out.println(songTitle + " is already liked...");
				return;
			}
			isSuccess = MusicPlayerDBAPIImpl.getInstance().likeASong(songId);

			
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (PlaylistOrSongNotFoundException e) {
			System.out.println(e.getMessage());
			return;
		}
		
		if(isSuccess) {
			System.out.println(songTitle + " liked...");
		} else {
			System.out.println("Song not found.");
		}
	}
	
	@Override
	public void unlikeASong() {
		// display liked songs
		boolean isSuccess = false;
		List<Integer> likedSongIds = new ArrayList<>();
		try {
			likedSongIds = MusicPlayerDBAPIImpl.getInstance().displayLikedSongs();
		} catch (SQLException e) {
			System.out.println("Issue while fetching liked songs...");
			return;
		}
		if (likedSongIds.isEmpty()) {
			return;
		}
		// get song to unlike from user
		Scanner inputeReader = new Scanner(System.in);
		System.out.println("Enter SongTitle: ");
		String songToUnlike = inputeReader.nextLine();
		System.out.println("Enter artistName: ");
		String artistName = inputeReader.nextLine();
		// check if the song is in liked list
		try {
			Song song = MusicPlayerDBAPIImpl.getInstance().getSongDetails(songToUnlike, artistName);
			if(song == null) {
				throw new PlaylistOrSongNotFoundException("Song not found...");
			}
			int songId = song.getSongId();
			if(likedSongIds.contains(songId)) {
				// unlike song
				isSuccess = MusicPlayerDBAPIImpl.getInstance().unlikeASong(songId);
			} else {
				System.out.println(songToUnlike + " is not a liked song...");
				return;
			}
		} catch (SQLException e) {
			System.out.println("Something went wrong while unliking song...");
		} catch (PlaylistOrSongNotFoundException e) {
			System.out.println(e.getMessage());
			return;
		}
		if(isSuccess) {
			System.out.println(songToUnlike + " is removed from liked songs..");
		} else {
			System.out.println("Failed to remove " + songToUnlike + " from liked songs...");
		}
	}
	
	@Override
	public void unlikePlaylist() {
		// display liked playlists
		boolean isSuccess = false;
		List<Integer> likedPlaylists = new ArrayList<>();
		try {
			likedPlaylists = MusicPlayerDBAPIImpl.getInstance().displayLikedPlayLists();
		} catch (SQLException e) {
			System.out.println("Issue while fetching liked playlists...");
			return;
		}
		if (likedPlaylists.isEmpty()) {
			return;
		}
		
		// get playlist to unlike from user
		Scanner inputeReader = new Scanner(System.in);
		System.out.println("Enter playlist: ");
		String playlistToUnlike = inputeReader.nextLine();
		// get playlist to unlike from user
		try {
			int playlistId = MusicPlayerDBAPIImpl.getInstance().getPlaylistId(playlistToUnlike);
			if(playlistId == -1) {
				throw new PlaylistOrSongNotFoundException("Playlist not found...");
			}
			if(likedPlaylists.contains(playlistId)) {
				// unlike song
				isSuccess = MusicPlayerDBAPIImpl.getInstance().unlikeAPlaylist(playlistId);
			} else {
				System.out.println(playlistToUnlike + " is not a liked playlist...");
				return;
			}
		} catch (SQLException e) {
			System.out.println("Something went wrong while unliking playlist...");
		} catch (PlaylistOrSongNotFoundException e) {
			System.out.println(e.getMessage());
			return;
		}
		if(isSuccess) {
			System.out.println(playlistToUnlike + " is removed from liked playlists..");
		} else {
			System.out.println("Failed to remove " + playlistToUnlike + " from liked playlists...");
		}
	}
	
	@Override
	public void likeAPlayList() {
		Scanner inputeReader = new Scanner(System.in);
		boolean isSuccess = false;
		System.out.println("Enter PlayList Name: ");
		String playListName = inputeReader.nextLine();
		try {
			int playlistId = MusicPlayerDBAPIImpl.getInstance().getPlaylistId(playListName);
			if(playlistId == -1) {
				throw new PlaylistOrSongNotFoundException("Playlist Not Found...");
			}
			if(MusicPlayerDBAPIImpl.getInstance().getLikedPlaylistSongs().contains(playlistId)) {
				System.out.println(playListName + " is already liked...");
				return;
			} 
			isSuccess = MusicPlayerDBAPIImpl.getInstance().likeAPlayList(playListName);

		} catch (SQLException e) {
			System.out.println(errorMsg);
			e.printStackTrace();
		} catch (PlaylistOrSongNotFoundException e) {
			System.out.println(e.getMessage());
			return;
		}
		
		if(isSuccess) {
			System.out.println(playListName + " liked...");
		} else {
			System.out.println("Failed to like Playlist " + playListName +"...");
		}
	}
	
	@Override
	public void changeSettings(SettingsMode setting) {
		Scanner inputeReader = new Scanner(System.in);
		System.out.println("ON/OFF: ");
		String inputSetting = inputeReader.nextLine();
		changeSettings(setting, inputSetting);
	}
	
	@Override
	public void changeSettings(SettingsMode setting, String inputSetting) {
		int currentSetting = -1;
		if(inputSetting.equalsIgnoreCase("ON") || inputSetting.equalsIgnoreCase("OFF")) {
			boolean isActive = inputSetting.equalsIgnoreCase("on");
			try {
				currentSetting = MusicPlayerDBAPIImpl.getInstance().getCurrentUserSettings();
			
				if(currentSetting != -1) {
					boolean isCurrentlyActive = setting.isTypeMatch(currentSetting);
					if(isActive != isCurrentlyActive) {
						int valueToUpdate;
						if(isActive) {
							valueToUpdate = currentSetting + setting.getMode();
						} else {
							valueToUpdate = currentSetting - setting.getMode();
						}
						boolean isValueUpdated = MusicPlayerDBAPIImpl.getInstance().updateSettings(valueToUpdate);
						if(isValueUpdated) {
							Cache.getInstance().cacheData(setting.toString(), isActive?"ON":"OFF");
							System.out.println(setting + " is turned " + (isActive?"ON":"OFF"));
							if(setting.equals(SettingsMode.AUTOPLAY) && !isActive) {
								MusicPlayerDBAPIImpl.getInstance().clearSongQueue();
								SongQueue.getInstance().setPlaying(true);
							} else if(setting.equals(SettingsMode.SHUFFLE)) {
								if(isActive) {
									SongQueueAPIImpl.getInstance().shuffleSongs();
								} else {
									SongQueueAPIImpl.getInstance().unShuffleSongs();
								}
							}
						}
					} else {
							System.out.println(setting + " is already " + (isActive?"ON":"OFF"));
					}
				} else {
					System.out.println("Issue while retreiving data from DB...");
				}
			} catch (SQLException e) {
				System.out.println(errorMsg);
			}
		} else {
			System.out.println("Invalid Input... Try Again...");
		}	
	}
	
	@Override
	public void displayLikedSongs() {
		try {
			MusicPlayerDBAPIImpl.getInstance().displayLikedSongs();
		} catch (Exception e) {
			System.out.println(errorMsg);
		}
	}
	
	@Override
	public void displayLikedPlayLists() {
		try {
			MusicPlayerDBAPIImpl.getInstance().displayLikedPlayLists();
		} catch (Exception e) {
			System.out.println(errorMsg);
		}
	}
	
	@Override
	public void displayFrequentlyPlayedGenre() {
		try {
			MusicPlayerDBAPIImpl.getInstance().displayFrequentlyPlayedGenre();
		} catch (Exception e) {
			System.out.println(errorMsg);
		}
	}
	
	@Override 
	public void displayFrequentlyPlayedSongs() {
		try {
			MusicPlayerDBAPIImpl.getInstance().displayFrequentlyPlayedSongs(true, true);
		} catch (Exception e) {
			System.out.println(errorMsg);
		}
	}
	
	@Override 
	public void displayMusicRecommendation() {
		try {
			MusicPlayerDBAPIImpl.getInstance().displayMusicRecommendation();
		} catch (Exception e) {
			System.out.println(errorMsg);
		}
	}
	
	
	@Override
	public void changeUserTheme() {
		boolean isSuccess = false;
		Scanner inputReader = new Scanner(System.in);
		for(DisplayColor color: DisplayColor.values()) {
			MusicPlayerMainMenu.getInstance().printHighlightMessage(color.name(), color);
		}
		System.out.println("Selected theme: ");
		String colorInput = inputReader.next().toUpperCase();
		 try {
	        DisplayColor selectedColor = DisplayColor.valueOf(colorInput);
	        if(selectedColor.name().equals(Cache.getInstance().getDataFromCache("theme"))) {
	        	System.out.println("The theme is already " + selectedColor.name() + "...");
	        	return;
	        }
	        isSuccess = MusicPlayerDBAPIImpl.getInstance().setUserTheme(selectedColor.name());
	        if(isSuccess) {
	        	Cache.getInstance().cacheData("theme", selectedColor.name());
	        	MusicPlayerMainMenu.getInstance().printHighlightMessage("Theme Changed Successfully", selectedColor);
	        } else {
	        	System.out.println("Failed to change theme...");
	        }
	    } catch (IllegalArgumentException e) {
	        System.out.println("Invalid color. Please enter a valid color.");
	    }
	}
	
	@Override
	public void displaySettings() {
		System.out.println("Settings");
		System.out.println("------------------");
		for(SettingsMode mode: SettingsMode.values()) {
			System.out.println(mode + ": " + Cache.getInstance().getDataFromCache(mode.toString()));
		}
	}
	
	@Override
	public void caCheSettings(boolean isSignUp) {
		int currentSetting = -1;
		if(isSignUp) {
			currentSetting = 0;
		} else {
			try {
				currentSetting = MusicPlayerDBAPIImpl.getInstance().getCurrentUserSettings();
			} catch (Exception e) {
				return;
			}
		}
		if(currentSetting != -1) {
			for(SettingsMode mode: SettingsMode.values()) {
				Cache.getInstance().cacheData(mode.toString(), mode.isTypeMatch(currentSetting)?"ON":"OFF");
			}
		}
	}
	
	public void displayUserDetails() {
		System.out.println("User Details");
		System.out.println("UserId: " + Cache.getInstance().getDataFromCache("USER_ID"));
		System.out.println("UserName: " + Cache.getInstance().getDataFromCache("userName"));
		
		try {
			Method method = MusicPlayerDBAPIImpl.class.getDeclaredMethod("getUserPassword");
			method.setAccessible(true);
			Object password = method.invoke(MusicPlayerDBAPIImpl.getInstance());
			if(password != null) {
				System.out.println("Password: " + password);
			}
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | InvocationTargetException | IllegalArgumentException e) {
			e.printStackTrace();
		}    
	}
	
	public void changeUserProfile() {
		Scanner inputReader = new Scanner(System.in);
		displayUserSettings();
		int choice = inputReader.nextInt(); // provide invalid input and handle the case
		switch (choice) {
		case 1:
			changeUserName();
			break;
		case 2:
			changePassword();
			break;
		default:
			System.out.println("Invalid Input...");
			break;
		}
	}
	
	private void changeUserName() {
		Scanner inputReader = new Scanner(System.in);
		System.out.println("Enter New UserName: ");
		String newUserName = inputReader.nextLine();
		try {
			if(MusicPlayerDBAPIImpl.getInstance().passwordValidation()) {
				if(MusicPlayerDBAPIImpl.getInstance().changeUserName(newUserName)) {
					Cache.getInstance().cacheData("userName", newUserName);
					System.out.println("UserName changes to " + newUserName);
				}
			} else {
				System.out.println("Invalid Password...");
			}
		} catch (SQLException e) {
			System.out.println("Something went wrong...");
			e.printStackTrace();
		}
	}
	
	private void changePassword() {
		Scanner inputReader = new Scanner(System.in);
		System.out.println("Enter New Password: ");
		String newPassword = inputReader.nextLine();
		try {
			if(MusicPlayerDBAPIImpl.getInstance().passwordValidation()) {
				if(MusicPlayerDBAPIImpl.getInstance().changePassword(newPassword)) {
					System.out.println("Password changed Successfully...");
				}
			} else {
				System.out.println("Invalid Password...");
			}
		} catch (SQLException e) {
			System.out.println("Something went wrong...");
			e.printStackTrace();
		}
	}
	
	private void displayUserSettings() {
		System.out.println("User Settings");
		System.out.println("------------------");
		System.out.println("1. Change User Name");
		System.out.println("2. Change password");
		System.out.println("Enter your choice: ");		
	}
}
