package lrandomdev.com.online.mp3player.dals;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.util.ArrayList;

import lrandomdev.com.online.mp3player.models.Album;
import lrandomdev.com.online.mp3player.models.Artist;

public class DALAlbum {
    Context context;
    ContentResolver contentResolver;

    public DALAlbum() {

    }


    public DALAlbum(Context context) {
        super();
        this.context = context;
        this.contentResolver = context.getContentResolver();
    }

    public ArrayList<Album> getAlbumOnMDS() {
        Uri uri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI;
        String[] projection = new String[] { MediaStore.Audio.Albums._ID,
                MediaStore.Audio.Albums.ALBUM, MediaStore.Audio.Albums.ARTIST,
                MediaStore.Audio.Albums.ALBUM_ART };
        Cursor cursor = contentResolver
                .query(uri, projection, null, null, null);
        return convertMediaStoreCursorToArrayList(cursor);
    }

    public ArrayList<Album> getAlbumsByIdOnMDS(int id) {
        Uri uri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI;
        String[] projection = new String[] { MediaStore.Audio.Albums._ID,
                MediaStore.Audio.Albums.ALBUM, MediaStore.Audio.Albums.ARTIST,
                MediaStore.Audio.Albums.ALBUM_ART };
        Cursor cursor = contentResolver.query(uri, projection,
                MediaStore.Audio.Albums._ID + "=" + id, null, null);
        return convertMediaStoreCursorToArrayList(cursor);
    }

    public ArrayList<Album> getAlbumsByTitleOnMDS(String title) {
        Uri uri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI;
        String[] projection = new String[] { MediaStore.Audio.Albums._ID,
                MediaStore.Audio.Albums.ALBUM, MediaStore.Audio.Albums.ARTIST,
                MediaStore.Audio.Albums.ALBUM_ART };
        Cursor cursor = contentResolver.query(uri, projection,
                MediaStore.Audio.Albums.ALBUM + " LIKE ?", new String[] { "%"
                        + title + "%" }, null);
        return convertMediaStoreCursorToArrayList(cursor);
    }

    private ArrayList<Album> convertMediaStoreCursorToArrayList(Cursor cursor) {
        ArrayList<Album> albumList = new ArrayList<Album>();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                Album album = new Album();
                album.setId(cursor.getInt(cursor
                        .getColumnIndex(MediaStore.Audio.Albums._ID)));
                album.setTitle(cursor.getString(cursor
                        .getColumnIndex(MediaStore.Audio.Albums.ALBUM)));
                ArrayList<Artist> artists = new ArrayList<>();
                Artist artist = new Artist();
                artist.setId(0);
                artist.setArtist(cursor.getString(cursor
                        .getColumnIndex(MediaStore.Audio.Albums.ARTIST)));
                artists.add(artist);
                album.setArtists(artists);
                album.setLocal(true);
                album.setThumb(cursor.getString(cursor
                        .getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART)));
                albumList.add(album);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return albumList;
    }
}
