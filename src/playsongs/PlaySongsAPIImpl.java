//$Id$
package playsongs;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Scanner;

import cache.Cache;
import dbmanager.MusicPlayerDBAPIImpl;
import exception.PlaylistOrSongNotFoundException;
import queue.SongQueue;
import song.SettingsMode;
import song.Song;
import user_preference.UserPreferenceAPIImpl;

public class PlaySongsAPIImpl implements PlaySongsAPI{
	
	public static PlaySongsAPIImpl getInstance() {
		return PlaySongsAPIImpInstance.INSTANCE;
	}
	
	private static class PlaySongsAPIImpInstance {
		private static final PlaySongsAPIImpl INSTANCE = new PlaySongsAPIImpl(); 
	}
	
	@Override
	public int playSong() {
		Scanner inputeReader = new Scanner(System.in);
		System.out.println("Enter SongTitle: ");
		String songTitle = inputeReader.nextLine();
		System.out.println("Enter ArtistName: ");
		String artistName = inputeReader.nextLine();
		int songId = -1;
		
		try {
			Song song  = MusicPlayerDBAPIImpl.getInstance().getSongDetails(songTitle, artistName);
			if(song == null) {
		    	throw new PlaylistOrSongNotFoundException("Cannot find " + songTitle + " by " + artistName);
		    }
			songId = song.getSongId();
		} catch (PlaylistOrSongNotFoundException e) {
			System.out.println(e.getMessage());
		} catch (Exception e) {
			System.out.println("Failed to play song...");
			return -1;
		}
		return playSong(songId);
	}
	
