package gov.anzong.receiveintent;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import gov.anzong.mediaplayer.VideoActivity;
import gov.anzong.util.StringUtil;
import io.vov.vitamio.LibsChecker;

public class ReceiveIntentActivity extends Activity {
	public String uri, title;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (!LibsChecker.checkVitamioLibs(this))
			return;
		Intent intent = getIntent();
		uri = intent.getStringExtra("uri");
		title = intent.getStringExtra("title");
		if (!StringUtil.isEmpty(uri)) {
			if (StringUtil.isEmpty(title)) {
				title = "未知来源视频";
			}
			VideoActivity.openVideo(this, Uri.parse(uri), title);
			this.finish();
		} else {
			Toast.makeText(this, "视频地址错误", Toast.LENGTH_SHORT).show();
			try {
				android.os.Process.killProcess(android.os.Process.myPid());
			} catch (Exception e) {
			}
		}
	}
}
