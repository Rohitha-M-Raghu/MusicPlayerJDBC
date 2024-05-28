//$Id$
package songdata;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import dbmanager.MusicPlayerDBAPIImpl;
import song.Song;

public class SongData {
	
	public static SongData getInstance() {
		return SongDataInstance.INSTANCE;
	}
	
	private static class SongDataInstance {
		private static final SongData INSTANCE = new SongData();
	}
	
	private Path getFilePath() throws IOException {
			
		Path filePath = Paths.get("SongData.txt");
		
		if (Files.notExists(filePath)) {
			try {
				Set<PosixFilePermission> permissions = PosixFilePermissions.fromString("rw-------");
				FileAttribute<Set<PosixFilePermission>> attributes = PosixFilePermissions.asFileAttribute(permissions);
				Files.createFile(filePath,attributes);
			} catch (IOException e) {
				throw new IOException("unable to create History.txt");
			}
		}
		
		return filePath;
	}
		
	public void writeHistoryFromTable() throws IOException {
        Path filePath = getFilePath();
        String query = "SELECT Song_Details.*, Artist_Details.ARTIST_NAME FROM Song_Details "
        		+ "JOIN Artist_Details ON Song_Details.Artist_ID = Artist_Details.ARTIST_ID";
        
        try (Connection connection = MusicPlayerDBAPIImpl.getInstance().getConn(); 
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            List<String> songData = new ArrayList<>();
            while (resultSet.next()) {
            	Song songToWrite = new Song();
            	songToWrite.setSongId(resultSet.getInt("Song_Details.SONG_ID"));
            	songToWrite.setSongTitle(resultSet.getString("Song_Details.SONG_TITLE"));
                songToWrite.setArtistName(resultSet.getString("Artist_Details.ARTIST_NAME"));
                songToWrite.setDuration(resultSet.getString("Song_Details.DURATION"));
                songToWrite.setGenre(resultSet.getString("Song_Details.GENRE"));
                songToWrite.setOrder(resultSet.getDouble("Song_Details.ORDER"));
                

                String songInfo = songToWrite.getSongId() + ", " 
                		+ songToWrite.getSongTitle() + ", " + songToWrite.getArtistName() + ", " 
                		+ songToWrite.getDuration() + ", " + songToWrite.getGenre() + ", " 
                		+ (int) songToWrite.getOrder() ;
                songData.add(songInfo);
            }

            Files.write(filePath, songData, StandardCharsets.UTF_8);
        } catch (SQLException e) {
            // Handle SQLException
            e.printStackTrace();
        }
    }

	public void populateSongDetailsFromFile() throws IOException {
		Path filePath = getFilePath();
        List<String> fileLines = Files.readAllLines(filePath, StandardCharsets.UTF_8);
        List<Song> songs = parseFileLines(fileLines);

        try (Connection connection = MusicPlayerDBAPIImpl.getInstance().getConn()) {
            String query = "INSERT INTO Song_Details (SONG_ID, SONG_TITLE, ARTIST_ID, DURATION, GENRE) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                for (Song song : songs) {
                	preparedStatement.setInt(1, song.getSongId());
                    preparedStatement.setString(2, song.getSongTitle());
                    preparedStatement.setString(3, song.getArtistName());
                    preparedStatement.setTime(4, parseDuration(song.getDuration()));     
                    preparedStatement.setString(5, song.getGenre());
                    preparedStatement.addBatch();
                }
                preparedStatement.executeBatch();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<Song> parseFileLines(List<String> fileLines) {
        List<Song> songs = new ArrayList<>();
        for (String line : fileLines) {
            String[] songData = line.split(", ");
            if (songData.length == 6) {
                Song song = new Song();
                songs.add(song);
                song.setSongId(Integer.parseInt(songData[0]));
                song.setSongTitle(songData[1]);
                song.setArtistName(songData[2]);
                song.setDuration(songData[3]);
                song.setGenre(songData[4]);
            }
        }
        return songs;
    }
    
    private Time parseDuration(String durationString) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        long timeInMillis = dateFormat.parse(durationString).getTime();
        return new Time(timeInMillis);
    }
}
