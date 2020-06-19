package lrandomdev.com.online.mp3player.adapters;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import lrandomdev.com.online.mp3player.R;

public class PagerAdapterLibrary extends FragmentStatePagerAdapter{
    Fragment[] fragments ;
    Context context;

    public PagerAdapterLibrary(FragmentManager fm, Fragment[] fragments, Context context) {
        super(fm);
        this.fragments=fragments;
        this.context=context;
    }

    @Override
    public Fragment getItem(int position) {
        return fragments[position];
    }

    @Override
    public int getCount() {
        return fragments.length;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position){
            case 0:
                return context.getString(R.string.tracks);

            case 1:
                return context.getString(R.string.albums);

            case 2:
                return context.getString(R.string.artists);

            case 3:
                return context.getString(R.string.playlists);

            case 4:
                return context.getString(R.string.download);

            case 5:
                return context.getString(R.string.favorites);
        }
        return context.getString(R.string.tracks);
    }
}
