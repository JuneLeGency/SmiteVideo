package gov.anzong.receiveintent;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import gov.anzong.mediaplayer.VideoActivity;
import gov.anzong.util.StringUtil;
import io.vov.vitamio.LibsChecker;

public class ReceiveIntentFilterActivity extends Activity {
	public String title;
	public Uri uri;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (!LibsChecker.checkVitamioLibs(this))
			return;
		uri = getIntent().getData();
		if (uri != null) {
			if (!StringUtil.isEmpty(uri.toString())) {
				VideoActivity.openVideo(this, uri, getname(uri));
				this.finish();
			} else {
				Toast.makeText(this, "视频地址错误", Toast.LENGTH_SHORT).show();
				try {
					android.os.Process.killProcess(android.os.Process.myPid());
				} catch (Exception e) {
				}
			}
		} else {
			Toast.makeText(this, "视频地址错误", Toast.LENGTH_SHORT).show();
			try {
				android.os.Process.killProcess(android.os.Process.myPid());
			} catch (Exception e) {
			}
		}
	}

	public static String getname(Uri uritmp) {
		String path = uritmp.toString().trim();
		int pos = path.lastIndexOf("/");
		int totallen = path.length();
		String title = "";
		if (pos > 0 && pos + 1 < totallen) {
			title = path.substring(pos + 1);
		}
		if (title.equals("")) {
			pos = path.lastIndexOf("\\");
			if (pos > 0 && pos + 1 < totallen) {
				title = path.substring(pos + 1);
			}
		}
		if (title.equals("")) {
			title = "未知";
		}
		return title;
	}
}
