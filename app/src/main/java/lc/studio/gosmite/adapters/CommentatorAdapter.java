package lc.studio.gosmite.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.UiThread;

import java.util.List;

import lc.studio.gosmite.model.Commentator;
import lc.studio.gosmite.model.Video;
import lc.studio.gosmite.views.CommentatorView;
import lc.studio.gosmite.views.CommentatorView_;
import lc.studio.gosmite.views.VideoCard;
import lc.studio.gosmite.views.VideoCard_;

/**
 * Created by legency on 2015/1/2.
 */
@EBean
public class CommentatorAdapter extends BaseAdapter {

    @RootContext
    Context context;

    private List<Commentator> commentators;

    @UiThread
    public void init(List<Commentator> commentators){
        if(this.commentators!=null&&this.commentators.size()>0){
            this.commentators.addAll(commentators);
        }else{
            this.commentators=commentators;
        }
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return commentators !=null?commentators.size():0;
    }

    @Override
    public Commentator getItem(int position) {
        return commentators !=null?commentators.get(position):null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        CommentatorView commentatorView= CommentatorView_.build(context);
        commentatorView.bind(getItem(position));
        return commentatorView;
    }

}
