package lrandomdev.com.online.mp3player.fragments.locals;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.List;

import io.objectbox.Box;
import io.objectbox.BoxStore;
import jp.wasabeef.glide.transformations.BlurTransformation;
import lrandomdev.com.online.mp3player.ActivityHome;
import lrandomdev.com.online.mp3player.MainApplication;
import lrandomdev.com.online.mp3player.R;
import lrandomdev.com.online.mp3player.adapters.AdapterTrackInPlaylist;
import lrandomdev.com.online.mp3player.fragments.FragmentHome;
import lrandomdev.com.online.mp3player.fragments.FragmentParent;
import lrandomdev.com.online.mp3player.helpers.RestClient;
import lrandomdev.com.online.mp3player.models.MyPlaylist;
import lrandomdev.com.online.mp3player.models.MyPlaylist_;
import lrandomdev.com.online.mp3player.models.Track;
import lrandomdev.com.online.mp3player.models.Track_;

public class FragmentTrackInPlaylist extends FragmentParent {
    private static final String ID = "id";
    TextView tvTitle, tvTrackCounter;
    RecyclerView rcPopularSong;
    int playlistId;
    ArrayList<Track> tracks = new ArrayList<>();
    ImageView imgCover, imgSmallThumb;
    AppBarLayout appBarLayout;
    CollapsingToolbarLayout htab_collapse_toolbar;
    Toolbar toolbar;
    TextView btnTag;
    BoxStore boxStore = MainApplication.getApp().getBoxStore();

    private FragmentHome.OnFragmentInteractionListener mListener;


    public static FragmentTrackInPlaylist newInstance(int id) {
        FragmentTrackInPlaylist fragment = new FragmentTrackInPlaylist();
        Bundle args = new Bundle();
        args.putInt(ID, id);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            playlistId = getArguments().getInt(ID);
        }

        boxStore = MainApplication.getApp().getBoxStore();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_track_in_playlist, container, false);
        tvTitle = (TextView) view.findViewById(R.id.tvTitle);
        tvTrackCounter = (TextView) view.findViewById(R.id.tvTrackCounter);

        imgCover = (ImageView) view.findViewById(R.id.imgCover);
        imgSmallThumb = (ImageView) view.findViewById(R.id.imgSmallThumb);

        appBarLayout = (AppBarLayout) view.findViewById(R.id.appbar_layout);
        htab_collapse_toolbar = (CollapsingToolbarLayout) view.findViewById(R.id.htab_collapse_toolbar);

        tvTrackCounter.setText("0 " + getResources().getText(R.string.tracks));
        toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        btnTag = (TextView) view.findViewById(R.id.btnTag);
        toolbar.setNavigationIcon(R.drawable.ic_back_white);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.pop();
            }
        });

        rcPopularSong = (RecyclerView) view.findViewById(R.id.rcPopularTracks);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        rcPopularSong.setLayoutManager(linearLayoutManager);

        loadInfo();
        loadTrack();
        return view;
    }

    void loadInfo() {
        Box<MyPlaylist> box = boxStore.boxFor(MyPlaylist.class);
        final MyPlaylist myPlaylist = box.query().equal(MyPlaylist_.id,playlistId).build().findFirst();

        tvTitle.setText(myPlaylist.getName());
        int placeholder = R.drawable.bg;

        if(myPlaylist.getThumbLocal()) {
            Glide.with(getActivity()).load(myPlaylist.getThumb())
                    .apply(RequestOptions.bitmapTransform(new BlurTransformation(5, 2)).error(R.drawable.bg).placeholder(R.drawable.bg))
                    .into(imgCover);

            Glide.with(getActivity()).load(myPlaylist.getThumb())
                    .apply(new RequestOptions().placeholder(placeholder).error(placeholder)).into(imgSmallThumb);
        }else{
            Glide.with(getActivity()).load(RestClient.BASE_URL+myPlaylist.getThumb())
                    .apply(RequestOptions.bitmapTransform(new BlurTransformation(5, 2)).error(R.drawable.bg).placeholder(R.drawable.bg))
                    .into(imgCover);

            Glide.with(getActivity()).load(RestClient.BASE_URL+myPlaylist.getThumb())
                    .apply(new RequestOptions().placeholder(placeholder).error(placeholder)).into(imgSmallThumb);
        }


        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (Math.abs(verticalOffset) - appBarLayout.getTotalScrollRange() == 0) {
                    //  Collapsed
                    htab_collapse_toolbar.setTitle(myPlaylist.getName());
                    btnTag.setVisibility(View.GONE);
                } else {
                    //Expanded
                    htab_collapse_toolbar.setTitle("");
                    btnTag.setVisibility(View.VISIBLE);
                }
            }
        });

    }

    void loadTrack() {
        Box<Track> box = boxStore.boxFor(Track.class);
        tracks = new ArrayList<>(box.query().equal(Track_.playlistId, playlistId).build().find());
        AdapterTrackInPlaylist mAdapter = new AdapterTrackInPlaylist(getActivity(),
                tracks, R.layout.row_track_item,playlistId);
        mAdapter.setOnItemClickListener(new AdapterTrackInPlaylist.OnItemClickListener() {
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
        rcPopularSong.setAdapter(mAdapter);
        tvTrackCounter.setText(tracks.size()+" "+getString(R.string.tracks));
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
