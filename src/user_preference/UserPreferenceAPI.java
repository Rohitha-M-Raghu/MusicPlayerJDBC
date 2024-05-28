//$Id$
package user_preference;

import song.SettingsMode;

public interface UserPreferenceAPI {

	void likeCurrentPlayingSong();

	void likeASong();

	void likeAPlayList();

	void changeSettings(SettingsMode setting);

	void displayLikedSongs();

	void displayLikedPlayLists();

	void displayFrequentlyPlayedSongs();

	void displaySettings();

	void caCheSettings(boolean isSignUp);

	void displayFrequentlyPlayedGenre();

	void changeUserTheme();

	void displayMusicRecommendation();

	void changeSettings(SettingsMode setting, String inputSetting);

	void unlikeASong();

	void unlikePlaylist();

	void likeASong(String songTitle, String artistName);

}
