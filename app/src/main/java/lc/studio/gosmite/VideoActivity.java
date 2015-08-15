package lc.studio.gosmite;

import android.app.Activity;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;

import lc.studio.gosmite.fragment.PlaceholderFragment_;
import lc.studio.gosmite.model.Commentator;

/**
 * Created by legency on 2015/1/8.
 */
@EActivity(R.layout.video_activity)
public class VideoActivity extends FragmentActivity {
    @Extra
    Commentator commentator;

    @AfterViews
    void setFragment() {

        getSupportFragmentManager().beginTransaction()
                .add(R.id.container, new PlaceholderFragment_().builder().commentator(commentator.getId()).build())
                .commit();
    }

}
