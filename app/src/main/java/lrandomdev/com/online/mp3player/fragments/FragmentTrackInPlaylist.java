package lrandomdev.com.online.mp3player.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
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
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;

import jp.wasabeef.glide.transformations.BlurTransformation;
import lrandomdev.com.online.mp3player.ActivityHome;
import lrandomdev.com.online.mp3player.R;
import lrandomdev.com.online.mp3player.adapters.AdapterTrack;
import lrandomdev.com.online.mp3player.helpers.ApiServices;
import lrandomdev.com.online.mp3player.helpers.Helpers;
import lrandomdev.com.online.mp3player.helpers.RestClient;
import lrandomdev.com.online.mp3player.models.Playlist;
import lrandomdev.com.online.mp3player.models.Track;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragmentTrackInPlaylist extends Fragment {
    private static final String ID = "id";

    TextView tvTitle,tvTrackCounter;
    RecyclerView rcPopularSong;
    int playlistId;
    ApiServices apiServices;
    AdapterTrack adapterTrack;
    ArrayList<Track> tracks=new ArrayList<>();
    ImageView imgCover,imgSmallThumb;
    AppBarLayout appBarLayout;
    CollapsingToolbarLayout htab_collapse_toolbar;
    Toolbar toolbar;
    TextView btnTag;
    FloatingActionButton fab;

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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_track_in_playlist, container, false);
        tvTitle=(TextView)view.findViewById(R.id.tvTitle);
        tvTrackCounter=(TextView)view.findViewById(R.id.tvTrackCounter);
        imgCover=(ImageView)view.findViewById(R.id.imgCover);
        imgSmallThumb=(ImageView)view.findViewById(R.id.imgSmallThumb);
        appBarLayout=(AppBarLayout)view.findViewById(R.id.appbar_layout);
        htab_collapse_toolbar=(CollapsingToolbarLayout)view.findViewById(R.id.htab_collapse_toolbar);
        tvTrackCounter.setText("0 "+getResources().getText(R.string.tracks));
        toolbar=(Toolbar)view.findViewById(R.id.toolbar);
        btnTag=(TextView)view.findViewById(R.id.btnTag);
        toolbar.setNavigationIcon(R.drawable.ic_back_white);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.pop();
            }
        });

        rcPopularSong=(RecyclerView)view.findViewById(R.id.rcPopularTracks);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(),LinearLayoutManager.VERTICAL,false);
        rcPopularSong.setLayoutManager(linearLayoutManager);

        apiServices= RestClient.getApiService();
        loadInfo();
        loadTrack();

        fab = (FloatingActionButton) view.findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tracks==null || tracks.size() == 0) {
                    Toast ts = Toast.makeText(
                            getActivity(), getString(R.string.do_not_have_any_song_to_play), Toast.LENGTH_SHORT);
                    ts.show();
                } else {
                    Intent intent = new Intent(ActivityHome.ON_TRACK_CLICK_PLAY);
                    Bundle bundle = new Bundle();
                    bundle.putInt("TRACK_INDEX",0);
                    bundle.putSerializable("TRACKS",tracks);
                    intent.putExtra("ON_TRACK_CLICK_ITEM",bundle);
                    getActivity().sendBroadcast(intent);
                }
            }
        });
        return view;
    }

    void loadInfo(){
        Call<Playlist> call = apiServices.getPlaylist(playlistId);
        call.enqueue(new Callback<Playlist>() {
            @Override
            public void onResponse(Call<Playlist> call, Response<Playlist> response) {
                final Playlist artist= response.body();
                if(artist!=null) {
                    tvTitle.setText(artist.getName());
                    int placeholder = R.drawable.bg;

                    Glide.with(getActivity()).load(artist.getThumb())
                            .apply(RequestOptions.bitmapTransform(new BlurTransformation(5, 2)).error(R.drawable.bg).placeholder(R.drawable.bg))
                            .into(imgCover);

                    Glide.with(getActivity()).load(artist.getThumb())
                            .apply(new RequestOptions().placeholder(placeholder).error(placeholder)).into(imgSmallThumb);


                    appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
                        @Override
                        public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {

                            if (Math.abs(verticalOffset) - appBarLayout.getTotalScrollRange() == 0) {
                                //  Collapsed
                                htab_collapse_toolbar.setTitle(artist.getName());
                                btnTag.setVisibility(View.GONE);
                            } else {
                                //Expanded
                                htab_collapse_toolbar.setTitle("");
                                btnTag.setVisibility(View.VISIBLE);
                            }
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<Playlist> call, Throwable t) {
                Log.e("activity_artist",t.getMessage());
            }
        });
    }

    void loadTrack(){
        tracks.clear();
        adapterTrack=new AdapterTrack(getActivity(),tracks,R.layout.row_track_item);
        rcPopularSong.setAdapter(adapterTrack);
        adapterTrack.setOnItemClickListener(new AdapterTrack.OnItemClickListener() {
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
        Call<ArrayList<Track>> call= apiServices.getTracks(0,100,null,null, null,playlistId+"");
        call.enqueue(new Callback<ArrayList<Track>>() {
            @Override
            public void onResponse(Call<ArrayList<Track>> call, Response<ArrayList<Track>> response) {
                for (Track track: response.body()
                ) {
                    tracks.add(track);
                }
                adapterTrack.notifyDataSetChanged();
                tvTrackCounter.setText(response.body().size()+" "+getResources().getString(R.string.tracks));
                Helpers.loadInAd(getActivity());
            }

            @Override
            public void onFailure(Call<ArrayList<Track>> call, Throwable t) {

            }
        });
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
