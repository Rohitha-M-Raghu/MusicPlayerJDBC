//$Id$
package song;

public class Song {
	private int songId;
	private String songTitle;
	private String artistName;
	private String duration;
	private String genre;
	private double order;
	private long count;
	private int artistId;
	
	public int getArtistId() {
		return artistId;
	}

	public void setArtistId(int artistId) {
		this.artistId = artistId;
	}

	public long getCount() {
		return count;
	}
	
	public Song() {
		super();
	}

	public Song(int songId, String songTitle) {
		super();
		this.songId = songId;
		this.songTitle = songTitle;
	}
	
	public void setCount(long count) {
		this.count = count;
	}
	public String getDuration() {
		return duration;
	}
	public void setDuration(String duration) {
		this.duration = duration;
	}
	public String getGenre() {
		return genre;
	}
	public void setGenre(String genre) {
		this.genre = genre;
	}
	public String getArtistName() {
		return artistName;
	}
	public void setArtistName(String artistName) {
		this.artistName = artistName;
	}
	public int getSongId() {
		return songId;
	}
	public void setSongId(int songId) {
		this.songId = songId;
	}
	public String getSongTitle() {
		return songTitle;
	}
	public void setSongTitle(String songTitle) {
		this.songTitle = songTitle;
	}
	public double getOrder() {
		return order;
	}
	public void setOrder(double order) {
		this.order = order;
	}
	
	public long getSongDurationInSecs() {
		if(duration == null) {
			return -1;
		}
		String[] time = duration.split(":");
		return ((3600L * Integer.parseInt(time[0])) + 60L * Integer.parseInt(time[1]) + Integer.parseInt(time[2]))*1000;
	}
}