	@Override
	public int playSong(int songId) {
		Connection conn = MusicPlayerDBAPIImpl.getInstance().getConn();
		
		try {
			conn.setAutoCommit(false);
			MusicPlayerDBAPIImpl.getInstance().clearSongQueue();
			MusicPlayerDBAPIImpl.getInstance().addSongToQueue(songId, 1.0, -1);
			MusicPlayerDBAPIImpl.getInstance().setCurrentPlayingSong(songId);
			conn.commit();
			// shuffle off if on
			if(Cache.getInstance().getDataFromCache(SettingsMode.SHUFFLE.toString()) != null && Cache.getInstance().getDataFromCache(SettingsMode.SHUFFLE.toString()).equals("ON")) {
				UserPreferenceAPIImpl.getInstance().changeSettings(SettingsMode.SHUFFLE, "OFF");
			}
			
		} catch (Exception e) {
			try {
				conn.rollback();
			} catch (SQLException e1) {
				System.out.println("Failed roll back transaction...");
			}
			System.out.println("Failed to play song...");
			return -1;
		} finally {
			try {
				conn.setAutoCommit(true);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return songId;
	}
	
	@Override
	public int playPlayList() {
		Scanner inputeReader = new Scanner(System.in);
		System.out.println("Enter Playlist Name: ");
		String playListName = inputeReader.nextLine();
		int playListId;
		// check if playlist exists
		try {
			playListId = MusicPlayerDBAPIImpl.getInstance().getPlaylistId(playListName);
			if(playListId == -1) {
				System.out.println("Playlist doesn't exist...");
				return -1;
			}
			return playPlaylist(playListId);
		} catch (SQLException e) {

			System.out.println("Something went wrong... Try Again...");
		}
		
		return -1;
	}
	
	@Override
	public int playPlaylist(int playListId) {
		Connection conn = MusicPlayerDBAPIImpl.getInstance().getConn();
		try {
			conn.setAutoCommit(false);
			MusicPlayerDBAPIImpl.getInstance().clearSongQueue();
			Song firstSong = MusicPlayerDBAPIImpl.getInstance().addAllPlaylistSongsToQueue(true, playListId);
			if(firstSong == null) {
				conn.rollback();
				System.out.println("No Songs in playlist...");
				return -1;
			}
			MusicPlayerDBAPIImpl.getInstance().setCurrentPlayingSong(firstSong.getSongId());
			conn.commit();
			
			// shuffle off if on
			if(Cache.getInstance().getDataFromCache(SettingsMode.SHUFFLE.toString()) != null && Cache.getInstance().getDataFromCache(SettingsMode.SHUFFLE.toString()).equals("ON")) {
				UserPreferenceAPIImpl.getInstance().changeSettings(SettingsMode.SHUFFLE, "OFF");
			}
			return firstSong.getSongId();
		} catch (Exception e) {
			try {
				conn.rollback();
			} catch (SQLException e1) {
				System.out.println("Failed roll back transaction...");
			}
			System.out.println("Failed to play playlist...");
		} finally {
			try {
				conn.setAutoCommit(true);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return -1;
	}
	
	@Override
	public void addSongToQueue() {
		Scanner inputeReader = new Scanner(System.in);
		int songId = -1;
		System.out.println("Enter SongTitle: ");
		String songTitle = inputeReader.nextLine();
		System.out.println("Enter ArtistName: ");
		String artistName = inputeReader.nextLine();
		// add song to end of queue
		try {
			Song song = MusicPlayerDBAPIImpl.getInstance().getSongDetails(songTitle, artistName);
			if(song == null) {
		    	throw new PlaylistOrSongNotFoundException("Song not found");
		    }
			songId = song.getSongId();
			double order = MusicPlayerDBAPIImpl.getInstance().getHighestOrder() + 1;
			MusicPlayerDBAPIImpl.getInstance().addSongToQueue(songId, order, -1);
			System.out.println(songTitle + " added to Queue...");
			
			// shuffle off if on
			if(Cache.getInstance().getDataFromCache(SettingsMode.SHUFFLE.toString()) != null && Cache.getInstance().getDataFromCache(SettingsMode.SHUFFLE.toString()).equals("ON")) {
				UserPreferenceAPIImpl.getInstance().changeSettings(SettingsMode.SHUFFLE, "OFF");
			}
		} catch (SQLException e) {
			System.out.println("Failed to add song...");	
		} catch (PlaylistOrSongNotFoundException e) {
			System.out.println(e.getMessage());
		}
	}
	
	@Override
	public void playSongNext() {
		int songId;
		Scanner inputeReader = new Scanner(System.in);
		System.out.println("Enter SongTitle: ");
		String songTitle = inputeReader.nextLine();
		System.out.println("Enter ArtistName: ");
		String artistName = inputeReader.nextLine();
		// add song to begining of queue
		try {
			Song song = MusicPlayerDBAPIImpl.getInstance().getSongDetails(songTitle, artistName);
			if(song == null) {
		    	throw new PlaylistOrSongNotFoundException("Song not found");
		    }
			songId = song.getSongId();
			
			double currentSongOrder = SongQueue.getInstance().getCurrentPlayingSongOrder();
			
			if(currentSongOrder == 0) {
				MusicPlayerDBAPIImpl.getInstance().addSongToQueue(songId, 1.0, -1);
				MusicPlayerDBAPIImpl.getInstance().setCurrentPlayingSong(songId);
			} else {
				double songOrder;
				Song nextSongInQueue = MusicPlayerDBAPIImpl.getInstance().getNextSongInQueue(false);
				if(nextSongInQueue == null) {
					songOrder = currentSongOrder + 1;
				} else {
					double nextSongOrder = nextSongInQueue.getOrder();
					songOrder = (currentSongOrder + nextSongOrder) / 2;
				}
				
				MusicPlayerDBAPIImpl.getInstance().addSongToQueue(songId, songOrder, -1);
				System.out.println("Song added...");
				// shuffle off if on
				if(Cache.getInstance().getDataFromCache(SettingsMode.SHUFFLE.toString()) != null && Cache.getInstance().getDataFromCache(SettingsMode.SHUFFLE.toString()).equals("ON")) {
					UserPreferenceAPIImpl.getInstance().changeSettings(SettingsMode.SHUFFLE, "OFF");
				}
			}
		} catch (Exception e) {
			System.out.println("Failed to add song...");
		}
	}
	
	@Override
	public void skipToNextTrack() {
		SongQueue.getInstance().setSkipTrack(true);
		System.out.println("Skipped to next song...");
	}
	
	@Override
	public void skipToPrevTrack() {
		SongQueue.getInstance().setSkipTrack(true);
		SongQueue.getInstance().setSkipToPrevTrack(true);
		System.out.println("Skipped to prev song...");
	}
	
	@Override
	public int searchSongOrPlaylist() {
		Scanner inputReader = new Scanner(System.in);
		System.out.println("Search: ");
		String searchContent = inputReader.next();
		try {
			return MusicPlayerDBAPIImpl.getInstance().searchSongOrPlaylist(searchContent);
		} catch (SQLException e) {
			System.out.println("Something went wrong while searching...");
			e.printStackTrace();
		}
		return -1;
	}
}
