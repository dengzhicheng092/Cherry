package lrandomdev.com.online.mp3player.fragments.locals;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.List;

import io.objectbox.Box;
import io.objectbox.BoxStore;
import lrandomdev.com.online.mp3player.MainApplication;
import lrandomdev.com.online.mp3player.R;
import lrandomdev.com.online.mp3player.adapters.AdapterMyPlaylist;
import lrandomdev.com.online.mp3player.adapters.AdapterPlaylist;
import lrandomdev.com.online.mp3player.fragments.FragmentHome;
import lrandomdev.com.online.mp3player.fragments.FragmentParent;
import lrandomdev.com.online.mp3player.models.MyPlaylist;

/**
 * Created by Lrandom on 3/29/18.
 */

public class FragmentMyPlaylist extends FragmentParent {
    FloatingActionButton fab;
    ArrayList<MyPlaylist> playlists = new ArrayList<MyPlaylist>();
    RecyclerView recyclerView;
    AdapterMyPlaylist mAdapter;
    int resources;
    BoxStore boxStore;

    private FragmentHome.OnFragmentInteractionListener mListener;

    public static final FragmentMyPlaylist newInstance() {
        FragmentMyPlaylist fragment = new FragmentMyPlaylist();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_playlists,
                container, false);
        fab = (FloatingActionButton) view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText edtName = new EditText(getContext());
                new AlertDialog.Builder(getContext())
                        .setTitle(getString(R.string.create))
                        .setView(edtName)
                        .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                boxStore = MainApplication.getApp().getBoxStore();
                                Box<MyPlaylist> playlistBox = boxStore.boxFor(MyPlaylist.class);
                                String name = edtName.getText().toString();
                                MyPlaylist playlist = new MyPlaylist();
                                playlist.setName(name);
                                playlist.setThumbLocal(true);
                                playlistBox.put(playlist);
                                playlists.add(playlist);
                                updateTotalTrack(playlist);
                            }
                        })
                        .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                            }
                        })
                        .show();
            }
        });

        recyclerView = (RecyclerView) view.findViewById(R.id.lvPlaylist);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 2);
        recyclerView.setLayoutManager(gridLayoutManager);
        this.resources = R.layout.row_playlist_item_grid;
        recyclerView.setHasFixedSize(true);
        playlists = new ArrayList<MyPlaylist>();

        FragmentLibrary fragmentLibrary = (FragmentLibrary) getParentFragment();
        mAdapter = new AdapterMyPlaylist(getContext(), playlists, this.resources, fragmentLibrary);
        mAdapter.setOnItemClickListener(new AdapterPlaylist.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                mListener.goToTrackInLocalPlaylist((int)playlists.get(position).getId());
            }
        });

        recyclerView.setAdapter(mAdapter);
        return view;
    }


    @Override
    public void onResume() {
        super.onResume();
        playlists.clear();
        boxStore = MainApplication.getApp().getBoxStore();
        Box<MyPlaylist> box = boxStore.boxFor(MyPlaylist.class);
        List<MyPlaylist> tmpList = box.getAll();
        for (int i = 0; i < tmpList.size(); i++) {
            playlists.add(tmpList.get(i));
        }
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void setMenuVisibility(boolean menuVisible) {
        super.setMenuVisibility(menuVisible);
    }

    public void updateTotalTrack(MyPlaylist playlist) {
        for (int i = 0; i < playlists.size(); i++) {
            if (playlists.get(i).getId() == playlist.getId()) {
                playlists.get(i).setTotal_track(playlist.getTotal_track());
                break;
            }
        }
        mAdapter.notifyDataSetChanged();
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
