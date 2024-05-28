//$Id$
package playlist;

public interface PlaylistAPI {

	void createPlayList();

	void addSongToPlayList();

	void displayPlaylistSongs();

	void removeSongFromPlaylist();

	void addCurrentPlayingSongToPlayList();

	void renamePlaylist();

	void deletePlaylist();

}
