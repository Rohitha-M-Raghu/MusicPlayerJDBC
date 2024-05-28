//$Id$
package ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import artist.ArtistAPIImp;
import authentication.UserAuthenticationAPIImpl;
import cache.Cache;
import dbmanager.MusicPlayerDBAPIImpl;
import playlist.PlaylistAPIImpl;
import playsongs.PlaySongsAPIImpl;
import queue.SongQueue;
import queue.SongQueueAPIImpl;
import song.SettingsMode;
import song.Song;
import user_preference.DreamMode;
import user_preference.UserPreferenceAPIImpl;

public class MusicPlayerMainMenu {
	private int choice;
	
	public static MusicPlayerMainMenu getInstance() {
		return MusicPlayerMainMenuInstance.INSTANCE;
	}
	
	private static class MusicPlayerMainMenuInstance {
		private static final MusicPlayerMainMenu INSTANCE = new MusicPlayerMainMenu();
	}
	
	public void mainMenu() {
		MenuFactory menuFactory = new MenuFactory();
		SongQueue.getInstance().setRunning(true);
		do {
			// display current playing song...
			List<Integer> skipDisplayForChoices = new ArrayList<>(Arrays.asList(1, 4, 7, 8, 24, 33)); 
			if(!skipDisplayForChoices.contains(choice)) {
				displayCurrentPlayingSong();
			}
			choice = displayMainMenu();
			switch (choice) {
			case -1:
				System.out.println("Invalid Input... Try Again");
				break;
			case 1:
				MusicPlayerDBAPIImpl.getInstance().displayAllSongs();
				int songId = PlaySongsAPIImpl.getInstance().playSong();
				displayCurrentPlayingSong(songId);
				break;
			case 2:
				// pause player
				SongQueue.getInstance().setPlaying(false);
				System.out.println("Music Player paused");
				break;
			case 3:
				// resume player
				SongQueue.getInstance().setPlaying(true);
				System.out.println("Music Player playing");
				break;
			case 4:
				// display all playlists  limit 10
				playPlaylist();
				break;
			case 5:
				// display all songs limit 50
				MusicPlayerDBAPIImpl.getInstance().displayAllSongs();
				PlaySongsAPIImpl.getInstance().addSongToQueue();
				break;
			case 6:
				// display all songs limit 50
				MusicPlayerDBAPIImpl.getInstance().displayAllSongs();
				PlaySongsAPIImpl.getInstance().playSongNext();
				break;
			case 7:
				PlaySongsAPIImpl.getInstance().skipToNextTrack();
				displayCurrentPlayingSong(-1);
				break;
			case 8:
				PlaySongsAPIImpl.getInstance().skipToPrevTrack();
				displayCurrentPlayingSong(-1);
				break;
			case 9:
				UserPreferenceAPIImpl.getInstance().likeCurrentPlayingSong();
				break;
			case 10:
				UserPreferenceAPIImpl.getInstance().likeASong();
				break;
			case 11:
				UserPreferenceAPIImpl.getInstance().likeAPlayList();
				break;
			case 12:
				UserPreferenceAPIImpl.getInstance().changeSettings(SettingsMode.SHUFFLE);
				break;
			case 13:
				UserPreferenceAPIImpl.getInstance().changeSettings(SettingsMode.LOOP);
				break;
			case 14:
				UserPreferenceAPIImpl.getInstance().changeSettings(SettingsMode.AUTOPLAY);
				break;
			case 15:
				PlaylistAPIImpl.getInstance().createPlayList();
				break;
			case 16:
				PlaylistAPIImpl.getInstance().addSongToPlayList();
				break;
			case 17:
				PlaylistAPIImpl.getInstance().addCurrentPlayingSongToPlayList();
				break;
			case 18:
				PlaylistAPIImpl.getInstance().displayPlaylistSongs();
				break;
			case 19:
				UserPreferenceAPIImpl.getInstance().displayLikedSongs();
				break;
			case 20:
				UserPreferenceAPIImpl.getInstance().displayLikedPlayLists();
				break;
			case 21:
				UserPreferenceAPIImpl.getInstance().displayFrequentlyPlayedSongs();
				break;
			case 22:
				UserPreferenceAPIImpl.getInstance().displaySettings();
				break;
			case 23:
				UserPreferenceAPIImpl.getInstance().displayFrequentlyPlayedGenre();
				break;
			case 24:
				removeSongFromQueue();
				break;
			case 25:
				// reflection
				menuFactory.invokeMethod(25);
				break;
			case 26:
				PlaylistAPIImpl.getInstance().removeSongFromPlaylist();
				break;
			case 27:
				// reflection
				menuFactory.invokeMethod(27);
				break;
			case 28:
				DreamMode.getInstance().setDreamMode();
				break;
			case 29:
				DreamMode.getInstance().resetDreamMode();
				break;
			case 30:
				UserPreferenceAPIImpl.getInstance().displayUserDetails();
				break;
			case 31:
				SongQueueAPIImpl.getInstance().displayStatistics();
				break;
			case 32:
				UserPreferenceAPIImpl.getInstance().changeUserProfile();
				break;
			case 33:
				int songID = PlaySongsAPIImpl.getInstance().searchSongOrPlaylist();
				displayCurrentPlayingSong(songID);
				break;
			case 34:
				PlaylistAPIImpl.getInstance().renamePlaylist();
				break;
			case 35:
				PlaylistAPIImpl.getInstance().deletePlaylist();
				break;
			case 36:
				ArtistAPIImp.getInstance().getArtistDetails();
				break;
			case 37:
				UserPreferenceAPIImpl.getInstance().displayMusicRecommendation();
				break;
			case 38:
				UserPreferenceAPIImpl.getInstance().unlikeASong();
				break;
			case 39:
				UserPreferenceAPIImpl.getInstance().unlikePlaylist();
				break;
			case 40:
				MusicPlayerDBAPIImpl.getInstance().displayAllSongs();
				SongQueueAPIImpl.getInstance().displaySongLyrics();
				break;
			case 41:
				UserAuthenticationAPIImpl.getInstance().manageSignOut();
				break;
			case 42:
				UserAuthenticationAPIImpl.getInstance().exitApplication();
				System.out.println("Exiting application...");
				System.exit(0);
				break;
			default:
				System.out.println("Something went wrong... Try Again");
				break;
			}
		} while(choice != 41 && choice != 42);
		// stop thread
		SongQueue.getInstance().setRunning(false);
	}
	
