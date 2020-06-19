package lrandomdev.com.online.mp3player.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;

import lrandomdev.com.online.mp3player.ActivityHome;
import lrandomdev.com.online.mp3player.R;
import lrandomdev.com.online.mp3player.adapters.AdapterAlbum;
import lrandomdev.com.online.mp3player.adapters.AdapterArtist;
import lrandomdev.com.online.mp3player.adapters.AdapterPlaylist;
import lrandomdev.com.online.mp3player.adapters.AdapterTrack;
import lrandomdev.com.online.mp3player.helpers.ApiServices;
import lrandomdev.com.online.mp3player.helpers.RestClient;
import lrandomdev.com.online.mp3player.models.Album;
import lrandomdev.com.online.mp3player.models.Artist;
import lrandomdev.com.online.mp3player.models.Playlist;
import lrandomdev.com.online.mp3player.models.Track;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragmentRealSearch extends FragmentParent {
    EditText edtSearch;
    String query;

    private FragmentHome.OnFragmentInteractionListener mListener;


    RecyclerView rcAlbums,rcArtists,rcTrack,rcPlaylists;
    Button btnMoreAlbums,btnMoreArtists,btnMorePlaylists, btnMoreTracks;
    TextView tvTracks,tvAlbums,tvPlaylists,tvArtists;
    AdapterAlbum adapterAlbum;
    AdapterArtist adapterArtist;
    AdapterPlaylist adapterPlaylist;
    AdapterTrack adapterTrack;

    ImageButton btnBack;

    ArrayList<Album> albums = new ArrayList<>();
    ArrayList<Artist> artists = new ArrayList<>();
    ArrayList<Playlist> playlists = new ArrayList<>();
    ArrayList<Track> tracks = new ArrayList<>();

    ApiServices apiServices;

    public static final FragmentRealSearch newInstance() {
        FragmentRealSearch fragment = new FragmentRealSearch();
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_real_seach, container, false);
        edtSearch=(EditText)view.findViewById(R.id.edtSearch);
        edtSearch.requestFocus();
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                query = edtSearch.getText().toString();
                loadTrack();
                loadAlbums();
                loadArtists();
                loadPlaylist();
            }
        });

        btnMoreAlbums=(Button)view.findViewById(R.id.btnMoreAlbum);
        btnMoreTracks=(Button)view.findViewById(R.id.btnMoreTrack);
        btnMoreArtists=(Button)view.findViewById(R.id.btnMoreArtist);
        btnMorePlaylists=(Button)view.findViewById(R.id.btnMorePlaylists);
        btnBack=(ImageButton) view.findViewById(R.id.btnBack);

        btnMoreTracks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.goToTrack(query,false);
            }
        });

        btnMoreAlbums.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.goToAlbum(query);
            }
        });

        btnMoreArtists.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.goToArtist(query);
            }
        });

        btnMorePlaylists.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.goToPlaylist(query);
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.pop();
            }
        });


        tvTracks=(TextView)view.findViewById(R.id.tvTracks);
        tvAlbums=(TextView)view.findViewById(R.id.tvAlbums);
        tvArtists=(TextView)view.findViewById(R.id.tvArtists);
        tvPlaylists=(TextView)view.findViewById(R.id.tvPlaylists);

        rcAlbums= (RecyclerView) view.findViewById(R.id.rcAlbums);
        rcArtists=(RecyclerView)view.findViewById(R.id.rcArtists);
        rcPlaylists=(RecyclerView)view.findViewById(R.id.rcPlaylists);
        rcTrack=(RecyclerView)view.findViewById(R.id.rcTracks);
        apiServices= RestClient.getApiService();
        return view;
    }

    void loadAlbums(){
        albums.clear();
        adapterAlbum=new AdapterAlbum(getActivity(),albums,R.layout.row_album_item);
        rcAlbums.setLayoutManager(new LinearLayoutManager(getActivity(),LinearLayoutManager.VERTICAL,false));
        rcAlbums.setHasFixedSize(true);
        rcAlbums.setAdapter(adapterAlbum);
        Call<ArrayList<Album>> call= apiServices.getAlbums(0,10,query);
        call.enqueue(new Callback<ArrayList<Album>>() {
            @Override
            public void onResponse(Call<ArrayList<Album>> call, Response<ArrayList<Album>> response) {
                for (Album album:response.body()
                ) {
                    albums.add(album);
                }
                adapterAlbum.notifyDataSetChanged();
                tvAlbums.setVisibility(View.VISIBLE);
                btnMoreAlbums.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFailure(Call<ArrayList<Album>> call, Throwable t) {
                tvAlbums.setVisibility(View.GONE);
                btnMoreAlbums.setVisibility(View.GONE);
            }
        });
        adapterAlbum.setOnItemClickListener(new AdapterAlbum.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                //mListener.goToTrackInAlbum(albums.get(position).getId(),true);
            }
        });
    }

    void loadArtists(){
        artists.clear();
        adapterArtist=new AdapterArtist(getActivity(),artists,R.layout.row_artist_item);
        rcArtists.setLayoutManager(new LinearLayoutManager(getActivity(),LinearLayoutManager.VERTICAL,false));
        rcArtists.setHasFixedSize(true);
        rcArtists.setAdapter(adapterArtist);
        Call<ArrayList<Artist>> call= apiServices.getArtists(0,10,query);
        call.enqueue(new Callback<ArrayList<Artist>>() {
            @Override
            public void onResponse(Call<ArrayList<Artist>> call, Response<ArrayList<Artist>> response) {
                for (Artist artist:response.body()
                ) {
                    artists.add(artist);
                }
                adapterArtist.notifyDataSetChanged();
                tvArtists.setVisibility(View.VISIBLE);
                btnMoreArtists.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFailure(Call<ArrayList<Artist>> call, Throwable t) {
                tvArtists.setVisibility(View.GONE);
                btnMoreArtists.setVisibility(View.GONE);
            }
        });
        adapterArtist.setOnItemClickListener(new AdapterArtist.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                //mListener.goToTrackInArtist(artists.get(position).getId(),true);
            }
        });
    }

    void loadPlaylist(){
        playlists.clear();
        adapterPlaylist = new AdapterPlaylist(getActivity(),playlists,R.layout.row_playlist_item);
        rcPlaylists.setLayoutManager(new LinearLayoutManager(getActivity(),LinearLayoutManager.VERTICAL,false));
        rcPlaylists.setHasFixedSize(true);
        rcPlaylists.setAdapter(adapterPlaylist);
        Call<ArrayList<Playlist>> call= apiServices.getPlaylists(0,10,query);
        call.enqueue(new Callback<ArrayList<Playlist>>() {
            @Override
            public void onResponse(Call<ArrayList<Playlist>> call, Response<ArrayList<Playlist>> response) {
                for (Playlist playlist:response.body()
                ) {
                    playlists.add(playlist);
                }
                adapterArtist.notifyDataSetChanged();
                tvPlaylists.setVisibility(View.VISIBLE);
                btnMorePlaylists.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFailure(Call<ArrayList<Playlist>> call, Throwable t) {
                tvPlaylists.setVisibility(View.GONE);
                btnMorePlaylists.setVisibility(View.GONE);
            }
        });
        adapterPlaylist.setOnItemClickListener(new AdapterPlaylist.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                //mListener.goToTrackInPlaylist((int)playlists.get(position).getId(),true);
            }
        });
    }

    void loadTrack(){
        tracks.clear();
        adapterTrack = new AdapterTrack(getActivity(),tracks,R.layout.row_track_item);
        rcTrack.setLayoutManager(new LinearLayoutManager(getActivity(),LinearLayoutManager.VERTICAL,false){
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        });
        rcTrack.setHasFixedSize(true);
        rcTrack.setAdapter(adapterTrack);
        Call<ArrayList<Track>> call= apiServices.getTracks(0,10,query);
        call.enqueue(new Callback<ArrayList<Track>>() {
            @Override
            public void onResponse(Call<ArrayList<Track>> call, Response<ArrayList<Track>> response) {
                for (Track track:response.body()
                ) {
                    tracks.add(track);
                }
                adapterTrack.notifyDataSetChanged();
                tvTracks.setVisibility(View.VISIBLE);
                btnMoreTracks.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFailure(Call<ArrayList<Track>> call, Throwable t) {
                tvTracks.setVisibility(View.GONE);
                btnMoreTracks.setVisibility(View.GONE);
            }
        });

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