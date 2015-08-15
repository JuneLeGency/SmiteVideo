package gov.anzong.task;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import gov.anzong.bean.BiliBiliApiData;
import gov.anzong.bean.BiliBiliParseJson;
import gov.anzong.bean.DataBetter;
import gov.anzong.mediaplayer.VideoActivity;
import gov.anzong.util.FunctionUtil;
import gov.anzong.util.HttpUtil;
import gov.anzong.util.StringUtil;

public class BiliBiliAidLoadTask extends AsyncTask<String, Integer, String> {

	final String origurl;
	String title = "";
	final FragmentActivity context;
	static final String dialogTag = "load";
	int errorcode = -1;// 0参数错误,1网络错误,2解析错误

	public BiliBiliAidLoadTask(String origurl, FragmentActivity context) {
		super();
		this.origurl = origurl;
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
		if (result != null) {
			VideoActivity.openVideo(context, Uri.parse(result), title);
			this.onCancelled();
		} else {
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
			Toast.makeText(context, errorinfo, Toast.LENGTH_LONG).show();
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setData(Uri.parse(origurl));
			boolean isIntentSafe = context.getPackageManager()
					.queryIntentActivities(intent, 0).size() > 0;
			if (isIntentSafe) {
				Intent chooser = Intent.createChooser(intent, "出错了,请选择其他打开方式:");
				context.startActivityForResult(chooser, 123);
			}
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
		String id = StringUtil.getStringBetween(origurl, 0, "/av", "/").result;
		String apiurl = StringUtil.get_bilibili_apiurl_withid(id);
		String js = "";
		for (int i = 0; i < 3; i++) {
			js = HttpUtil.bilibiliGetHtml(apiurl);
			if (!StringUtil.isEmpty(js)) {
				i = 10;
			}
		}
		if (!StringUtil.isEmpty(js)) {
			try {
				BiliBiliApiData data = BiliBiliParseJson.parseRead(js);
				if (!StringUtil.isEmpty(data.cid)) {
					String html5cidurl = "http://www.bilibili.com/m/html5?cid="
							+ data.cid;
					String htmlString = HttpUtil.iosGetHtml(html5cidurl);
					String videourl = StringUtil.getStringBetween(htmlString,
							0, "\"src\":\"", "\"").result;
					if (StringUtil.isEmpty(data.title)) {
						title = "BiliBili";
					} else {
						title = data.title + " - BiliBili";
					}
					if (!StringUtil.isEmpty(videourl)) {
						return videourl;
					}
				}
			} catch (Exception e) {

			}
		}
		uri = FunctionUtil.uribase64(uri);
		js = "";
		for (int i = 0; i < 3; i++) {
			js = HttpUtil.iosGetHtml(uri);
			if (!StringUtil.isEmpty(js)) {
				i = 10;
			}
		}
		DataBetter result = FunctionUtil.dataselectbetterone(js, "BiliBili",
				context);
		errorcode = result.errorcode;
		if (errorcode >= 0) {
			return null;
		}
		title = result.title;
		return result.url;

	}

}
