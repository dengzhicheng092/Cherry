package lrandomdev.com.online.mp3player.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
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
import lrandomdev.com.online.mp3player.adapters.AdapterAlbum;
import lrandomdev.com.online.mp3player.adapters.AdapterTrack;
import lrandomdev.com.online.mp3player.fragments.locals.FragmentLibrary;
import lrandomdev.com.online.mp3player.helpers.ApiServices;
import lrandomdev.com.online.mp3player.helpers.Helpers;
import lrandomdev.com.online.mp3player.helpers.RestClient;
import lrandomdev.com.online.mp3player.models.Album;
import lrandomdev.com.online.mp3player.models.Artist;
import lrandomdev.com.online.mp3player.models.Track;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class FragmentTrackInArtist extends Fragment {
    private static final String ID = "id";

    TextView tvTitle;
    RecyclerView rcPopularSong, rcAlbums;
    int artistId;
    ApiServices apiServices;
    AdapterTrack adapterTrack;
    AdapterAlbum adapterAlbum;
    ArrayList<Track> tracks=new ArrayList<>();
    ArrayList<Album> albums=new ArrayList<>();
    ImageView imgCover,imgSmallThumb;
    AppBarLayout appBarLayout;
    CollapsingToolbarLayout htab_collapse_toolbar;
    Toolbar toolbar;
    TextView btnTag;
    FloatingActionButton fab;

    private FragmentHome.OnFragmentInteractionListener mListener;

    // TODO: Rename and change types and number of parameters
    public static FragmentTrackInArtist newInstance(int id) {
        FragmentTrackInArtist fragment = new FragmentTrackInArtist();
        Bundle args = new Bundle();
        args.putInt(ID, id);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
           artistId = getArguments().getInt(ID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view =inflater.inflate(R.layout.fragment_track_in_artist, container, false);
        tvTitle=(TextView)view.findViewById(R.id.tvTitle);
        imgCover=(ImageView)view.findViewById(R.id.imgCover);
        imgSmallThumb=(ImageView)view.findViewById(R.id.imgSmallThumb);
        appBarLayout=(AppBarLayout)view.findViewById(R.id.appbar_layout);
        htab_collapse_toolbar=(CollapsingToolbarLayout)view.findViewById(R.id.htab_collapse_toolbar);
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

        rcAlbums=(RecyclerView)view.findViewById(R.id.rcAlbums);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(),2);
        rcAlbums.setLayoutManager(gridLayoutManager);

        apiServices= RestClient.getApiService();
        loadInfo();
        loadTrack();
        loadAlbum();

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
        Call<Artist> call = apiServices.getArtist(artistId);
        call.enqueue(new Callback<Artist>() {
            @Override
            public void onResponse(Call<Artist> call, Response<Artist> response) {
                final Artist artist= response.body();
                if(artist!=null) {
                    tvTitle.setText(artist.getArtist());
                    //tvTrackCounter.setText(ar));
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
                                htab_collapse_toolbar.setTitle(artist.getArtist());
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
            public void onFailure(Call<Artist> call, Throwable t) {
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
        Call<ArrayList<Track>> call= apiServices.getTracks(0,100,null,null, artistId+"",null);
        call.enqueue(new Callback<ArrayList<Track>>() {
            @Override
            public void onResponse(Call<ArrayList<Track>> call, Response<ArrayList<Track>> response) {
                for (Track track: response.body()
                ) {
                    tracks.add(track);
                }
                adapterTrack.notifyDataSetChanged();
                Helpers.loadInAd(getActivity());
            }

            @Override
            public void onFailure(Call<ArrayList<Track>> call, Throwable t) {

            }
        });
    }

    void loadAlbum(){
        albums.clear();
        adapterAlbum=new AdapterAlbum(getActivity(),albums,R.layout.row_album_item_grid);
        rcAlbums.setAdapter(adapterAlbum);
        adapterAlbum.setOnItemClickListener(new AdapterAlbum.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                mListener.goToTrackInAlbum(albums.get(position).getId(),true);
            }
        });
        Call<ArrayList<Album>> call = apiServices.getAlbums(0,100, artistId);
        call.enqueue(new Callback<ArrayList<Album>>() {
            @Override
            public void onResponse(Call<ArrayList<Album>> call, Response<ArrayList<Album>> response) {
                for (Album album: response.body()
                ) {
                    albums.add(album);
                }
                adapterAlbum.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Call<ArrayList<Album>> call, Throwable t) {

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
