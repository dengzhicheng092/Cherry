package lrandomdev.com.online.mp3player;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.codemybrainsout.ratingdialog.RatingDialog;
import com.google.gson.JsonObject;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.ViewHolder;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;
import jp.wasabeef.glide.transformations.BlurTransformation;
import lrandomdev.com.online.mp3player.adapters.AdapterTrackInQueue;
import lrandomdev.com.online.mp3player.fragments.FragmentAlbum;
import lrandomdev.com.online.mp3player.fragments.FragmentArtist;
import lrandomdev.com.online.mp3player.fragments.FragmentCategories;
import lrandomdev.com.online.mp3player.fragments.FragmentHome;
import lrandomdev.com.online.mp3player.fragments.FragmentSetting;
import lrandomdev.com.online.mp3player.fragments.locals.FragmentLibrary;
import lrandomdev.com.online.mp3player.fragments.FragmentParent;
import lrandomdev.com.online.mp3player.fragments.FragmentPlaylist;
import lrandomdev.com.online.mp3player.fragments.FragmentSearch;
import lrandomdev.com.online.mp3player.fragments.dialogs.FragmentSelectPlaylistDialog;
import lrandomdev.com.online.mp3player.fragments.FragmentTrack;
import lrandomdev.com.online.mp3player.fragments.FragmentTrackInAlbum;
import lrandomdev.com.online.mp3player.fragments.FragmentTrackInArtist;
import lrandomdev.com.online.mp3player.fragments.FragmentTrackInPlaylist;
import lrandomdev.com.online.mp3player.helpers.ApiServices;
import lrandomdev.com.online.mp3player.helpers.Helpers;
import lrandomdev.com.online.mp3player.helpers.RestClient;
import lrandomdev.com.online.mp3player.helpers.SimpleItemTouchHelperCallback;
import lrandomdev.com.online.mp3player.models.Artist;
import lrandomdev.com.online.mp3player.models.Track;
import lrandomdev.com.online.mp3player.services.ServicePlayer;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Lrandom on 3/23/18.
 */

