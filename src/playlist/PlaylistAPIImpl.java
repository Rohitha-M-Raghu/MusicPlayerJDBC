//$Id$
package playlist;

import java.sql.SQLException;
import java.util.Scanner;

import dbmanager.MusicPlayerDBAPIImpl;
import exception.PlaylistOrSongNotFoundException;
import song.Song;

public class PlaylistAPIImpl implements PlaylistAPI{
	
	public static PlaylistAPIImpl getInstance() {
		return PlaylistAPIImplInstance.INSTANCE;
	}
	
	private static class PlaylistAPIImplInstance {
		private static final PlaylistAPIImpl INSTANCE = new PlaylistAPIImpl();
	}
	
	private static final String ERRORMSG = "Something went wrong... Please try again...";
	
	@Override
	public void createPlayList() {
		Scanner inputeReader = new Scanner(System.in);
		boolean isSuccess = false;
		System.out.println("Enter PlayList Name: ");
		String playListName = inputeReader.nextLine();
		try {
			isSuccess = MusicPlayerDBAPIImpl.getInstance().addNewPlayList(playListName);
		} catch (SQLException e) {
			System.out.println(ERRORMSG);
		}
		if(isSuccess) {
			System.out.println(playListName + " created...");
		}
	}
	
	@Override
	public void addSongToPlayList() {
		Scanner inputeReader = new Scanner(System.in);
		boolean isSuccess = false;
		System.out.println("Enter SongTitle: ");
		String songTitle = inputeReader.nextLine();
		System.out.println("Enter Artist Name: ");
		String artistName = inputeReader.nextLine();
		System.out.println("Enter PlayList Name: ");
		String playListName = inputeReader.nextLine();
		
		// add song to playlist
		try {
			isSuccess = MusicPlayerDBAPIImpl.getInstance().addSongToPlayList(songTitle, playListName, artistName);
		} catch (SQLException e) {
			System.out.println(ERRORMSG);
		}
		if(isSuccess) {
			System.out.println(songTitle + " added to playlist " + playListName);
		}
	}
	
	@Override
	public void addCurrentPlayingSongToPlayList() {
		Song currentPlayingSong = MusicPlayerDBAPIImpl.getInstance().getCurrentPlayingSongDetails();
		if(currentPlayingSong == null) {
			System.out.println("No Song playing right now...");
			return;
		}
		
		Scanner inputeReader = new Scanner(System.in);
		boolean isSuccess = false;
		System.out.println("Enter PlayList Name: ");
		String playListName = inputeReader.nextLine();
		
		// add song to playlist
		try {
			isSuccess = MusicPlayerDBAPIImpl.getInstance().addSongToPlayList(currentPlayingSong.getSongTitle(), playListName, currentPlayingSong.getArtistName());
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println(ERRORMSG);
		}
		if(isSuccess) {
			System.out.println(currentPlayingSong.getSongTitle() + " added to playlist " + playListName);
		}
	}
	
	@Override
	public void removeSongFromPlaylist() {
		Scanner inputeReader = new Scanner(System.in);
		boolean isSuccess = false;
		System.out.println("Enter SongTitle: ");
		String songTitle = inputeReader.nextLine();
		System.out.println("Enter artistName: ");
		String artistName = inputeReader.nextLine();
		System.out.println("Enter PlayList Name: ");
		String playListName = inputeReader.nextLine();
		
		// add song to playlist
		try {
			isSuccess = MusicPlayerDBAPIImpl.getInstance().removeSongFromPlaylist(songTitle,artistName, playListName);
		} catch (SQLException e) {
			System.out.println(ERRORMSG);
		} catch (PlaylistOrSongNotFoundException e) {
			System.out.println(e.getMessage());
		}
		if(isSuccess) {
			System.out.println(songTitle + " removed from playlist " + playListName);
		}
	}
	
	@Override
	public void displayPlaylistSongs() {
		Scanner inputeReader = new Scanner(System.in);
		System.out.println("Enter PlayList Name: ");
		String playListName = inputeReader.nextLine();
		try {
			MusicPlayerDBAPIImpl.getInstance().displayPlaylistSongs(playListName);
		} catch (PlaylistOrSongNotFoundException e) {
			System.out.println(e.getMessage());
		} catch (Exception e) {
			System.out.println(ERRORMSG);
		}
	}
	
	@Override
	public void renamePlaylist() {
		if(!MusicPlayerDBAPIImpl.getInstance().displayUserPlaylists()) {
			return;
		}
		Scanner inputeReader = new Scanner(System.in);
		System.out.println("Enter PlayList Name: ");
		String playListName = inputeReader.nextLine();
		try {
			int playlistId = MusicPlayerDBAPIImpl.getInstance().getPlaylistId(playListName);
			if(playlistId == -1) {
				System.out.println("Playlist Not Found...");
				return;
			}
			
			System.out.println("Enter new Playlist name: ");
			String newPlaylistName = inputeReader.nextLine();
			boolean isSuccess = MusicPlayerDBAPIImpl.getInstance().renamePlaylist(playlistId, newPlaylistName);
			if(isSuccess) {
				System.out.println("Playlist " + playListName + " renamed to " + newPlaylistName + "... ");
			} else {
				System.out.println("Failed to rename playlist " + playListName);
			}
		} catch (SQLException e) {
			System.out.println(ERRORMSG);
			e.printStackTrace();
		}
	}
	
	@Override
	public void deletePlaylist() {
		if(!MusicPlayerDBAPIImpl.getInstance().displayUserPlaylists()) {
			return;
		}
		Scanner inputeReader = new Scanner(System.in);
		System.out.println("Enter PlayList Name: ");
		String playListName = inputeReader.nextLine();
		try {
			int playlistId = MusicPlayerDBAPIImpl.getInstance().getPlaylistId(playListName);
			if(playlistId == -1) {
				System.out.println("Playlist Not Found...");
				return;
			}
			boolean isSuccess = MusicPlayerDBAPIImpl.getInstance().deletePlaylist(playlistId);
			if(isSuccess) {
				System.out.println("Deleted Playlist " + playListName + "...");
			} else {
				System.out.println("Failed to delete playlist " + playListName + "...");
			}
		} catch (SQLException e) {
			System.out.println(ERRORMSG);
			e.printStackTrace();
		}
	}
}
