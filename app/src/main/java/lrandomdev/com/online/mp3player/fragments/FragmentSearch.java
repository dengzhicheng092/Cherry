package lrandomdev.com.online.mp3player.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.util.ArrayList;

import lrandomdev.com.online.mp3player.R;
import lrandomdev.com.online.mp3player.adapters.AdapterCategories;
import lrandomdev.com.online.mp3player.helpers.ApiServices;
import lrandomdev.com.online.mp3player.helpers.Helpers;
import lrandomdev.com.online.mp3player.helpers.RestClient;
import lrandomdev.com.online.mp3player.models.Categories;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragmentSearch extends FragmentParent{
    ArrayList<Categories> categories=new ArrayList<Categories>();
    ApiServices apiServices;
    RecyclerView rc;
    AdapterCategories adapterCategories;
    EditText edtSearch;
    private FragmentHome.OnFragmentInteractionListener mListener;

    public static FragmentSearch newInstance() {
        FragmentSearch fragment = new FragmentSearch();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        apiServices= RestClient.getApiService();
        edtSearch=(EditText)view.findViewById(R.id.edtSearch);
        edtSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentRealSearch fragmentRealSearch = FragmentRealSearch.newInstance();
                mListener.goToFragment(fragmentRealSearch);
                //Intent intent =new Intent(getActivity(), ActivitySearch.class);
                //startActivity(intent);
            }
        });
        rc= (RecyclerView) view.findViewById(R.id.rc);
        adapterCategories =new AdapterCategories(getActivity(),categories,R.layout.row_category_item);
        GridLayoutManager gridLayoutManager =new GridLayoutManager(getActivity(),2){
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };
        adapterCategories.setOnItemClickListener(new AdapterCategories.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                mListener.goToTrackInCategory(categories.get(position).getId(),"\""+categories.get(position).getTitle()+"\" "+getString(R.string.category));
            }
        });
        rc.setLayoutManager(gridLayoutManager);
        rc.setAdapter(adapterCategories);
        rc.setHasFixedSize(true);
        loadCategory();

        LinearLayout adView = (LinearLayout)view.findViewById(R.id.adView);
        Helpers.loadAd(getActivity(),adView);
        return view;
    }

    void loadCategory(){
        Call<ArrayList<Categories>> call = apiServices.getCategories(0,50,null);
        call.enqueue(new Callback<ArrayList<Categories>>() {
            @Override
            public void onResponse(Call<ArrayList<Categories>> call, Response<ArrayList<Categories>> response) {
                for (int i = 0; i < response.body().size() ; i++) {
                    categories.add(response.body().get(i));
                }
                adapterCategories.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Call<ArrayList<Categories>> call, Throwable t) {

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
