package lrandomdev.com.online.mp3player.fragments.locals;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import lrandomdev.com.online.mp3player.R;
import lrandomdev.com.online.mp3player.adapters.AdapterArtist;
import lrandomdev.com.online.mp3player.dals.DALArtist;
import lrandomdev.com.online.mp3player.fragments.FragmentHome;
import lrandomdev.com.online.mp3player.fragments.FragmentParent;
import lrandomdev.com.online.mp3player.models.Artist;

public class FragmentArtist extends FragmentParent {
    ArrayList<Artist> artists;
    private FragmentHome.OnFragmentInteractionListener mListener;

    public static final FragmentArtist newInstance() {
        FragmentArtist fragment = new FragmentArtist();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_local_recyclerview,null,false);
        DALArtist dalArtist= new DALArtist(getActivity());
        artists= dalArtist.getArtistsOnMDS();
        RecyclerView recyclerView= (RecyclerView)view.findViewById(R.id.lv);
        GridLayoutManager layoutManager=new GridLayoutManager(getActivity(),2);
        recyclerView.setLayoutManager(layoutManager);
        AdapterArtist mAdapter = new AdapterArtist(getActivity(),artists,R.layout.row_artist_item_grid);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(new AdapterArtist.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                mListener.goToTrackInLocalArtist(artists.get(position).getId(),2,artists.get(position).getArtist());
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


