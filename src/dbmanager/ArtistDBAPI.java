//$Id$
package dbmanager;

import java.sql.SQLException;
import java.util.HashMap;

public interface ArtistDBAPI {

	HashMap<Integer, String> getArtistList();

	boolean displayArtistDetails(int artistId);

	int getArtistId(String artistName) throws SQLException;

//	boolean validateSongAndArtist(int songId, int artistId) throws SQLException;

}
