package lrandomdev.com.online.mp3player.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wang.avi.AVLoadingIndicatorView;

import java.util.ArrayList;

import lrandomdev.com.online.mp3player.ActivityHome;
import lrandomdev.com.online.mp3player.R;
import lrandomdev.com.online.mp3player.adapters.AdapterTrack;
import lrandomdev.com.online.mp3player.fragments.locals.FragmentLibrary;
import lrandomdev.com.online.mp3player.helpers.ApiServices;
import lrandomdev.com.online.mp3player.helpers.Helpers;
import lrandomdev.com.online.mp3player.helpers.RestClient;
import lrandomdev.com.online.mp3player.interfaces.EndlessRecyclerViewScrollListener;
import lrandomdev.com.online.mp3player.models.Track;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Lrandom on 3/29/18.
 */

public class FragmentTrack extends FragmentParent{
    ArrayList<Track> tracks=new ArrayList<Track>();
    AdapterTrack mAdapter;
    RecyclerView recyclerView;
    FragmentLibrary fragmentLibrary;
    ApiServices apiServices;
    int first=-10;
    int offset=10;
    AVLoadingIndicatorView avLoadingIndicatorView;
    int resources;
    SwipeRefreshLayout swipeRefreshLayout;
    String query = null;
    Toolbar toolbar;
    int id=0;
    String title;

    private FragmentHome.OnFragmentInteractionListener mListener;

    public static final FragmentTrack newInstance() {
        FragmentTrack fragment = new FragmentTrack();
        return fragment;
    }

    public static final FragmentTrack newInstance(int id, String title) {
        FragmentTrack fragment = new FragmentTrack();
        Bundle args=new Bundle();
        args.putInt("id",id);
        args.putString("title",title);
        fragment.setArguments(args);
        return fragment;
    }


    public static final FragmentTrack newInstance(String query) {
        FragmentTrack fragment = new FragmentTrack();
        Bundle args=new Bundle();
        args.putString("query",query);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            id = getArguments().getInt("id");
            title=getArguments().getString("title");
            query=getArguments().getString("query");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tracks,
                container, false);
        toolbar=(Toolbar)view.findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_back_white);

        if(query==null) {
            toolbar.setTitle(getString(R.string.tracks));
        }else{
            toolbar.setTitle(getString(R.string.tracks) +" "+getString(R.string.contain)+" \""+query+"\"");
        }

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.pop();
            }
        });

        if(id!=0){
            toolbar.setTitle(getString(R.string.tracks)+" in "+title);
        }
        avLoadingIndicatorView=(AVLoadingIndicatorView)view.findViewById(R.id.loadingView);
        apiServices= RestClient.getApiService();

        fragmentLibrary = ((FragmentLibrary) getParentFragment());
        recyclerView= (RecyclerView)view.findViewById(R.id.lvTrack);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        Boolean isGrid = prefs.getBoolean("track_grid", true);
        if(isGrid){
            GridLayoutManager gridLayoutManager=new GridLayoutManager(getActivity(),2);
            recyclerView.setLayoutManager(gridLayoutManager);
            this.resources=R.layout.row_track_item_grid;
            recyclerView.addOnScrollListener(new EndlessRecyclerViewScrollListener(gridLayoutManager) {
                @Override
                public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                    loadMore();
                }
            });
        }else{
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
            recyclerView.setLayoutManager(linearLayoutManager);
            this.resources=R.layout.row_track_item;
            recyclerView.addOnScrollListener(new EndlessRecyclerViewScrollListener(linearLayoutManager) {
                @Override
                public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                    loadMore();
                }
            });
        }

        recyclerView.setHasFixedSize(true);
        mAdapter = new AdapterTrack(getActivity(), tracks, fragmentLibrary, this.resources);
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
        loadMore();

        swipeRefreshLayout=(SwipeRefreshLayout)view.findViewById(R.id.swiperefresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(tracks.size()>0) {
                    Call<ArrayList<Track>> call= apiServices.pullTrackInCategories(tracks.get(0).getRemoteId(),id+"");
                    call.enqueue(new Callback<ArrayList<Track>>() {
                        @Override
                        public void onResponse(Call<ArrayList<Track>> call, Response<ArrayList<Track>> response) {
                            swipeRefreshLayout.setRefreshing(false);
                            ArrayList<Track> tmp = response.body();

                            if (tmp != null) {
                                for (int i = 0; i < tmp.size(); i++) {
                                    tracks.add(0, tmp.get(i));
                                }
                            }
                            mAdapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onFailure(Call<ArrayList<Track>> call, Throwable t) {
                            swipeRefreshLayout.setRefreshing(false);
                        }
                    });
                }else {
                    swipeRefreshLayout.setRefreshing(false);
                }
            }
        });

        Helpers.loadInAd(getActivity());
        return view;
    }

    void loadMore(){
        avLoadingIndicatorView.show();
        first+=10;

        Call<ArrayList<Track>> call=null;
        if(id!=0){
           call= apiServices.getTracks(first,offset,id+"",null,null,null);
        }else {
           call= apiServices.getTracks(first, offset, query);
        }

                call.enqueue(new Callback<ArrayList<Track>>() {
                    @Override
                    public void onResponse(Call<ArrayList<Track>> call, Response<ArrayList<Track>> response) {
                        ArrayList<Track> tmpListPhoto=response.body();
                        if(tmpListPhoto!=null) {
                            for (int i = 0; i < tmpListPhoto.size(); i++) {
                                tracks.add(tmpListPhoto.get(i));
                            }
                        }

                                mAdapter.notifyDataSetChanged();
                                avLoadingIndicatorView.hide();

                    }

                    @Override
                    public void onFailure(Call<ArrayList<Track>> call, Throwable t) {
                        Log.e("ERROR",t.getMessage().toString());

                                avLoadingIndicatorView.hide();

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
