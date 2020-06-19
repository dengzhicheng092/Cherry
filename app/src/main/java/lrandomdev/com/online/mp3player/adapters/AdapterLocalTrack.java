package lrandomdev.com.online.mp3player.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.varunest.sparkbutton.SparkButton;

import java.io.File;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import io.objectbox.BoxStore;
import lrandomdev.com.online.mp3player.ActivityHome;
import lrandomdev.com.online.mp3player.MainApplication;
import lrandomdev.com.online.mp3player.R;
import lrandomdev.com.online.mp3player.fragments.dialogs.FragmentSelectPlaylistDialog;
import lrandomdev.com.online.mp3player.fragments.locals.FragmentLibrary;
import lrandomdev.com.online.mp3player.helpers.Helpers;
import lrandomdev.com.online.mp3player.helpers.RestClient;
import lrandomdev.com.online.mp3player.models.Artist;
import lrandomdev.com.online.mp3player.models.Track;

public class AdapterLocalTrack extends RecyclerView.Adapter<AdapterLocalTrack.TrackViewHolder> {
    private Context context;
    private ArrayList<Track> tracks;
    AdapterTrack.OnItemClickListener mItemClickListener;
    int resources;
    BoxStore boxStore;

    public AdapterLocalTrack(Context context, ArrayList<Track> tracks, int resources) {
        this.context = context;
        this.tracks = tracks;
        this.resources = resources;
        boxStore = MainApplication.getApp().getBoxStore();
    }

    @Override
    public AdapterLocalTrack.TrackViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(this.resources, parent, false);
        return new AdapterLocalTrack.TrackViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final AdapterLocalTrack.TrackViewHolder holder, int position) {
        final Track track = tracks.get(position);
        holder.tvTitle.setText(track.getTitle());
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
            holder.tvArtist.setText(Helpers.trimRightComma(artist_text));
        } else {
            holder.tvArtist.setText(track.getArtist());
        }

        holder.btnMenu.setTag(track);
        holder.btnMenu.setTag(R.id.tvTitle, position);
        holder.btnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMenu(v);
            }
        });

        int placeholder = R.drawable.bg_two;
        if (resources == R.layout.row_track_item_grid) {
            placeholder = R.drawable.bg_two;
        }


        if (track.isLocal()) {
            if (track.getDuration().contains(":")) {
                holder.tvDuration.setText(track.getDuration());
            } else {
                holder.tvDuration.setText(Helpers.timer(Long.parseLong(track.getDuration())));
            }
        }else{
            holder.tvDuration.setText(track.getDuration());
        }

        Glide.with(context)
                .load(track.getThumb())
                .apply(new RequestOptions().placeholder(placeholder).error(placeholder))
                .into(holder.imgThumb);
    }

    @Override
    public int getItemCount() {
        return tracks.size();
    }

    public class TrackViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView tvTitle;
        TextView tvArtist;
        ImageButton btnMenu;
        TextView tvDuration;
        ImageView imgThumb;
        SparkButton btnFavorites;

        public TrackViewHolder(View itemView) {
            super(itemView);
            tvTitle = (TextView) itemView.findViewById(R.id.tvTitle);
            tvArtist = (TextView) itemView.findViewById(R.id.tvArtist);
            tvDuration = (TextView) itemView.findViewById(R.id.tvDuration);
            btnMenu = (ImageButton) itemView.findViewById(R.id.btnMenu);
            btnFavorites = (SparkButton) itemView.findViewById(R.id.btnFavorites);
            btnFavorites.setVisibility(View.GONE);
            if (resources == R.layout.row_track_item) {
                imgThumb = (ImageView) itemView.findViewById(R.id.imgThumb);
            } else {
                imgThumb = (ImageView) itemView.findViewById(R.id.imgThumb);
            }
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mItemClickListener != null) {
                mItemClickListener.onItemClick(v, getAdapterPosition());
            }
        }
    }

    public void setOnItemClickListener(final AdapterTrack.OnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }

    public void showMenu(final View view) {
        PopupMenu menu = new PopupMenu(context, view);
        menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                int position = (int) view.getTag(R.id.tvTitle);
                final Track track = (Track) view.getTag();
                switch (id) {
                    case R.id.item_play:
                        Intent intent = new Intent(ActivityHome.ON_TRACK_CLICK_PLAY);
                        Bundle bundle = new Bundle();
                        bundle.putInt("TRACK_INDEX", position);
                        bundle.putSerializable("TRACKS", tracks);
                        intent.putExtra("ON_TRACK_CLICK_ITEM", bundle);
                        context.sendBroadcast(intent);
                        break;

                    case R.id.item_add_to_queue:
                        intent = new Intent(ActivityHome.ON_TRACK_CLICK_ADD_TO_QUEUE);
                        intent.putExtra("TRACK", track);
                        context.sendBroadcast(intent);
                        Toast.makeText(context, track.getTitle() + " " + context.getString(R.string.has_been_add_to_queue), Toast.LENGTH_LONG).show();
                        break;

                    case R.id.item_to_playlist:
                        FragmentSelectPlaylistDialog newFragment = FragmentSelectPlaylistDialog
                                .newInstance();
                        bundle = new Bundle();
                        bundle.putSerializable("item", track);
                        newFragment.setArguments(bundle);
                        Activity activity = (Activity) context;
                        newFragment.show(activity.getFragmentManager(), "dialog");
                        break;

                    case R.id.item_share:
                        Helpers.shareAction(context, track);
                        break;

                    case R.id.item_delete:
                        try {
                            File file = new File(track.getRealPath());
                            file.delete();
                            tracks.remove(track);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        notifyDataSetChanged();
                        break;
                }
                return true;
            }
        });
        menu.inflate(R.menu.menu_track_local);
        menu.show();
    }

    public interface OnItemClickListener {
        public void onItemClick(View view, int position);
    }
}
