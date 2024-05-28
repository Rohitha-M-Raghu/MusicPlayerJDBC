//$Id$
package dbmanager;

import java.sql.SQLException;
import java.util.List;

import exception.PlaylistOrSongNotFoundException;

public interface PlaylistDBAPI {
	public boolean addNewPlayList(String playListName) throws SQLException;
	
	public boolean addSongToPlayList(String songTitle, String playListName, String artistName) throws SQLException;
	
	public boolean removeSongFromPlaylist(String songTitle, String artistName,String playListName) throws SQLException, PlaylistOrSongNotFoundException;
	
	public int getLastOrderOfPlayListSongs(int playListId) throws SQLException;
	
	public boolean displayUserPlaylists();
	
	public void displayPlaylistSongs(String playListName) throws SQLException, PlaylistOrSongNotFoundException;
	
	public List<Integer> displayLikedPlayLists() throws SQLException;
	
	public boolean likeAPlayList(String playListName) throws SQLException, PlaylistOrSongNotFoundException;
	
	public int getPlaylistId(String playListName) throws SQLException;

	boolean renamePlaylist(int playlistId, String newPlaylistName) throws SQLException;

	boolean deletePlaylist(int playlistId) throws SQLException;

	boolean unlikeAPlaylist(int playlistId) throws SQLException;

	List<Integer> getLikedPlaylistSongs() throws SQLException;
}
