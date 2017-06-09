package com.hwatong.ipod;

import com.hwatong.ipod.ICallback;
import com.hwatong.ipod.Playlist;
import com.hwatong.ipod.MediaItem;
import com.hwatong.ipod.Artist;
import com.hwatong.ipod.Album;
import com.hwatong.ipod.Genre;
import com.hwatong.ipod.Composer;
import com.hwatong.ipod.NowPlaying;

interface IService {
	boolean isAttached();

	void registerCallback(ICallback cb);
	void unregisterCallback(ICallback cb);	

    List<Playlist> getPlaylistList();
    List<MediaItem> getMediaItemsInPlaylist(String playlistIdentifier);

    List<Artist> getArtistList();
    List<MediaItem> getMediaItemsByArtist(String artistIdentifier);

    List<Album> getAlbumList();
    List<MediaItem> getMediaItemsByAlbum(String albumIdentifier);

    List<Genre> getGenreList();
    List<MediaItem> getMediaItemsByGenre(String genreIdentifier);

    List<Composer> getComposerList();
    List<MediaItem> getMediaItemsByComposer(String composerIdentifier);

    int getMediaItemCount();
    MediaItem getMediaItem(int i);

	void playInPlaylist(String playlistIdentifier, int position);
	void playMediaLibraryItems(in String[] Identifiers, int NumberOfIdentifiers, int ItemsStartingIndex);

    NowPlaying getNowPlaying(boolean full);

	void play();
	void pause();
	void previous();
	void next();
	void playPause();
	void repeat();
	void shuffle();
}
