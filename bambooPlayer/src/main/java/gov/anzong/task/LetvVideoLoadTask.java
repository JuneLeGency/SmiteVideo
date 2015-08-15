package gov.anzong.task;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import gov.anzong.mediaplayer.VideoActivity;
import gov.anzong.util.FunctionUtil;
import gov.anzong.util.HttpUtil;
import gov.anzong.util.StringUtil;

public class LetvVideoLoadTask extends AsyncTask<String, Integer, String> {

	final String origurl, from;
	String title = "";
	final FragmentActivity context;
	static final String dialogTag = "load";
	int errorcode = -1;// 0参数错误,1网络错误,2解析错误

	public LetvVideoLoadTask(String origurl, String from,
			FragmentActivity context) {
		super();
		this.origurl = origurl;
		this.context = context;
		this.from = from;
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
		int hd = FunctionUtil.getvideohdformodel(context);
		String format;
		switch (hd) {
		case 2:
			format = "super";
			break;
		case 1:
			format = "high";
			break;
		default:
			format = "normal";
			break;
		}
		uri = FunctionUtil.uriEncodeFlvcd(uri, format);
		if (StringUtil.isEmpty(uri)) {
			errorcode = 2;
			return null;
		}
		String htmlString = null;
		for (int i = 0; i < 3; i++) {
			htmlString = HttpUtil.iosGetHtml(uri);
			if (!StringUtil.isEmpty(htmlString)) {
				i = 10;
			}
		}
		if (StringUtil.isEmpty(htmlString)) {
			errorcode = 1;
			return null;
		}
		String flvadd = StringUtil.getStringBetween(htmlString, 0,
				"clipurl = \"", "\"").result;
		if (StringUtil.isEmpty(flvadd)) {
			errorcode = 2;
			return null;
		}
		title = StringUtil.getStringBetween(htmlString, 0, "cliptitle = \"",
				"\"").result;
		if (!StringUtil.isEmpty(title)) {
			title = getFileName(title) + " - " + from;
		} else {
			title = from;
		}
		return flvadd;
	}

	public String getFileName(String file) {
		int pointpos = file.lastIndexOf(".");
		if (pointpos <= 0) {
			return file;
		} else {
			return file.substring(0, pointpos);
		}
	}

}
