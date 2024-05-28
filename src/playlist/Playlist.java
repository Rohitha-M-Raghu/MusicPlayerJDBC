//$Id$
package playlist;

public class Playlist {
	int playListId;
	String playlistName;
	
	public int getPlayListId() {
		return playListId;
	}
	public void setPlayListId(int playListId) {
		this.playListId = playListId;
	}
	public String getPlaylistName() {
		return playlistName;
	}
	public void setPlaylistName(String playlistName) {
		this.playlistName = playlistName;
	}
	public Playlist(int playListId, String playlistName) {
		super();
		this.playListId = playListId;
		this.playlistName = playlistName;
	}
}