public class ActivityHome extends ActivityParent implements
        SeekBar.OnSeekBarChangeListener, FragmentHome.OnFragmentInteractionListener{
    private static final String TAG = ActivityHome.class.getSimpleName();
    private FragmentManager fragmentManager;
    private FragmentParent fragment = null;
    private SearchView mSearchView;
    private SlidingUpPanelLayout slidingUpPanelLayout;
    LinearLayout miniPlayback;
    SlidingUpPanelLayout.PanelState oldPanelState = SlidingUpPanelLayout.PanelState.COLLAPSED;
    ServicePlayer audioPlayerService;
    Handler handler = new Handler();
    ArrayList<Track> tracks = new ArrayList<Track>();
    TextView tvTitle, tvTotalTime, tvElapsedTime, tvArtist, tvMiniTitle, tvMiniArtist, tvProfile;
    ImageButton btnPlayAndPause, btnNext, btnPrev, btnRepeat, btnShuffle,
            btnPlaylist, btnShare, btnLyrics, btnAddToPlaylist, btnMiniPrev, btnMiniNext, btnMiniPlayAndPause;
    SeekBar prgTrack;
    boolean isRepeat, isShuffle;
    int trackIndex;
    String thumb;
    CircleImageView imgThumb;
    FrameLayout frameLayout;
    ImageView imgBg;
    ImageButton btnBack;
    Animation rotation, rotationMenuDisk;
    ImageView imgMiniThumb, navigationHeaderBg;
    AdapterTrackInQueue adapterTrackInQueue;
    Boolean flagSetting = false;
    private ItemTouchHelper mItemTouchHelper;
    Intent intent;
    String lang;
    ApiServices apiService;
    String lyrics="";
    AVLoadingIndicatorView indicator,mainIndicator;
    public static final String ON_TRACK_CLICK_PLAY="lrandomdev.com.online.mp3player.ON_TRACK_CLICK_PLAY";
    public static final String ON_TRACK_CLICK_ADD_TO_QUEUE="lrandomdev.com.online.mp3player.ON_TRACK_CLICK_ADD_TO_QUEUE";
    FragmentTransaction fragmentTransaction;


    private BroadcastReceiver notificationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()){
                case ServicePlayer.UPDATE_UI:
                    updatePlaybackUI();
                    indicator.setVisibility(View.GONE);
                    mainIndicator.setVisibility(View.GONE);
                    btnMiniPlayAndPause.setVisibility(View.VISIBLE);
                    btnPlayAndPause.setVisibility(View.VISIBLE);
                    break;

                case ServicePlayer.BUFFERING:
                    indicator.setVisibility(View.VISIBLE);
                    mainIndicator.setVisibility(View.VISIBLE);

                    imgMiniThumb.setImageResource(R.drawable.bg);
                    //imgThumb.setImageResource(R.drawable.bg);
                    imgThumb.refreshDrawableState();

                    Glide.with(getApplicationContext()).load(R.drawable.bg)
                            .apply(RequestOptions.bitmapTransform(new BlurTransformation(30, 15)).error(R.drawable.bg).placeholder(R.drawable.bg))
                            .into(imgBg);

                    tvMiniTitle.setText(getString(R.string.loading));
                    tvMiniArtist.setText(getString(R.string.loading));
                    tvArtist.setText(getString(R.string.loading));
                    tvArtist.setText(getString(R.string.loading));

                    btnMiniPlayAndPause.setVisibility(View.INVISIBLE);
                    btnPlayAndPause.setVisibility(View.INVISIBLE);
                    break;

                case ON_TRACK_CLICK_PLAY:
                    Bundle bundle =intent.getBundleExtra("ON_TRACK_CLICK_ITEM");
                    trackIndex= bundle.getInt("TRACK_INDEX");
                    tracks=(ArrayList<Track>) bundle.getSerializable("TRACKS");
                    if(audioPlayerService!=null){
                        audioPlayerService.play(trackIndex,tracks);
                    }
                    break;

                case ON_TRACK_CLICK_ADD_TO_QUEUE:
                    Track track=(Track)intent.getSerializableExtra("TRACK");
                    tracks.add(track);
                    if (adapterTrackInQueue != null) {
                        adapterTrackInQueue.notifyDataSetChanged();
                    }
                    break;
            }
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        apiService = RestClient.getApiService();
        new Thread(new Runnable() {
            @Override
            public void run() {

                Intent intent = new Intent(ActivityHome.this,
                        ServicePlayer.class);
                getApplicationContext().bindService(intent, serviceConnection,
                        Context.BIND_AUTO_CREATE);

                SharedPreferences prefs = getSharedPreferences("timer_sleep", MODE_PRIVATE);
                if (!prefs.contains("h")) {
                    SharedPreferences.Editor editor = getSharedPreferences("timer_sleep", MODE_PRIVATE).edit();
                    editor.putInt("h", 0);
                    editor.putInt("m", 0);
                    editor.apply();
                }

                prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                lang = prefs.getString("language", "");

            }
        }).run();


        final RatingDialog ratingDialog = new RatingDialog.Builder(this)
                .session(10)
                .ratingBarColor(R.color.colorPrimary).build();
        ratingDialog.show();
        presetMainUI();
        presetPlaybackUI();
        if (audioPlayerService != null) {
            updatePlaybackUI();
        }

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }


    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ServicePlayer.UPDATE_UI);
        intentFilter.addAction(ServicePlayer.BUFFERING);
        intentFilter.addAction(ON_TRACK_CLICK_PLAY);
        intentFilter.addAction(ON_TRACK_CLICK_ADD_TO_QUEUE);
        registerReceiver(notificationReceiver, intentFilter);

        if (flagSetting) {
            int somePrefValue = Integer.valueOf(prefs.getString("themes", "0"));
            switch (somePrefValue) {
                case 0:
                    setTheme(R.style.PurpeTheme);
                    break;

                case 1:
                    setTheme(R.style.OrangeTheme);
                    break;
            }

            String language = prefs.getString("language", "en");
            if (!lang.equalsIgnoreCase(language)) {
                Locale locale = new Locale(language);
                Locale.setDefault(locale);
                Configuration config = new Configuration();
                config.locale = locale;
                getBaseContext().getResources().updateConfiguration(config,
                        getBaseContext().getResources().getDisplayMetrics());
                Intent intent = getIntent();
                finish();
                startActivity(intent);
            }

            presetMainUI();
            presetPlaybackUI();
            if (audioPlayerService != null) {
                updatePlaybackUI();
            }

            flagSetting = false;
        } else {
            if (audioPlayerService != null) {
                updatePlaybackUI();
            }
        }

        if (getIntent().hasExtra("DOWNLOAD")) {
            //move to download fragment
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(notificationReceiver);
    }

    void presetMainUI() {
        setContentView(R.layout.activity_home);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        fragment = FragmentHome.newInstance();

        fragmentTransaction.replace(R.id.main_container_wrapper, fragment);
        fragmentTransaction.commit();

        miniPlayback = (LinearLayout) findViewById(R.id.miniPlayback);
        slidingUpPanelLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        slidingUpPanelLayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                Log.i(TAG, "onPanelSlide, offset " + oldPanelState);
                Log.i(TAG, "onPanelSlide, offset " + slideOffset);
                Log.i(TAG, slidingUpPanelLayout.getPanelState() + "");

                if (oldPanelState == SlidingUpPanelLayout.PanelState.COLLAPSED && slideOffset > 0.5) {
                    miniPlayback.animate()
                            .alpha(0.0f)
                            .setDuration(300)
                            .setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    super.onAnimationEnd(animation);
                                    miniPlayback.setVisibility(View.GONE);
                                }
                            });
                }

                if (oldPanelState == SlidingUpPanelLayout.PanelState.EXPANDED && slideOffset < 0.5) {
                    miniPlayback.animate()
                            .alpha(1.0f)
                            .setDuration(300)
                            .setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    super.onAnimationEnd(animation);
                                    miniPlayback.setVisibility(View.VISIBLE);
                                }
                            });
                }
            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
                if (newState == SlidingUpPanelLayout.PanelState.EXPANDED) {
                    oldPanelState = SlidingUpPanelLayout.PanelState.EXPANDED;
                }

                if (newState == SlidingUpPanelLayout.PanelState.COLLAPSED) {
                    oldPanelState = SlidingUpPanelLayout.PanelState.COLLAPSED;
                }
            }
        });

        BottomNavigationView bottomNavigationView = (BottomNavigationView)findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.nav_home:
                        fragmentTransaction = fragmentManager.beginTransaction();
                        fragment = FragmentHome.newInstance();
                        fragmentTransaction.replace(R.id.main_container_wrapper, fragment);
                        fragmentTransaction.commit();
                        break;

                    case R.id.nav_search:
                        fragmentTransaction =fragmentManager.beginTransaction();
                        fragment= FragmentSearch.newInstance();
                        fragmentTransaction.replace(R.id.main_container_wrapper,fragment);
                        fragmentTransaction.commit();
                        break;

                    case R.id.nav_my_music:
                        fragmentTransaction =fragmentManager.beginTransaction();
                        fragment= FragmentLibrary.newInstance();
                        fragmentTransaction.replace(R.id.main_container_wrapper,fragment);
                        fragmentTransaction.commit();
                        break;


                    case R.id.nav_settings:
                        fragmentTransaction =fragmentManager.beginTransaction();
                        fragment= FragmentSetting.newInstance();
                        fragmentTransaction.replace(R.id.main_container_wrapper,fragment);
                        fragmentTransaction.commit();
                        break;


                }
                return true;
            }
        });
    }//end presetMainUI


    private ServiceConnection serviceConnection = new ServiceConnection() {

        public void onServiceDisconnected(ComponentName name) {
            // TODO Auto-generated method stub
            // audioPlayerService = null;
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            // TODO Auto-generated method stub
            audioPlayerService = ((ServicePlayer.PlayerBinder) service)
                    .getService();
            if (audioPlayerService.getTrackIndex() != -1) {
                updatePlaybackUI();
            }
        }
    };


    public void presetPlaybackUI() {
        indicator=(AVLoadingIndicatorView)findViewById(R.id.indicator);
        mainIndicator=(AVLoadingIndicatorView)findViewById(R.id.mainIndicator);


        tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvArtist = (TextView) findViewById(R.id.tvArtist);
        tvMiniArtist = (TextView) findViewById(R.id.tvMiniArtist);
        tvMiniTitle = (TextView) findViewById(R.id.tvMiniTitle);
        btnMiniNext = (ImageButton) findViewById(R.id.btnMiniNext);
        btnMiniPrev = (ImageButton) findViewById(R.id.btnMiniPrev);
        btnMiniPlayAndPause = (ImageButton) findViewById(R.id.btnMiniPlayPause);
        imgMiniThumb = (ImageView) findViewById(R.id.imgMiniThumb);

        tvTotalTime = (TextView) findViewById(R.id.tvTotalTime);
        tvElapsedTime = (TextView) findViewById(R.id.tvElapsedTime);
        prgTrack = (SeekBar) findViewById(R.id.seekTrack);
        btnPlayAndPause = (ImageButton) findViewById(R.id.btnPlay);
        btnNext = (ImageButton) findViewById(R.id.btnNext);
        btnPrev = (ImageButton) findViewById(R.id.btnPrev);
        imgThumb = (CircleImageView) findViewById(R.id.imgThumb);
        imgBg = (ImageView) findViewById(R.id.bg);
        btnShuffle = (ImageButton) findViewById(R.id.btnShuffle);
        btnRepeat = (ImageButton) findViewById(R.id.btnRepeat);
        frameLayout = (FrameLayout) findViewById(R.id.frameAlbum);


        rotation = AnimationUtils.loadAnimation(ActivityHome.this, R.anim.rotation);
        rotation.setFillEnabled(true);
        rotation.setFillAfter(true);

        rotationMenuDisk = AnimationUtils.loadAnimation(ActivityHome.this, R.anim.rotation);
        rotationMenuDisk.setFillEnabled(true);
        rotationMenuDisk.setFillAfter(true);

        btnPlaylist = (ImageButton) findViewById(R.id.btnList);
        btnShare = (ImageButton) findViewById(R.id.btnShare);
        btnAddToPlaylist = (ImageButton) findViewById(R.id.btnAddToPlaylist);
        btnBack = (ImageButton) findViewById(R.id.btnBack);
        btnLyrics = (ImageButton) findViewById(R.id.btnLyrics);

        btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tracks.get(trackIndex).getRemoteId() == null) {
                    Helpers.shareAction(ActivityHome.this, tracks.get(trackIndex));
                } else {
                    Helpers.shareAction(ActivityHome.this, RestClient.BASE_URL + "detail?id=" + tracks.get(trackIndex).getRemoteId());
                }
            }
        });

        btnAddToPlaylist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentSelectPlaylistDialog newFragment = FragmentSelectPlaylistDialog
                        .newInstance();
                Bundle bundle = new Bundle();
                bundle.putSerializable("item", tracks.get(trackIndex));
                newFragment.setArguments(bundle);
                newFragment.show(ActivityHome.this.getFragmentManager(), "dialog");
            }
        });


        btnLyrics.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogPlus dialog = DialogPlus.newDialog(ActivityHome.this)
                        .setContentHolder(new ViewHolder(R.layout.layout_lyrics))
                        .setContentBackgroundResource(R.drawable.border_white)
                        .setExpanded(true, 800)
                        .create();
                ScrollView views = (ScrollView) dialog.getHolderView();
                final TextView tvContent = (TextView) views.findViewById(R.id.tvContent);
                final LinearLayout indicator = (LinearLayout) views.findViewById(R.id.wrapIndicator);

                lyrics = tracks.get(trackIndex).getDescription();
                if(lyrics.equalsIgnoreCase("")) {
                    String title = tracks.get(trackIndex).getTitle();
                    ArrayList<Artist> artists = tracks.get(trackIndex).getArtists();
                    indicator.setVisibility(View.VISIBLE);
                    if (artists != null && artists.size() != 0) {
                        Call<JsonObject> call = apiService.getLyrics(RestClient.BASE_URL + "lyrics.php", "json", artists.get(0).getArtist(), title);
                        call.enqueue(new Callback<JsonObject>() {
                            @Override
                            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                                JsonObject jsonObject = response.body();
                                Log.e("F", jsonObject.toString());
                                lyrics = jsonObject.get("lyrics").getAsString();
                                if (lyrics.equalsIgnoreCase("")) {
                                    lyrics = getString(R.string.not_found_lyrics);
                                }

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    tvContent.setText(Html.fromHtml(lyrics, Html.FROM_HTML_MODE_COMPACT));
                                } else {
                                    tvContent.setText(Html.fromHtml(lyrics));
                                }
                                indicator.setVisibility(View.GONE);
                            }

                            @Override
                            public void onFailure(Call<JsonObject> call, Throwable t) {
                                lyrics = getString(R.string.not_found_lyrics);
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    tvContent.setText(Html.fromHtml(lyrics, Html.FROM_HTML_MODE_COMPACT));
                                } else {
                                    tvContent.setText(Html.fromHtml(lyrics));
                                }
                                indicator.setVisibility(View.GONE);
                            }
                        });
                    }else{
                        lyrics = getString(R.string.not_found_lyrics);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            tvContent.setText(Html.fromHtml(lyrics, Html.FROM_HTML_MODE_COMPACT));
                        } else {
                            tvContent.setText(Html.fromHtml(lyrics));
                        }
                    }
                }else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        tvContent.setText(Html.fromHtml(lyrics, Html.FROM_HTML_MODE_COMPACT));
                    } else {
                        tvContent.setText(Html.fromHtml(lyrics));
                    }
                }

                dialog.show();
            }
        });


        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            }
        });


        btnMiniPlayAndPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (audioPlayerService.isPlay()) {
                    audioPlayerService.pause();
                    btnMiniPlayAndPause
                            .setImageResource(R.drawable.ic_play_no_circle);
                    btnPlayAndPause
                            .setImageResource(R.drawable.ic_play);
                } else {
                    audioPlayerService.resume();
                    btnMiniPlayAndPause
                            .setImageResource(R.drawable.ic_stop_no_circle);
                    btnPlayAndPause
                            .setImageResource(R.drawable.ic_stop);
                }
            }
        });

        btnMiniPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isShuffle) {
                    if (trackIndex == 0) {
                        trackIndex = tracks.size() - 1;
                    } else {
                        trackIndex -= 1;
                    }
                } else {
                    Random rand = new Random();
                    trackIndex = rand.nextInt((tracks.size() - 1) - 0 + 1) + 0;
                }
                audioPlayerService.play(trackIndex, tracks);
            }
        });

        btnMiniNext.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (!isShuffle) {
                    if (trackIndex == (tracks.size() - 1)) {
                        trackIndex = 0;
                    } else {
                        trackIndex += 1;
                    }
                } else {
                    Random rand = new Random();
                    trackIndex = rand.nextInt((tracks.size() - 1) - 0 + 1) + 0;
                }
                audioPlayerService.play(trackIndex, tracks);
            }
        });

        btnPlayAndPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnMiniPlayAndPause.callOnClick();
            }
        });


        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnMiniNext.callOnClick();
            }
        });

        btnPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnMiniPrev.callOnClick();
            }
        });

        btnShuffle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isShuffle) {
                    isShuffle = true;
                    audioPlayerService.setShuffle(true);
                    btnShuffle
                            .setImageResource(R.drawable.ic_shuffle_gr);
                } else {
                    isShuffle = false;
                    audioPlayerService.setShuffle(false);
                    btnShuffle.setImageResource(R.drawable.ic_shuffle);
                }
            }
        });

        btnRepeat.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (!isRepeat) {
                    isRepeat = true;
                    audioPlayerService.setRepeat(true);
                    btnRepeat
                            .setImageResource(R.drawable.ic_repeat_gr);
                } else {
                    isRepeat = false;
                    audioPlayerService.setRepeat(false);
                    btnRepeat.setImageResource(R.drawable.ic_repeat);
                }
            }
        });


        LinearLayout adView = (LinearLayout) findViewById(R.id.adView);
        Helpers.loadAd(getApplicationContext(),adView);
    }


    public void updatePlaybackUI() {
        tracks = audioPlayerService.getTracks();
        if (tracks != null) {
            trackIndex = audioPlayerService.getTrackIndex();
            Track track = tracks.get(trackIndex);
            tvTitle.setText(track.getTitle());

            String artist_text = "";
            ArrayList<Artist> artists = track.getArtists();
            if (artists != null && artists.size() != 0) {
                for (int i = 0; i < artists.size(); i++) {
                    if (i == (artists.size() - 1)) {
                        artist_text += artists.get(i).getArtist();
                    } else {
                        artist_text += artists.get(i).getArtist() + " , ";
                    }
                }
                tvArtist.setText(Helpers.trimRightComma(artist_text));
                tvMiniArtist.setText(Helpers.trimRightComma(artist_text));
            } else {
                tvArtist.setText(track.getArtist());
                tvMiniArtist.setText(track.getArtist());
            }

            if (audioPlayerService.isPlay()) {
                btnMiniPlayAndPause.setImageResource(R.drawable.ic_stop_no_circle);
                btnPlayAndPause.setImageResource(R.drawable.ic_stop);
                frameLayout.startAnimation(rotation);
            } else {
                btnMiniPlayAndPause.setImageResource(R.drawable.ic_play_no_circle);
                btnPlayAndPause.setImageResource(R.drawable.ic_play);
                frameLayout.clearAnimation();
            }

            tvMiniTitle.setText(track.getTitle());
            if (track.getThumb() != null) {
                thumb = tracks.get(trackIndex).getThumb();
                if(!tracks.get(trackIndex).isLocal()) {
                    thumb = RestClient.BASE_URL + thumb;
                }
                if (thumb != null) {
                    Glide.with(this).load(thumb)
                            .apply(RequestOptions.errorOf(R.drawable.bg).placeholder(R.drawable.bg))
                            .into(imgMiniThumb);

                    Glide.with(this).load(thumb)
                            .apply(RequestOptions.errorOf(R.drawable.bg).placeholder(R.drawable.bg))
                            .into(imgThumb);

                    Glide.with(this).load(thumb)
                            .apply(RequestOptions.bitmapTransform(new BlurTransformation(30, 15)).error(R.drawable.bg).placeholder(R.drawable.bg))
                            .into(imgBg);

                } else {
                    imgMiniThumb.setImageResource(R.drawable.bg);
                    imgThumb.setImageResource(R.drawable.bg);
                    imgThumb.refreshDrawableState();

                    Glide.with(this).load(R.drawable.bg)
                            .apply(RequestOptions.bitmapTransform(new BlurTransformation(30, 15)).error(R.drawable.bg).placeholder(R.drawable.bg))
                            .into(imgBg);

                    Glide.with(this).load(R.drawable.bg)
                            .apply(RequestOptions.bitmapTransform(new BlurTransformation(30, 15)).error(R.drawable.bg).placeholder(R.drawable.bg))
                            .into(navigationHeaderBg);
                }
            }

            adapterTrackInQueue = new AdapterTrackInQueue(ActivityHome.this, tracks);
            btnPlaylist.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DialogPlus dialog = DialogPlus.newDialog(ActivityHome.this)
                            .setContentHolder(new ViewHolder(R.layout.layout_queue))
                            .setContentBackgroundResource(R.drawable.border_white)
                            .setExpanded(true, 800)
                            .create();

                    RecyclerView listQueue = (RecyclerView) dialog.getHolderView();
                    listQueue.setAdapter(adapterTrackInQueue);
                    listQueue.setLayoutManager(new LinearLayoutManager(ActivityHome.this));
                    ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(adapterTrackInQueue);
                    mItemTouchHelper = new ItemTouchHelper(callback);
                    mItemTouchHelper.attachToRecyclerView(listQueue);
                    adapterTrackInQueue.setOnDragListener(new AdapterTrackInQueue.OnStartDragListener() {
                        @Override
                        public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
                            mItemTouchHelper.startDrag(viewHolder);
                        }
                    });
                    adapterTrackInQueue.setOnItemClickListener(new AdapterTrackInQueue.OnItemClickListener() {
                        @Override
                        public void onItemClick(View view, int position) {
                            audioPlayerService.play(position, tracks);
                        }
                    });
                    adapterTrackInQueue.setOnPlayListener(new AdapterTrackInQueue.OnPlayListener() {
                        @Override
                        public void onItemClickCallback(int position, ArrayList<Track> tracks) {
                            audioPlayerService.play(position, tracks);
                        }
                    });
                    dialog.show();
                }
            });
            prgTrack.setOnSeekBarChangeListener(this);
            updateProgress();
        }
    }


    private Runnable updateTime = new Runnable() {
        public void run() {
            // TODO Auto-generated method stub
            prgTrack.setSecondaryProgress(audioPlayerService
                    .getBufferingDownload());

            long totalDuration = audioPlayerService.getTotalTime();
            long currentDuration = audioPlayerService.getElapsedTime();
            tvTotalTime.setText("" + Helpers.timer(totalDuration));
            tvElapsedTime.setText("" + Helpers.timer(currentDuration));
            int progress = (int) (Helpers.getProgressPercentage(currentDuration,
                    totalDuration));
            prgTrack.setProgress(progress);
            handler.postDelayed(this, 100);
        }
    };

    public void updateProgress() {
        handler.postDelayed(updateTime, 100);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        handler.postDelayed(updateTime, 100);
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        handler.removeCallbacks(updateTime);
        int totalDuration = audioPlayerService.getTotalTime();
        int currentPosition = Helpers.progressToTimer(seekBar.getProgress(),
                totalDuration);
        audioPlayerService.seek(currentPosition);
        updateProgress();
    }

    @Override
    public void onBackPressed() {
        FragmentManager fm = getSupportFragmentManager();
        int count = fm.getBackStackEntryCount();

        if(count==0) {
            if (slidingUpPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED) {
                slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            } else {
                finish();
            }
        }else{
            pop();
        }
    }

    @Override
    public void goToTrackInAlbum(int id,boolean isReplace) {
        fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        if(isReplace){
            fragmentTransaction.replace(R.id.main_container_wrapper, FragmentTrackInAlbum.newInstance(id));
        }else{
            fragmentTransaction.add(R.id.main_container_wrapper, FragmentTrackInAlbum.newInstance(id));
        }
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    @Override
    public void goToTrackInArtist(int id, boolean isReplace) {
        fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        if(isReplace){
            fragmentTransaction.replace(R.id.main_container_wrapper, FragmentTrackInArtist.newInstance(id));
        }else{
            fragmentTransaction.add(R.id.main_container_wrapper, FragmentTrackInArtist.newInstance(id));
        }
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    @Override
    public void goToPlaylist(String query) {
        fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.main_container_wrapper, FragmentPlaylist.newInstance(query));
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    @Override
    public void goToTrackInPlaylist(int id, boolean isReplace) {
        fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        if(isReplace){
            fragmentTransaction.replace(R.id.main_container_wrapper, FragmentTrackInPlaylist.newInstance(id));
        }else{
            fragmentTransaction.add(R.id.main_container_wrapper, FragmentTrackInPlaylist.newInstance(id));
        }
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    @Override
    public void goToTrack(String query,boolean isReplace) {
        fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        if(isReplace){
            fragmentTransaction.replace(R.id.main_container_wrapper, FragmentTrack.newInstance(query));
        }else{
            fragmentTransaction.add(R.id.main_container_wrapper, FragmentTrack.newInstance(query));
        }
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    @Override
    public void goToAlbum(String query) {
        goToFragment(FragmentAlbum.newInstance(query));
    }

    @Override
    public void goToArtist(String query) {
        goToFragment(FragmentArtist.newInstance(query));
    }

    @Override
    public void goToCategory() {
        goToFragment(FragmentCategories.newInstance());
    }

    //1 album,2 artist,3 playlist
    @Override
    public void goToTrackInLocalAlbum(int id,int type, String name) {
       goToFragment(lrandomdev.com.online.mp3player.fragments.locals.FragmentTrack.newInstance(type,id,name));
    }

    @Override
    public void goToTrackInLocalArtist(int id,int type, String name) {
        goToFragment(lrandomdev.com.online.mp3player.fragments.locals.FragmentTrack.newInstance(type,id,name));
    }

    @Override
    public void goToTrackInLocalPlaylist(int id) {
        goToFragment(lrandomdev.com.online.mp3player.fragments.locals.FragmentTrackInPlaylist.newInstance(id));
    }



    @Override
    public void pop() {
        FragmentManager fm = getSupportFragmentManager();
        fm.popBackStack(fm.getBackStackEntryCount()-1,FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }

    @Override
    public void goToTrackInCategory(int id,String title) {
        fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.main_container_wrapper, FragmentTrack.newInstance(id,title));
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }


    @Override
    public void goToFragment(FragmentParent fragment){
        fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.main_container_wrapper,fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }
}
