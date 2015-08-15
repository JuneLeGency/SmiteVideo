package gov.anzong.task;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import gov.anzong.bean.VideoDetialData;
import gov.anzong.bean.VideoParseJson;
import gov.anzong.mediaplayer.VideoActivity;
import gov.anzong.util.FunctionUtil;
import gov.anzong.util.HttpUtil;
import gov.anzong.util.StringUtil;

public class SohuHandleLoadTask extends AsyncTask<String, Integer, String> {

	final String origurl, from;
	String title = "";
	String m3u8Url = "";
	final FragmentActivity context;
	static final String dialogTag = "load";
	int errorcode = -1;// 0参数错误,1网络错误,2解析错误

	public SohuHandleLoadTask(String origurl, String from,
			FragmentActivity context) {
		super();
		this.origurl = origurl;
		this.from = from;
		this.context = context;
	}

	@Override
	protected void onPreExecute() {
		FunctionUtil.createdialog(context, dialogTag);
		super.onPreExecute();
	}

	@Override
	protected void onPostExecute(String result) {
		FunctionUtil.closedialog(context, dialogTag);
		if (StringUtil.isEmpty(m3u8Url)) {
			String errorinfo = "";
			switch (errorcode) {// 0参数错误,1网络错误,2解析错误
			case 0:
				errorinfo = "参数错误";
				break;
			case 1:
				errorinfo = "网络错误";
				break;
			case 2:
				errorinfo = "解析错误";
				break;
			case -1:
			default:
				errorinfo = "未知错误";
				break;
			}
			Toast.makeText(context, errorcode, Toast.LENGTH_LONG).show();
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setData(Uri.parse(origurl));
			boolean isIntentSafe = context.getPackageManager()
					.queryIntentActivities(intent, 0).size() > 0;
			if (isIntentSafe) {
				Intent chooser = Intent.createChooser(intent, "出错了,请选择其他打开方式:");
				context.startActivityForResult(chooser, 123);
			}
		} else {
			if (result != null) {
				title = result;
			} else {
				title = "搜狐视频";
			}
			VideoActivity.openVideo(context, Uri.parse(m3u8Url), title);
			this.onCancelled();
		}
		super.onPostExecute(result);
	}

	@Override
	protected void onCancelled(String result) {
		this.onCancelled();
	}

	@Override
	protected void onCancelled() {
		if (context != null) {
			context.finish();
		}
	}

	@Override
	protected String doInBackground(String... params) {
		String uri = params[0];
		if (StringUtil.isEmpty(uri)) {
			errorcode = 0;
			return null;
		}
		final String htmlString = HttpUtil.iosGetHtml(uri);
		if (StringUtil.isEmpty(htmlString)) {
			errorcode = 1;
			return null;
		}
		final String id = StringUtil.getStringBetween(htmlString, 0, "vid=\"",
				"\";").result;
		if (StringUtil.isEmpty(id)) {
			errorcode = 2;
			return null;
		}
		m3u8Url = "http://hot.vrs.sohu.com/ipad" + id + ".m3u8";

		uri = FunctionUtil.uribase64(origurl);
		String js = "";
		for (int i = 0; i < 3; i++) {
			js = HttpUtil.iosGetHtml(uri);
			if (!StringUtil.isEmpty(js)) {
				i = 10;
			}
		}
		if (StringUtil.isEmpty(js)) {
			return null;
		}
		try {
			VideoDetialData[] jsondata = VideoParseJson.parseRead(js);
			if (FunctionUtil.isjsondataempty(jsondata)) {
				return null;
			}
			String title2 = jsondata[0].title;
			String sitename = jsondata[0].site;
			title = FunctionUtil.videotitle(title2, sitename, from);
			return jsondata[0].title;
		} catch (Exception e) {
			return null;
		}
	}

}
