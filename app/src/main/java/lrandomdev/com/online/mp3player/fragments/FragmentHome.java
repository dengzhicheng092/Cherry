package lrandomdev.com.online.mp3player.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.smarteist.autoimageslider.DefaultSliderView;
import com.smarteist.autoimageslider.IndicatorAnimations;
import com.smarteist.autoimageslider.SliderAnimations;
import com.smarteist.autoimageslider.SliderLayout;
import com.smarteist.autoimageslider.SliderView;

import java.util.ArrayList;

import lrandomdev.com.online.mp3player.ActivityHome;
import lrandomdev.com.online.mp3player.R;
import lrandomdev.com.online.mp3player.adapters.AdapterAlbum;
import lrandomdev.com.online.mp3player.adapters.AdapterArtist;
import lrandomdev.com.online.mp3player.adapters.AdapterCategories;
import lrandomdev.com.online.mp3player.adapters.AdapterPlaylist;
import lrandomdev.com.online.mp3player.adapters.AdapterTrack;
import lrandomdev.com.online.mp3player.helpers.ApiServices;
import lrandomdev.com.online.mp3player.helpers.Helpers;
import lrandomdev.com.online.mp3player.helpers.RestClient;
import lrandomdev.com.online.mp3player.models.Album;
import lrandomdev.com.online.mp3player.models.Artist;
import lrandomdev.com.online.mp3player.models.Categories;
import lrandomdev.com.online.mp3player.models.Playlist;
import lrandomdev.com.online.mp3player.models.Track;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragmentHome extends FragmentParent {



    private FragmentHome.OnFragmentInteractionListener mListener;

    RecyclerView rcTopAlbums,rcTopArtists,rcNewReleases,rcNewPlaylists,rcCategories;
    Button btnMoreAlbums,btnMoreArtists,btnMoreNew,btnMoreNewPlaylists, btnMoreCategory;
    AdapterAlbum adapterAlbum;
    AdapterArtist adapterArtist;
    AdapterPlaylist adapterPlaylist;
    AdapterTrack adapterTrack;
    AdapterCategories adapterCategories;

    ArrayList<Album> albums = new ArrayList<>();
    ArrayList<Artist> artists = new ArrayList<>();
    ArrayList<Playlist> playlists = new ArrayList<>();
    ArrayList<Track> tracks = new ArrayList<>();
    ArrayList<Categories> categories = new ArrayList<>();

    ApiServices apiServices;
    SliderLayout sliderLayout;

    public static final FragmentHome newInstance() {
        FragmentHome fragment = new FragmentHome();
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        apiServices=RestClient.getApiService();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home,
                container, false);
        rcTopAlbums= (RecyclerView) view.findViewById(R.id.rcTracks);
        rcTopArtists=(RecyclerView)view.findViewById(R.id.rcTopArtist);
        rcNewReleases=(RecyclerView)view.findViewById(R.id.rcNewReleases);
        rcNewPlaylists=(RecyclerView)view.findViewById(R.id.rcNewPlaylists);
        rcCategories=(RecyclerView)view.findViewById(R.id.rcCategory);

        btnMoreAlbums=(Button) view.findViewById(R.id.btnMoreAlbums);
        btnMoreArtists=(Button) view.findViewById(R.id.btnMoreArtists);
        btnMoreNew=(Button) view.findViewById(R.id.btnMoreNew);
        btnMoreNewPlaylists=(Button) view.findViewById(R.id.btnMoreNewPlaylists);
        btnMoreCategory = (Button) view.findViewById(R.id.btnMoreCategory);

        btnMoreAlbums.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.goToAlbum(null);
            }
        });

        btnMoreArtists.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
             mListener.goToArtist(null);
            }
        });

        btnMoreNewPlaylists.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.goToPlaylist(null);
            }
        });

        btnMoreCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.goToCategory();
            }
        });

        btnMoreNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.goToTrack(null,false);
            }
        });


