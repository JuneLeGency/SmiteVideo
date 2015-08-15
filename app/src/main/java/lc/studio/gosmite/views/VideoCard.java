package lc.studio.gosmite.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

import lc.studio.gosmite.R;
import lc.studio.gosmite.model.Video;

/**
 * Created by legency on 2015/1/2.
 */
@EViewGroup(R.layout.video_list_item)
public class VideoCard extends LinearLayout {
    @ViewById
    TextView title;

    @ViewById
    TextView commentator;

    @ViewById
    TextView duration;

    @ViewById
    LinearLayout video_top;

    @ViewById
    ImageView video_snap;

    Integer color;

    private final Context context;



    public VideoCard(Context context){
        super(context);
        this.context=context;
    }

    public void bind(Video video){
        title.setText(video.getTitle());
        commentator.setText(video.getCommentator());
        Picasso.with(context).load(video.getCover_url()).placeholder(R.drawable.no_signal).resize(800,450).centerCrop().into(video_snap);
        duration.setText(video.getDuration());
    }
}
