package lc.studio.gosmite.views;

import android.content.Context;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

import lc.studio.gosmite.R;
import lc.studio.gosmite.model.Commentator;

/**
 * Created by legency on 2015/1/8.
 */
@EViewGroup(R.layout.commentator_item)
public class CommentatorView extends LinearLayout{

    @ViewById
    TextView commentator_name;

    @ViewById
    ImageView commentator_avator;

    @ViewById
    TextView description;

    public CommentatorView(Context context) {
        super(context);
    }

    public void bind(Commentator commentator){
        Picasso.with(getContext()).load(commentator.getCover_url()).into(commentator_avator);
        commentator_name.setText(commentator.getTitle());
        description.setText(commentator.getDescription());
    }
}
