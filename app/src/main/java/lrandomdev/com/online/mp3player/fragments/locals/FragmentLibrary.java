package lrandomdev.com.online.mp3player.fragments.locals;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import lrandomdev.com.online.mp3player.R;
import lrandomdev.com.online.mp3player.adapters.PagerAdapterLibrary;
import lrandomdev.com.online.mp3player.fragments.FragmentParent;
import lrandomdev.com.online.mp3player.helpers.Helpers;

/**
 * Created by Lrandom on 3/29/18.
 */

public class FragmentLibrary extends FragmentParent {
    ViewPager viewPager;
    Fragment[] fragment = new Fragment[6];


    public static final FragmentLibrary newInstance() {
        FragmentLibrary fragment = new FragmentLibrary();
        return fragment;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view =inflater.inflate(R.layout.fragment_library, container, false);
        fragment[0]= FragmentTrack.newInstance();
        fragment[1]= FragmentAlbum.newInstance();
        fragment[2]= FragmentArtist.newInstance();
        fragment[3]=FragmentMyPlaylist.newInstance();
        fragment[4]=FragmentDownload.newInstance();
        fragment[5]=FragmentFavorites.newInstance();

        PagerAdapterLibrary adapter = new PagerAdapterLibrary(getActivity().getSupportFragmentManager(),fragment, getActivity());
        viewPager=(ViewPager)view.findViewById(R.id.view_pager);
        viewPager.setAdapter(adapter);
        TabLayout tabLayout = (TabLayout)view.findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        LinearLayout adView = (LinearLayout)view.findViewById(R.id.adView);
        Helpers.loadAd(getActivity(),adView);
        return view;
    }
}