	public static int displayMainMenu() {
		Scanner inputeReader = new Scanner(System.in);
		System.out.println();
		System.out.println();
		String musicPlayerName = Cache.getInstance().getDataFromCache("userName").toUpperCase() + "'s Music Player";
		MusicPlayerMainMenuInstance.INSTANCE.printHighlightMessage(musicPlayerName, DisplayColor.valueOf(Cache.getInstance().getDataFromCache("theme")));
		System.out.println("-------------------------");
		System.out.println("1. Play Song");
		System.out.println("2. Pause Music Player");
		System.out.println("3. Play Music Player");
		System.out.println("4. Play playlist");
		System.out.println("5. Add Song to Queue");
		System.out.println("6. Play this Song next");
		System.out.println("7. Skip to next track");
		System.out.println("8. Skip to prev track");
		System.out.println("9. Like current playing Song");
		System.out.println("10. Like a song");
		System.out.println("11. Like a playlist");
		System.out.println("12. Shuffle");
		System.out.println("13. Loop");
		System.out.println("14. Autoplay");
		System.out.println("15. Create Playlist");
		System.out.println("16. Add song to playlist");
		System.out.println("17. Add Current Playing Song to Playlist");
		System.out.println("18. Display playlist songs");
		System.out.println("19. Display liked songs");
		System.out.println("20. Display liked playlist");
		System.out.println("21. Display frequently played songs");
		System.out.println("22. Display Settings");
		System.out.println("23. Display Most Listened Genre");
		System.out.println("24. Remove Song from Queue");
		System.out.println("25. Clear Queued Songs");
		System.out.println("26. Remove Song From Playlist");
		System.out.println("27. Change music player theme");
		System.out.println("28. Dream Mode");
		System.out.println("29. Turn off Dream Mode");
		System.out.println("30. User Data");
		System.out.println("31. App Music Statistics");
		System.out.println("32. Change User Profile/password");
		System.out.println("33. Search and Play Song/Playlist");
		System.out.println("34. Rename Playlist");
		System.out.println("35. Delete Playlist");
		System.out.println("36. Artist Details");
		System.out.println("37. Music Recommender");
		System.out.println("38. Unlike Song");
		System.out.println("39. Unlike Playlist");
		System.out.println("40. Song Lyrics");
		System.out.println("41. Sign out");
		System.out.println("42. Exit application");	
		System.out.println("Enter your choice: ");
		try {
			return inputeReader.nextInt();
		} catch (Exception e) {
			return -1;
		} 
	}
	
