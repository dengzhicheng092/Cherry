package lrandomdev.com.online.mp3player.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wang.avi.AVLoadingIndicatorView;

import java.util.ArrayList;

import lrandomdev.com.online.mp3player.R;
import lrandomdev.com.online.mp3player.adapters.AdapterPlaylist;
import lrandomdev.com.online.mp3player.fragments.locals.FragmentLibrary;
import lrandomdev.com.online.mp3player.helpers.ApiServices;
import lrandomdev.com.online.mp3player.helpers.RestClient;
import lrandomdev.com.online.mp3player.interfaces.EndlessRecyclerViewScrollListener;
import lrandomdev.com.online.mp3player.models.Playlist;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragmentPlaylist extends FragmentParent {
    ArrayList<Playlist> playlists = new ArrayList<Playlist>();
    AdapterPlaylist mAdapter;
    RecyclerView recyclerView;
    int resources;
    int first = -10;
    int offset = 10;
    ApiServices apiServices;
    AVLoadingIndicatorView avLoadingIndicatorView;
    SwipeRefreshLayout swipeRefreshLayout;
    String query = null;
    Toolbar toolbar;

    private FragmentHome.OnFragmentInteractionListener mListener;

    public static FragmentPlaylist newInstance() {
        FragmentPlaylist fragment = new FragmentPlaylist();
        return fragment;
    }

    public static final FragmentPlaylist newInstance(String query) {
        FragmentPlaylist fragment = new FragmentPlaylist();
        Bundle bundle = new Bundle();
        bundle.putString("query",query);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            query= getArguments().getString("query");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recyclerview,
                container, false);
        toolbar=(Toolbar)view.findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_back_white);
        if(query==null) {
            toolbar.setTitle(getString(R.string.playlists));
        }else{
            toolbar.setTitle(getString(R.string.playlists) +" "+getString(R.string.contain)+" \""+query+"\"");
        }
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.pop();
            }
        });
        apiServices = RestClient.getApiService();
        avLoadingIndicatorView = (AVLoadingIndicatorView) view.findViewById(R.id.loadingView);
        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 2);
        recyclerView.setLayoutManager(gridLayoutManager);
        this.resources = R.layout.row_playlist_item_grid;
        recyclerView.setHasFixedSize(true);
        FragmentLibrary fragmentLibrary = ((FragmentLibrary) getParentFragment());
        mAdapter = new AdapterPlaylist(getActivity(), playlists, this.resources);
        mAdapter.setOnItemClickListener(new AdapterPlaylist.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                mListener.goToTrackInPlaylist((int)playlists.get(position).getId(),true);
            }
        });
        recyclerView.setAdapter(mAdapter);
        playlists.clear();
        this.first=-10;
        loadMore();
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swiperefresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (playlists.size() > 0) {
                    Call<ArrayList<Playlist>> call = apiServices.pullPlaylists(playlists.get(0).getId() + "");
                    call.enqueue(new Callback<ArrayList<Playlist>>() {
                        @Override
                        public void onResponse(Call<ArrayList<Playlist>> call, Response<ArrayList<Playlist>> response) {
                            swipeRefreshLayout.setRefreshing(false);
                            ArrayList<Playlist> tmp = response.body();
                            if (tmp != null) {
                                for (int i = 0; i < tmp.size(); i++) {
                                    playlists.add(0, tmp.get(i));
                                }
                            }
                            mAdapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onFailure(Call<ArrayList<Playlist>> call, Throwable t) {
                            swipeRefreshLayout.setRefreshing(false);
                        }
                    });
                } else {
                    swipeRefreshLayout.setRefreshing(false);
                }
            }
        });
        recyclerView.addOnScrollListener(new EndlessRecyclerViewScrollListener(gridLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                loadMore();
            }
        });
        return view;
    }



    void loadMore() {
        avLoadingIndicatorView.show();
        first += 10;
        final Call<ArrayList<Playlist>> call = apiServices.getPlaylists(first, offset, query);

                call.enqueue(new Callback<ArrayList<Playlist>>() {
                    @Override
                    public void onResponse(Call<ArrayList<Playlist>> call, Response<ArrayList<Playlist>> response) {
                        ArrayList<Playlist> tmpListPhoto = response.body();
                        if (tmpListPhoto != null) {
                            for (int i = 0; i < tmpListPhoto.size(); i++) {
                                playlists.add(tmpListPhoto.get(i));
                            }
                        }

                                mAdapter.notifyDataSetChanged();
                                avLoadingIndicatorView.hide();


                    }

                    @Override
                    public void onFailure(Call<ArrayList<Playlist>> call, Throwable t) {

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
