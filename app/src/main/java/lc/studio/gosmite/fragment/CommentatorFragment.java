package lc.studio.gosmite.fragment;


import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.GridView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.rest.RestService;

import java.util.List;

import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.widget.VideoView;
import lc.studio.gosmite.R;
import lc.studio.gosmite.VideoActivity;
import lc.studio.gosmite.VideoActivity_;
import lc.studio.gosmite.adapters.CommentatorAdapter;
import lc.studio.gosmite.api.Api;
import lc.studio.gosmite.model.Commentator;
import lc.studio.gosmite.model.Video;

@EFragment(R.layout.fragment_commentator)
public class CommentatorFragment extends Fragment {
    @ViewById
    GridView commentator_grid;

    @Bean
    CommentatorAdapter adapter;

    @RestService
    Api api;

    @AfterViews
    void init(){
        commentator_grid.setAdapter(adapter);
        getdata();
    }

    @Background
    void getdata(){
        List<Commentator> c = api.getCommentators();
        adapter.init(c);
    }

    @ItemClick
    public void commentator_gridItemClicked(Commentator commentator){
        Intent intent=new Intent();
        intent.setClass(getActivity(), VideoActivity_.class);
        intent.putExtra("commentator",commentator);
        startActivity(intent);
    }
}
