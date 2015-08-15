package lc.studio.gosmite.fragment;

import android.net.Uri;
import android.support.v4.app.Fragment;
import android.widget.AbsListView;
import android.widget.ListView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.rest.RestService;

import java.util.List;

import gov.anzong.mediaplayer.VideoActivity;
import gov.anzong.util.StringUtil;
import lc.studio.gosmite.R;
import lc.studio.gosmite.adapters.VideosAdapter;
import lc.studio.gosmite.api.Api;
import lc.studio.gosmite.model.Video;

/**
 * A placeholder fragment containing a simple view.
 */
@EFragment(R.layout.fragment_main)
public class PlaceholderFragment extends Fragment implements AbsListView.OnScrollListener {

    @ViewById
    ListView video_list;

    @FragmentArg
    String commentator;

    @Bean
    VideosAdapter adapter;

    @RestService
    Api api;
    private int currentPage=0;
    private boolean dataloaded=false;

    @AfterViews
    void listbind() {
        video_list.setAdapter(adapter);
        video_list.setOnScrollListener(this);
        getdata();
    }

    @Background
    void getdata() {
        List<Video> v;
        if(StringUtil.isEmpty(commentator)){
             v = api.getVideos(currentPage);
        }else{
             v = api.getVideosByCommentator(currentPage,commentator);
        }
        binddata(v);
    }
    @UiThread
    void binddata(List<Video> v){
        if(v!=null){
            adapter.init(v);
        }
        dataloaded=true;
    }
    @ItemClick
    public void video_listItemClicked(Video video){
        getdetail(video);
//        ((MainActivity)getActivity()).pushFragment(PlayerFragment_.builder().videoslug(video.getSlug()).build());
    }
    @Background
    void getdetail(Video video){
        Video detail = api.getVideoDetail(video.getSlug());
//        String videoPath="http://pro.api.115.com/m3u8/%5BHD%5DJUX-422.avi?filesha1=BBABDC9A5141F47050A1B9F0ACFCC266F2B0D92D&userid=365011808&rsa=80aed017cb172a045e87030e66e55a97&definition=3";
        VideoActivity.openVideo(getActivity(),Uri.parse(detail.getVideo_url()),detail.getTitle());
//        mIntent.setClass(getActivity(),ReceiveIntentActivity.class);
//        Intent intent = new Intent(Intent.ACTION_VIEW);
//        intent.setDataAndType(Uri.parse(detail.getVideo_url()), "video/mp4");
//        getActivity().startActivity(intent);
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollState == SCROLL_STATE_IDLE&&dataloaded==true) {
            if (video_list.getLastVisiblePosition() >= video_list.getCount() - 1) {
                currentPage++;
                //load more list items:
                getdata();
            }
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

    }
}