package lc.studio.gosmite;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.gson.Gson;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.rest.RestService;

import java.util.List;

import lc.studio.gosmite.api.Api;
import lc.studio.gosmite.model.Token;
import lc.studio.gosmite.model.Video;

@EActivity(R.layout.activity_test_api)
public class TestApi extends ActionBarActivity {

    @RestService
    Api api;

    @ViewById
    TextView logout;
    private Gson gson;

    @AfterInject
    @Background
    void getdatas() {
        gson = new Gson();
        Token token = api.register("Android", "7bf94266f8c4964a0128a1aa64ace8e12703174df792fb6aceab8d6df4850217");
        output(token);
        List<Video> videos = api.getVideos(0);
        output(videos);
    }

    void output(Object obj) {
        logout.append(gson.toJson(obj));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_api);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_test_api, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
