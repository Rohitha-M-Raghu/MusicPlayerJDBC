//$Id$
package exception;

public class PlaylistOrSongNotFoundException extends Exception {
	public PlaylistOrSongNotFoundException(String errorMsg) {
		super(errorMsg);
	}
	
	public PlaylistOrSongNotFoundException() {
		super("Playlist or song not found...");
	}
}