	private void removeSongFromQueue() {
		boolean isDeletedCurrentPlayingSong = SongQueueAPIImpl.getInstance().removeSongFromQueue();
		if(isDeletedCurrentPlayingSong) {
			choice = 7;
			displayCurrentPlayingSong(-1);
		} else {
			displayCurrentPlayingSong();
		}
	}
	
	private void playPlaylist() {
		if(MusicPlayerDBAPIImpl.getInstance().displayUserPlaylists()) {
			int firstSongId = PlaySongsAPIImpl.getInstance().playPlayList();
			displayCurrentPlayingSong(firstSongId);
		}
	}
	
	private void displayCurrentPlayingSong() {
		System.out.println();
		printHighlightMessage("************************", DisplayColor.GREEN);
		Song songDetails = MusicPlayerDBAPIImpl.getInstance().getCurrentPlayingSongDetails();
		
		if(songDetails == null) {
			printHighlightMessage("Nothing is playing...", DisplayColor.BLUE);
		} else {
			printHighlightMessage("SongTitle: " + songDetails.getSongTitle(), DisplayColor.BLUE);
			printHighlightMessage("Artist: " + songDetails.getArtistName(), DisplayColor.BLUE);
		}
		printHighlightMessage("************************", DisplayColor.GREEN);
	}
	
	private void displayCurrentPlayingSong(int songId) {
		System.out.println();
		printHighlightMessage("************************", DisplayColor.GREEN);
		Song songDetails = null;
		
		try {
			if(choice == 7 || choice == 8) {
				if(choice == 7) {
					songDetails = MusicPlayerDBAPIImpl.getInstance().getNextSongInQueue(true);
				} else {
					songDetails = MusicPlayerDBAPIImpl.getInstance().getPrevSongInQueue(true);
				}
				if(songDetails == null) {
					if(choice == 8) {
						songDetails = MusicPlayerDBAPIImpl.getInstance().getCurrentPlayingSongDetails();
						boolean isLoopEnabled = Cache.getInstance().getDataFromCache(SettingsMode.LOOP.toString()).equals("ON");
						if (isLoopEnabled) {
							songDetails = MusicPlayerDBAPIImpl.getInstance().getLastSongInQueue(true);
						}
					}
					
					if(choice == 7) {
						boolean isAutoplayEnabled = Cache.getInstance().getDataFromCache(SettingsMode.AUTOPLAY.toString()).equals("ON");
						boolean isLoopEnabled = Cache.getInstance().getDataFromCache(SettingsMode.LOOP.toString()).equals("ON");
						if(isAutoplayEnabled) {
							songDetails = MusicPlayerDBAPIImpl.getInstance().getFirstSongDetails();
						} else if(isLoopEnabled) {
							songDetails = MusicPlayerDBAPIImpl.getInstance().getFirstSongInQueue(true);
						}
					}
					// handle looping case
				}
			} else {
				songDetails = MusicPlayerDBAPIImpl.getInstance().getSongDetails(songId);
			}
		} catch (Exception e) {
			songDetails = null;
		}
		
		if(songDetails == null) {
			printHighlightMessage("Nothing is playing...", DisplayColor.BLUE);
		} else {
			printHighlightMessage("SongTitle: " + songDetails.getSongTitle(), DisplayColor.BLUE);
			printHighlightMessage("Artist: " + songDetails.getArtistName(), DisplayColor.BLUE);
		}
		printHighlightMessage("************************", DisplayColor.GREEN);
	}
	
	public void printHighlightMessage(String message, DisplayColor color) {
	    String ansiReset = "\u001B[0m";   // ANSI code to reset color
	    System.out.println(color.getAnsiCode() + message + ansiReset);
	}
}
