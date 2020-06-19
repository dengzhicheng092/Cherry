package lrandomdev.com.online.mp3player.fragments.locals;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import lrandomdev.com.online.mp3player.ActivityHome;
import lrandomdev.com.online.mp3player.R;
import lrandomdev.com.online.mp3player.adapters.AdapterLocalTrack;
import lrandomdev.com.online.mp3player.adapters.AdapterTrack;
import lrandomdev.com.online.mp3player.dals.DALTrack;
import lrandomdev.com.online.mp3player.fragments.FragmentHome;
import lrandomdev.com.online.mp3player.fragments.FragmentParent;
import lrandomdev.com.online.mp3player.models.Track;

public class FragmentTrack extends FragmentParent {
    int resources;
    ArrayList<Track> tracks;
    Toolbar toolbar;
    private FragmentHome.OnFragmentInteractionListener mListener;

    public static final FragmentTrack newInstance() {
        FragmentTrack fragment = new FragmentTrack();
        return fragment;
    }

    public static final FragmentTrack newInstance(int type, int id, String name) {
        FragmentTrack fragment = new FragmentTrack();
        Bundle bundle = new Bundle();
        bundle.putInt("type", type);
        bundle.putInt("id", id);
        bundle.putString("name", name);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_local_tracks, container, false);
        DALTrack dalTrack = new DALTrack(getActivity());
        Toolbar toolbar = (Toolbar)view.findViewById(R.id.toolbar);
        Bundle bundle = getArguments();
        if (bundle!=null && bundle.containsKey("type")) {
            int type = bundle.getInt("type");
            switch (type) {
                case 1:
                    tracks = dalTrack.getTracksByAlbumIdOnMDS(bundle.getInt("id"));
                    toolbar.setTitle(getString(R.string.albums)+" "+bundle.getString("name"));
                    break;

                case 2:
                    tracks = dalTrack.getTracksByArtistIdOnMDS(bundle.getInt("id"));
                    toolbar.setTitle(getString(R.string.artists)+" "+bundle.getString("name"));
                    break;

                case 3:
                    //tracks=dalTrack.getTracksByArtistIdOnMDS(bundle.getInt("id"));
                    break;

                default:
                    tracks = dalTrack.getTracksOnMDS("DESC");
                    break;
            }
            toolbar.setNavigationIcon(R.drawable.ic_back_white);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mListener.pop();
                }
            });
        } else {
            toolbar.setVisibility(View.GONE);
            tracks = dalTrack.getTracksOnMDS("DESC");
        }

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.lv);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        Boolean isGrid = prefs.getBoolean("track_grid", true);
        if (isGrid) {
            GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 2);
            recyclerView.setLayoutManager(gridLayoutManager);
            this.resources = R.layout.row_track_item_grid;
        } else {
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
            recyclerView.setLayoutManager(linearLayoutManager);
            this.resources = R.layout.row_track_item;
        }
        AdapterLocalTrack adapterTrack = new AdapterLocalTrack(getActivity(), tracks, this.resources);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapterTrack);

        adapterTrack.setOnItemClickListener(new AdapterTrack.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Intent intent = new Intent(ActivityHome.ON_TRACK_CLICK_PLAY);
                Bundle bundle = new Bundle();
                bundle.putInt("TRACK_INDEX", position);
                bundle.putSerializable("TRACKS", tracks);
                intent.putExtra("ON_TRACK_CLICK_ITEM", bundle);
                getActivity().sendBroadcast(intent);
            }
        });
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof FragmentHome.OnFragmentInteractionListener) {
            mListener = (FragmentHome.OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
}
