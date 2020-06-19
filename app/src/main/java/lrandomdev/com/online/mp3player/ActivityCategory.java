package lrandomdev.com.online.mp3player;


import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import lrandomdev.com.online.mp3player.adapters.PagerAdapterLibrary;
import lrandomdev.com.online.mp3player.fragments.FragmentAlbum;
import lrandomdev.com.online.mp3player.fragments.FragmentPlaylist;
import lrandomdev.com.online.mp3player.fragments.FragmentTrack;
import lrandomdev.com.online.mp3player.helpers.ApiServices;
import lrandomdev.com.online.mp3player.helpers.RestClient;

public class ActivityCategory extends AppCompatActivity {
    ApiServices apiServices;
    ViewPager viewPager;
    Fragment[] fragment = new Fragment[3];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        apiServices= RestClient.getApiService();
        setContentView(R.layout.activity_category);
        fragment[0]= FragmentTrack.newInstance();
        fragment[1]= FragmentAlbum.newInstance();
        fragment[2]=FragmentPlaylist.newInstance();
        PagerAdapterLibrary adapter = new PagerAdapterLibrary(getSupportFragmentManager(),fragment,ActivityCategory.this);
        viewPager=(ViewPager)findViewById(R.id.pager);
        viewPager.setAdapter(adapter);
        TabLayout tabLayout = (TabLayout)findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
    }

}
