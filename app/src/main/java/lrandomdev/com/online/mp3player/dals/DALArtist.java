package lrandomdev.com.online.mp3player.dals;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.MediaStore;

import java.util.ArrayList;

import lrandomdev.com.online.mp3player.models.Artist;

public class DALArtist {
    Context context;
    ContentResolver contentResolver;

    public DALArtist(Context context) {
        // TODO Auto-generated constructor stub
        super();
        this.context = context;
        contentResolver = context.getContentResolver();
    }


    public ArrayList<Artist> getArtistsOnMDS() {
        Uri uri = MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI;
        String[] projection = new String[] { MediaStore.Audio.Artists._ID,
                MediaStore.Audio.Artists.ARTIST, MediaStore.Audio.Artists.NUMBER_OF_ALBUMS,MediaStore.Audio.Artists.NUMBER_OF_TRACKS };
        Cursor cursor = contentResolver.query(uri, projection, null, null, null);
        return convertMediaStoreCursorToArrayList(cursor);
    }

    public ArrayList<Artist> getArtistsByNameOnMDS(String name) {
        Uri uri = MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI;
        String[] projection = new String[] { MediaStore.Audio.Artists._ID,
                MediaStore.Audio.Artists.ARTIST,MediaStore.Audio.Artists.NUMBER_OF_ALBUMS,MediaStore.Audio.Artists.NUMBER_OF_TRACKS };
        Cursor cursor = contentResolver.query(uri, projection,
                MediaStore.Audio.Artists.ARTIST + " LIKE ?", new String[] { "%"
                        + name + "%" }, null);
        return convertMediaStoreCursorToArrayList(cursor);
    }

    private ArrayList<Artist> convertMediaStoreCursorToArrayList(Cursor cursor) {
        ArrayList<Artist> artistList = new ArrayList<Artist>();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                Artist artist = new Artist();
                artist.setId(cursor.getInt(cursor
                        .getColumnIndex(MediaStore.Audio.Artists._ID)));
                artist.setArtist(cursor.getString(cursor
                        .getColumnIndex(MediaStore.Audio.Artists.ARTIST)));
                artistList.add(artist);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return artistList;
    }
}