//        sliderLayout = view.findViewById(R.id.imageSlider);
//        sliderLayout.setIndicatorAnimation(IndicatorAnimations.SWAP); //set indicator animation by using SliderLayout.IndicatorAnimations. :WORM or THIN_WORM or COLOR or DROP or FILL or NONE or SCALE or SCALE_DOWN or SLIDE and SWAP!!
//        sliderLayout.setSliderTransformAnimation(SliderAnimations.FADETRANSFORMATION);
//        sliderLayout.setScrollTimeInSec(5); //set scroll delay in seconds :
        //setSliderViews();

        loadAlbums();

        loadArtists();

        loadPlaylist();

        loadTrack();

        loadCategory();

        LinearLayout adView = (LinearLayout)view.findViewById(R.id.adView);
        Helpers.loadAd(getActivity(),adView);

        return view;
    }

    void loadAlbums(){
        albums.clear();
        adapterAlbum=new AdapterAlbum(getActivity(),albums,R.layout.row_home_album_item_grid);
        rcTopAlbums.setLayoutManager(new LinearLayoutManager(getActivity(),LinearLayoutManager.HORIZONTAL,false));
        rcTopAlbums.setHasFixedSize(true);
        rcTopAlbums.setAdapter(adapterAlbum);
        Call<ArrayList<Album>> call= apiServices.getAlbums(0,10,null);
        call.enqueue(new Callback<ArrayList<Album>>() {
            @Override
            public void onResponse(Call<ArrayList<Album>> call, Response<ArrayList<Album>> response) {
                for (Album album:response.body()
                        ) {
                    albums.add(album);
                }
                adapterAlbum.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Call<ArrayList<Album>> call, Throwable t) {
            }
        });
        adapterAlbum.setOnItemClickListener(new AdapterAlbum.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                mListener.goToTrackInAlbum(albums.get(position).getId(),true);
            }
        });
    }

    void loadArtists(){
        artists.clear();
        adapterArtist=new AdapterArtist(getActivity(),artists,R.layout.row_home_artist_item);
        rcTopArtists.setLayoutManager(new LinearLayoutManager(getActivity(),LinearLayoutManager.HORIZONTAL,false));
        rcTopArtists.setHasFixedSize(true);
        rcTopArtists.setAdapter(adapterArtist);
        Call<ArrayList<Artist>> call= apiServices.getArtists(0,10,null);
        call.enqueue(new Callback<ArrayList<Artist>>() {
            @Override
            public void onResponse(Call<ArrayList<Artist>> call, Response<ArrayList<Artist>> response) {
                for (Artist artist:response.body()
                        ) {
                    artists.add(artist);
                }
                adapterArtist.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Call<ArrayList<Artist>> call, Throwable t) {

            }
        });
        adapterArtist.setOnItemClickListener(new AdapterArtist.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
               mListener.goToTrackInArtist(artists.get(position).getId(),true);
            }
        });
    }

    void loadPlaylist(){
        playlists.clear();
       adapterPlaylist = new AdapterPlaylist(getActivity(),playlists,R.layout.row_home_playlist_item_grid);
        rcNewPlaylists.setLayoutManager(new LinearLayoutManager(getActivity(),LinearLayoutManager.HORIZONTAL,false));
        rcNewPlaylists.setHasFixedSize(true);
        rcNewPlaylists.setAdapter(adapterPlaylist);
        Call<ArrayList<Playlist>> call= apiServices.getPlaylists(0,10,null);
        call.enqueue(new Callback<ArrayList<Playlist>>() {
            @Override
            public void onResponse(Call<ArrayList<Playlist>> call, Response<ArrayList<Playlist>> response) {
                for (Playlist playlist:response.body()
                        ) {
                    playlists.add(playlist);
                }
                adapterArtist.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Call<ArrayList<Playlist>> call, Throwable t) {
            }
        });
        adapterPlaylist.setOnItemClickListener(new AdapterPlaylist.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                mListener.goToTrackInPlaylist((int)playlists.get(position).getId(),true);
            }
        });
    }

    LinearLayoutManager myLinearLayoutManager  = new LinearLayoutManager(getActivity()){
        @Override
        public boolean canScrollVertically() {
            return false;
        }
    };


    void loadTrack(){
        tracks.clear();
        adapterTrack = new AdapterTrack(getActivity(),tracks,R.layout.row_track_item);
        rcNewReleases.setLayoutManager(new LinearLayoutManager(getActivity(),LinearLayoutManager.VERTICAL,false){
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        });
        rcNewReleases.setHasFixedSize(true);
        rcNewReleases.setAdapter(adapterTrack);
        Call<ArrayList<Track>> call= apiServices.getTracks(0,10,null);
        call.enqueue(new Callback<ArrayList<Track>>() {
            @Override
            public void onResponse(Call<ArrayList<Track>> call, Response<ArrayList<Track>> response) {
                for (Track track:response.body()
                        ) {
                    tracks.add(track);
                }
                adapterTrack.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Call<ArrayList<Track>> call, Throwable t) {
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

    void loadCategory(){
        categories.clear();
        adapterCategories = new AdapterCategories(getActivity(),categories,R.layout.row_category_item);
        rcCategories.setLayoutManager(new GridLayoutManager(getActivity(),2));
        rcCategories.setHasFixedSize(true);
        rcCategories.setAdapter(adapterCategories);
        adapterCategories.setOnItemClickListener(new AdapterCategories.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                mListener.goToTrackInCategory(categories.get(position).getId(),categories.get(position).getTitle());
            }
        });
        Call<ArrayList<Categories>> call= apiServices.getCategories(0,4,null);
        call.enqueue(new Callback<ArrayList<Categories>>() {
            @Override
            public void onResponse(Call<ArrayList<Categories>> call, Response<ArrayList<Categories>> response) {
                for (Categories category:response.body()
                        ) {
                    categories.add(category);
                }
                adapterCategories.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Call<ArrayList<Categories>> call, Throwable t) {
            }
        });

    }


//    private void setSliderViews() {
//        for (int i = 0; i <= 3; i++) {
//            SliderView sliderView = new DefaultSliderView(getActivity());
//
//            switch (i) {
//                case 0:
//                    sliderView.setImageUrl("https://images.pexels.com/photos/547114/pexels-photo-547114.jpeg?auto=compress&cs=tinysrgb&dpr=2&h=750&w=1260");
//                    break;
//                case 1:
//                    sliderView.setImageUrl("https://images.pexels.com/photos/218983/pexels-photo-218983.jpeg?auto=compress&cs=tinysrgb&dpr=2&h=750&w=1260");
//                    break;
//                case 2:
//                    sliderView.setImageUrl("https://images.pexels.com/photos/747964/pexels-photo-747964.jpeg?auto=compress&cs=tinysrgb&h=750&w=1260");
//                    break;
//                case 3:
//                    sliderView.setImageUrl("https://images.pexels.com/photos/929778/pexels-photo-929778.jpeg?auto=compress&cs=tinysrgb&dpr=2&h=750&w=1260");
//                    break;
//            }
//
//            sliderView.setImageScaleType(ImageView.ScaleType.CENTER_CROP);
//            sliderView.setDescription("setDescription " + (i + 1));
//            ((DefaultSliderView) sliderView).setDescriptionTextSize(22);
//            final int finalI = i;
//            sliderView.setOnSliderClickListener(new SliderView.OnSliderClickListener() {
//                @Override
//                public void onSliderClick(SliderView sliderView) {
//
//                }
//            });
//
//            //at last add this view in your layout :
//            sliderLayout.addSliderView(sliderView);
//        }
//    }

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

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void goToAlbum(String query);
        void goToTrackInAlbum(int id, boolean isReplace);
        void goToArtist(String query);
        void goToTrackInArtist(int id, boolean isReplace);
        void goToPlaylist(String query);
        void goToTrackInPlaylist(int id, boolean isReplace);
        void goToCategory();
        void goToTrack(String query,boolean isReplace);
        void goToTrackInCategory(int id, String title);
        void goToTrackInLocalAlbum(int id, int type, String name);
        void goToTrackInLocalArtist(int id, int type, String name);
        void goToTrackInLocalPlaylist(int id);
        void goToFragment(FragmentParent fragment);
        void pop();
    }
}
