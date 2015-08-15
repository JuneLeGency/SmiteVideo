package lc.studio.gosmite.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

import java.util.List;

import lc.studio.gosmite.model.Video;
import lc.studio.gosmite.views.VideoCard;
import lc.studio.gosmite.views.VideoCard_;

/**
 * Created by legency on 2015/1/2.
 */
@EBean
public class VideosAdapter extends BaseAdapter {

    @RootContext
    Context context;

    private List<Video> videoList;

    public void init(List<Video> videoList){
        if(this.videoList!=null&&this.videoList.size()>0){
            this.videoList.addAll(videoList);
        }else{
            this.videoList=videoList;
        }
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return videoList !=null?videoList.size():0;
    }

    @Override
    public Video getItem(int position) {
        return videoList !=null?videoList.get(position):null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        VideoCard videoCard= VideoCard_.build(context);
        videoCard.bind(getItem(position));
        return videoCard;
    }

}
