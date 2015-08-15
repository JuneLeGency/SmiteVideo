package lc.studio.gosmite;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import gov.anzong.mediaplayer.VideoActivity;
import io.vov.vitamio.LibsChecker;
import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.widget.MediaController;
import io.vov.vitamio.widget.VideoView;

public class TestVideo extends Activity {

    /**
     * TODO: Set the path variable to a streaming video URL or a local media file
     * path.
     */
    private String path = "";
    private VideoView mVideoView;
    private EditText mEditText;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        if (!LibsChecker.checkVitamioLibs(this))
            return;
        setContentView(R.layout.videoview);
        mEditText = (EditText) findViewById(R.id.url);
        mVideoView = (VideoView) findViewById(R.id.surface_view);
        path="http://pl.youku.com/playlist/m3u8?ts=1420203900&keyframe=0&vid=XODYxNjEwMDAw&type=hd2&ep=dyaWGkuNUs0A4STYgT8bMn%2Fmc3ELXJZ1gnbN%2F4gXR8ZQOa%2FQnjrVwQ%3D%3D&sid=542020430477912ec4764&token=7446&ctype=12&ev=1&oip=3079203019";
//        path = "http://cdnuni.115.com/gdown_group14/M00/0A/EE/d5Nqu0vC0OIAAAAALcoIAGWTs608358772/Taylor.Swift_-_Crazier_%28HD.1080p.DD5.1.Blu-Ray%29-PeakScaler13.ts?k=GftTj3wuOs3WaVS1tmgAKg&t=1419234296&u=1790520662-20665225-a9jx47qdfj30oh91o&s=307200&file=Taylor.Swift_-_Crazier_%28HD.1080p.DD5.1.Blu-Ray%29-PeakScaler13.ts";
        if (path.equals("")) {
            // Tell the user to provide a media file URL/path.
            Toast.makeText(TestVideo.this, "Please edit VideoViewDemo Activity, and set path" + " variable to your media file URL/path", Toast.LENGTH_LONG).show();
            return;
        } else {
			/*
			 * Alternatively,for streaming media you can use
			 * mVideoView.setVideoURI(Uri.parse(URLstring));
			 */
//            mVideoView.setVideoPath(path);
            mVideoView.setMediaController(new MediaController(this));
            mVideoView.requestFocus();

            mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    // optional need Vitamio 4.0
                    mediaPlayer.setPlaybackSpeed(1.0f);
                }
            });
        }
//        startActivity(new Intent(this,MainActivity.class));
        VideoActivity.openVideo(this, Uri.parse(path), "lol");
    }

    public void startPlay(View view) {
        String url = mEditText.getText().toString();
        path = url;
        if (!TextUtils.isEmpty(url)) {
            mVideoView.setVideoPath(url);
        }
    }

    public void openVideo(View View) {
        mVideoView.setVideoPath(path);
    }


}