package lrandomdev.com.online.mp3player.dals;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.File;
import java.util.ArrayList;

import lrandomdev.com.online.mp3player.models.Track;

public class DALTrack {
    Context context;
    ContentResolver contentResolver;
    public DALTrack() {

    }

    public DALTrack(Context context) {
        super();
        this.context = context;
        this.contentResolver = context.getContentResolver();
    }


    public ArrayList<Track> getTracksOnMDS(String sort) {
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection = new String[] { MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM_ID, MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.DURATION };
        Cursor cursor = contentResolver
                .query(uri, projection, null, null, MediaStore.Audio.Media.TITLE+" "+sort.toUpperCase());
        return convertMediaStoreCursorToArrayList(cursor);
    }

    public ArrayList<Track> getTracksByTitleOnMDS(String title) {
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection = new String[] { MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM_ID, MediaStore.Audio.Media.DATA,MediaStore.Audio.Media.DURATION };
        Cursor cursor = contentResolver.query(uri, projection,
                MediaStore.Audio.Media.TITLE + " LIKE ?", new String[] { "%"
                        + title + "%" }, null);
        return convertMediaStoreCursorToArrayList(cursor);
    }

    public ArrayList<Track> getTracksByIdOnMDS(int id) {

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection = new String[] { MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM_ID, MediaStore.Audio.Media.DATA,MediaStore.Audio.Media.DURATION};
        Cursor cursor = contentResolver.query(uri, projection,
                MediaStore.Audio.Media._ID + "=" + id, null, null);
        return convertMediaStoreCursorToArrayList(cursor);
    }

    public ArrayList<Track> getTracksByAlbumIdOnMDS(int id) {
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection = new String[] { MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM_ID, MediaStore.Audio.Media.DATA,MediaStore.Audio.Media.DURATION };
        Cursor cursor = contentResolver.query(uri, projection,
                MediaStore.Audio.Media.ALBUM_ID + "=" + id, null, null);
        return convertMediaStoreCursorToArrayList(cursor);
    }

    public ArrayList<Track> getTracksByAlbumOnMDS(String name) {
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection = new String[] { MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM_ID, MediaStore.Audio.Media.DATA,MediaStore.Audio.Media.DURATION };
        Cursor cursor = contentResolver.query(uri, projection,
                MediaStore.Audio.Media.ALBUM + "=" + name, null, null);
        return convertMediaStoreCursorToArrayList(cursor);
    }

    public ArrayList<Track> getTracksByArtistIdOnMDS(int id) {
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection = new String[] { MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM_ID, MediaStore.Audio.Media.DATA,MediaStore.Audio.Media.DURATION };
        Cursor cursor = contentResolver.query(uri, projection,
                MediaStore.Audio.Media.ARTIST_ID + "=" + id, null, null);
        return convertMediaStoreCursorToArrayList(cursor);
    }

    public ArrayList<Track> getTracksByArtistOnMDS(String artistName) {
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection = new String[] { MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM_ID, MediaStore.Audio.Media.DATA,MediaStore.Audio.Media.DURATION };
        Cursor cursor = contentResolver.query(uri, projection,
                MediaStore.Audio.Media.ARTIST + "=" + artistName, null, null);
        return convertMediaStoreCursorToArrayList(cursor);
    }

    public Uri insertTracksOnMDS(Uri uri, ContentValues values) {
        return contentResolver.insert(uri, values);
    }

    public int removeTracksOnMDS(Uri uri, String where) {
        return contentResolver.delete(uri, where, null);
    }

    // end
    private ArrayList<Track> convertMediaStoreCursorToArrayList(Cursor cursor) {
        ArrayList<Track> trackList = new ArrayList<Track>();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                Track track = new Track();
                track.setRemoteId("local_"+cursor.getInt(cursor
                        .getColumnIndex(MediaStore.Audio.Media._ID))+"");
                track.setArtist(cursor.getString(cursor
                        .getColumnIndex(MediaStore.Audio.Media.ARTIST)));
                track.setTitle(cursor.getString(cursor
                        .getColumnIndex(MediaStore.Audio.Media.TITLE)));
                track.setPath(cursor.getString(cursor
                        .getColumnIndex(MediaStore.Audio.Media.DATA)));
                track.setAlbumId(Integer.parseInt(cursor.getString(cursor
                        .getColumnIndex(MediaStore.Audio.Media.ALBUM_ID))));
                track.setDuration(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)));

                Cursor cursorAlbum = context.getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                        new String[] {MediaStore.Audio.Albums._ID, MediaStore.Audio.Albums.ALBUM_ART},
                        MediaStore.Audio.Albums._ID+ "=?",
                        new String[] {String.valueOf(track.getAlbumId())},
                        null);

                if (cursorAlbum.moveToFirst()) {
                    String path = cursorAlbum.getString(cursorAlbum.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART));
                    track.setThumb(path);
                    // do whatever you need to do
                }
                track.setLocal(true);


                trackList.add(track);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return trackList;
    }

}


