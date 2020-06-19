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
import lrandomdev.com.online.mp3player.adapters.AdapterAlbum;
import lrandomdev.com.online.mp3player.dals.DALAlbum;
import lrandomdev.com.online.mp3player.fragments.FragmentHome;
import lrandomdev.com.online.mp3player.fragments.FragmentParent;
import lrandomdev.com.online.mp3player.models.Album;

public class FragmentAlbum extends FragmentParent {
    ArrayList<Album> albums;
    private FragmentHome.OnFragmentInteractionListener mListener;

    public static final FragmentAlbum newInstance() {
        FragmentAlbum fragment = new FragmentAlbum();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_local_recyclerview,null,false);
        DALAlbum dalAlbum= new DALAlbum(getActivity());
        albums= dalAlbum.getAlbumOnMDS();
        RecyclerView recyclerView= (RecyclerView)view.findViewById(R.id.lv);
            GridLayoutManager gridLayoutManager=new GridLayoutManager(getActivity(),2);
            recyclerView.setLayoutManager(gridLayoutManager);

        AdapterAlbum mAdapter = new AdapterAlbum(getActivity(),albums,R.layout.row_album_item_grid);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(new AdapterAlbum.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                mListener.goToTrackInLocalAlbum(albums.get(position).getId(),1, albums.get(position).getTitle());
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

