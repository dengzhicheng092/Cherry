package lrandomdev.com.online.mp3player.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import java.util.ArrayList;

import lrandomdev.com.online.mp3player.ActivityHome;
import lrandomdev.com.online.mp3player.R;
import lrandomdev.com.online.mp3player.fragments.locals.FragmentLibrary;
import lrandomdev.com.online.mp3player.fragments.dialogs.FragmentSelectPlaylistDialog;
import lrandomdev.com.online.mp3player.helpers.Helpers;
import lrandomdev.com.online.mp3player.models.Track;
import lrandomdev.com.online.mp3player.services.DownloadService;


/**
 * Created by Lrandom on 4/8/18.
 */

public class AdapterTrackInPlaylist extends AdapterTrack {

    int playlistId;

    public AdapterTrackInPlaylist(Context context, ArrayList<Track> tracks, int resources, int playlistId) {
        super(context, tracks, resources);
        this.playlistId = playlistId;
    }


    @Override
    public void showMenu(final View view) {
        PopupMenu menu = new PopupMenu(getContext(), view);
        menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                final Track track = (Track) view.getTag();
                int position=(int)view.getTag(R.id.tvTitle);
                switch (id) {
                    case R.id.item_play:
                        Intent intent = new Intent(ActivityHome.ON_TRACK_CLICK_PLAY);
                        Bundle bundle = new Bundle();
                        bundle.putInt("TRACK_INDEX", position);
                        bundle.putSerializable("TRACKS", getTracks());
                        intent.putExtra("ON_TRACK_CLICK_ITEM", bundle);
                        getContext().sendBroadcast(intent);
                        break;

                    case R.id.item_add_to_queue:
                        intent = new Intent(ActivityHome.ON_TRACK_CLICK_ADD_TO_QUEUE);
                        intent.putExtra("TRACK", track);
                        getContext().sendBroadcast(intent);
                        Toast.makeText(getContext(), track.getTitle() + " " + getContext().getString(R.string.has_been_add_to_queue), Toast.LENGTH_LONG).show();
                        break;

                    case R.id.item_download:
                        intent = new Intent(getContext(), DownloadService.class);
                        intent.putExtra("file", track);
                        getContext().startService(intent);
                        //Toast.makeText(getContext(),"This not avaiable in demo",Toast.LENGTH_SHORT).show();
                        break;

                    case R.id.item_to_playlist:
                        FragmentSelectPlaylistDialog newFragment = FragmentSelectPlaylistDialog
                                .newInstance();
                        bundle = new Bundle();
                        bundle.putSerializable("item", track);
                        newFragment.setArguments(bundle);
                        Activity activity = (Activity) getContext();
                        newFragment.show(activity.getFragmentManager(), "dialog");
                        break;

                    case R.id.item_share:
                        Helpers.shareAction(getContext(), track);
                        break;

                    case R.id.item_remove_from_playlist:
                        Helpers.removeTrackInPlaylist(boxStore, track, playlistId);
                        getTracks().remove(track);
                        notifyDataSetChanged();
                        break;
                }
                return true;
            }
        });
        menu.inflate(R.menu.menu_track_in_playlist);
        menu.show();

        SharedPreferences prefs = getContext().getSharedPreferences("allow_download",Context.MODE_PRIVATE);
        int isAllow= prefs.getInt("is_allow",0);
        if(isAllow==0){
            menu.getMenu().findItem(R.id.item_download).setVisible(false);
        }else{
            menu.getMenu().findItem(R.id.item_download).setVisible(true);
        }
    }
}
