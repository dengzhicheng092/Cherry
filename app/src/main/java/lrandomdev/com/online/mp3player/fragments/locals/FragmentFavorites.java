package lrandomdev.com.online.mp3player.fragments.locals;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import io.objectbox.BoxStore;
import lrandomdev.com.online.mp3player.ActivityFavorites;
import lrandomdev.com.online.mp3player.ActivityHome;
import lrandomdev.com.online.mp3player.MainApplication;
import lrandomdev.com.online.mp3player.R;
import lrandomdev.com.online.mp3player.adapters.AdapterTrack;
import lrandomdev.com.online.mp3player.fragments.FragmentParent;
import lrandomdev.com.online.mp3player.helpers.Helpers;
import lrandomdev.com.online.mp3player.models.Track;
import lrandomdev.com.online.mp3player.services.ServicePlayer;

public class FragmentFavorites extends FragmentParent {
    BoxStore boxStore;
    RecyclerView recyclerView;
    int resources;
    ArrayList<Track> tracks;
    AdapterTrack mAdapter;

    public static FragmentFavorites newInstance() {
        FragmentFavorites fragment = new FragmentFavorites();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_local_recyclerview, container, false);
        boxStore= MainApplication.getApp().getBoxStore();
        recyclerView= (RecyclerView)view.findViewById(R.id.lv);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        Boolean isGrid = prefs.getBoolean("track_grid", true);
        if(isGrid){
            GridLayoutManager gridLayoutManager=new GridLayoutManager(getActivity(),2);
            recyclerView.setLayoutManager(gridLayoutManager);
            this.resources=R.layout.row_track_item_grid;
        }else{
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
            recyclerView.setLayoutManager(linearLayoutManager);
            this.resources=R.layout.row_track_item;
        }

        tracks = Helpers.getFavoritesList(boxStore);
        recyclerView.setHasFixedSize(true);
        mAdapter = new AdapterTrack(getActivity(), tracks, this.resources);
        mAdapter.setOnItemClickListener(new AdapterTrack.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Intent intent = new Intent(ActivityHome.ON_TRACK_CLICK_PLAY);
                Bundle bundle = new Bundle();
                bundle.putInt("TRACK_INDEX",position);
                bundle.putSerializable("TRACKS",tracks);
                intent.putExtra("ON_TRACK_CLICK_ITEM",bundle);
                getActivity().sendBroadcast(intent);
            }
        });
        recyclerView.setAdapter(mAdapter);
        return view;
    }
}
