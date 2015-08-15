package lc.studio.gosmite.fragment;


import android.content.pm.ActivityInfo;
import android.support.v4.app.Fragment;
import android.view.View;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.rest.RestService;

import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.widget.VideoView;
import lc.studio.gosmite.R;
import lc.studio.gosmite.api.Api;
import lc.studio.gosmite.model.Video;

@EFragment(R.layout.fragment_player)
public class PlayerFragment extends Fragment {

    @ViewById
    VideoView videoview;

    @FragmentArg
    String videoslug;

    @RestService
    Api api;

    @AfterViews
    void getdata(){
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        getdetail(videoslug);
    }

    @UiThread
    void binddata(Video detail){
//        if (!LibsChecker.checkVitamioLibs(getActivity()))
//            return;
        videoview.requestFocus();
        videoview.setVideoPath(detail.getVideo_url());
//        videoview.setMediaController(new MediaController(getActivity()));
        videoview.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                // optional need Vitamio 4.0
                mediaPlayer.setPlaybackSpeed(1.0f);
            }
        });
        videoview.start();
        videoview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }
        });
    }

    @Background
    void getdetail(String slug){
        Video detail = api.getVideoDetail(slug);
        binddata(detail);
    }
}
