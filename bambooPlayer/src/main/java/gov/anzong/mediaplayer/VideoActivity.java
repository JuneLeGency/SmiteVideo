package gov.anzong.mediaplayer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Parcelable;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@SuppressLint("HandlerLeak")
public class VideoActivity extends Activity implements MediaController.MediaPlayerControl, VideoView.SurfaceCallback {

	public static final int RESULT_FAILED = -7;

	private Toast toast = null;

	private static final IntentFilter SCREEN_FILTER = new IntentFilter(Intent.ACTION_SCREEN_ON);
	private static final IntentFilter HEADSET_FILTER = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
	private static final IntentFilter BATTERY_FILTER = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);

	private boolean mCreated = false;
	private boolean mNeedLock;
	private String mDisplayName;
	private String mBatteryLevel;
	private boolean mFromStart;
	private int mLoopCount;
	private boolean mSaveUri;
	private int mParentId;
	private float mStartPos;
	private boolean mEnd = false;
	private String mSubPath;
	private boolean mSubShown;
	private View mViewRoot;
	private VideoView mVideoView;
	private View mVideoLoadingLayout;
	private TextView mVideoLoadingText;
	private View mSubtitleContainer;
	private OutlineTextView mSubtitleText;
	private ImageView mSubtitleImage;
	private Uri mUri;
	private ScreenReceiver mScreenReceiver;
	private HeadsetPlugReceiver mHeadsetPlugReceiver;
	private BatteryReceiver mBatteryReceiver;
	private boolean mReceiverRegistered = false;
	private boolean mHeadsetPlaying = false;
	private boolean mCloseComplete = false;
	private boolean mIsHWCodec = false;
	private float lastposition;

	private MediaController mMediaController;
	private PlayerService vPlayer;
	private ServiceConnection vPlayerServiceConnection;
	private Animation mLoadingAnimation;
	private View mLoadingProgressView;
	private ImageButton player_back;

	static {
		SCREEN_FILTER.addAction(Intent.ACTION_SCREEN_OFF);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		if (!io.vov.vitamio.LibsChecker.checkVitamioLibs(this))
			return;
		vPlayerServiceConnection = new ServiceConnection() {
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				vPlayer = ((PlayerService.LocalBinder) service).getService();
				mServiceConnected = true;
				if (mSurfaceCreated)
					vPlayerHandler.sendEmptyMessage(OPEN_FILE);
			}

			@Override
			public void onServiceDisconnected(ComponentName name) {
				vPlayer = null;
				mServiceConnected = false;
			}
		};

		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		parseIntent(getIntent());
		loadView(R.layout.activity_video);
		manageReceivers();

		mCreated = true;
	}

	private void attachMediaController() {
		if (mMediaController != null) {
			mNeedLock = mMediaController.isLocked();
			mMediaController.release();
		}
		mMediaController = new MediaController(this, mNeedLock);
		mMediaController.setMediaPlayer(this);
		mMediaController.setAnchorView(mVideoView.getRootView());
		setFileName();
		setBatteryLevel();
	}

	@Override
	public void onStart() {
		super.onStart();
        Log.e("Video","VideoStarted MY PID"+android.os.Process.myPid());
		if (!mCreated)
			return;
		Intent serviceIntent = new Intent(this, PlayerService.class);
		serviceIntent.putExtra("isHWCodec", mIsHWCodec);
		bindService(serviceIntent, vPlayerServiceConnection, Context.BIND_AUTO_CREATE);
	}

	@Override
	public void onResume() {
		super.onResume();
        Log.i("resume", "Video");
		if (!mCreated)
			return;
		if (isInitialized()) {
			KeyguardManager keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
			if (!keyguardManager.inKeyguardRestrictedInputMode()) {
				startPlayer();
			}
		} else {
			if (mCloseComplete) {
				reOpen();
			}
		}
	}

	@Override
	public void onPause() {
		super.onPause();
        Log.i("pause", "Video");
		if (!mCreated)
			return;
		if (isInitialized()) {
			savePosition();
			if (vPlayer != null && vPlayer.isPlaying()) {
				stopPlayer();
			}
		}
	}

	@Override
	public void onStop() {
		super.onStop();
        Log.i("stop", "Video");
		if (!mCreated)
			return;
		if (isInitialized()) {
			vPlayer.releaseSurface();
		}
		if (mServiceConnected) {
			unbindService(vPlayerServiceConnection);
			mServiceConnected = false;
		}

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
        Log.i("destory","Video");
		if (!mCreated)
			return;
		manageReceivers();
		if (isInitialized() && !vPlayer.isPlaying())
			release();
		if (mMediaController != null)
			mMediaController.release();
		try {
				android.os.Process.killProcess(android.os.Process.myPid());
		}catch(Exception e){
			}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		if (isInitialized()) {
			setVideoLayout();
			attachMediaController();
		}

		super.onConfigurationChanged(newConfig);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// http://code.google.com/p/android/issues/detail?id=19917
		outState.putString("WORKAROUND_FOR_BUG_19917_KEY", "WORKAROUND_FOR_BUG_19917_VALUE");
		super.onSaveInstanceState(outState);
	}

	public void showMenu() {

	}

	public static void openVideo(Context context, Uri uri, String title) {
		Intent intent = new Intent(context, VideoActivity.class);
		intent.setData(uri);
		intent.putExtra("displayName", title);
		context.startActivity(intent);
	}

	private void loadView(int id) {
		setContentView(id);
		getWindow().setBackgroundDrawable(null);
		mViewRoot = findViewById(R.id.video_root);
		mVideoView = (VideoView) findViewById(R.id.video);
		mVideoView.initialize(this, this, mIsHWCodec);
		mSubtitleContainer = findViewById(R.id.subtitle_container);
		mSubtitleText = (OutlineTextView) findViewById(R.id.subtitle_text);
		mSubtitleImage = (ImageView) findViewById(R.id.subtitle_image);
		mVideoLoadingText = (TextView) findViewById(R.id.video_loading_text);
		mVideoLoadingLayout = findViewById(R.id.video_loading);
		mLoadingProgressView = mVideoLoadingLayout.findViewById(R.id.video_loading_progress);
		player_back = (ImageButton) findViewById(R.id.player_back);
		player_back.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}

		});
		mLoadingAnimation = AnimationUtils.loadAnimation(VideoActivity.this, R.anim.loading_rotate);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}
	public static Uri getIntentUri(Intent intent) {
		Uri result = null;
		if (intent != null) {
			result = intent.getData();
			if (result == null) {
				final String type = intent.getType();
				String sharedUrl = intent.getStringExtra(Intent.EXTRA_TEXT);
				if (!TextUtils.isEmpty(sharedUrl)) {
					if ("text/plain".equals(type) && sharedUrl != null) {
						result = getTextUri(sharedUrl);
					} else if ("text/html".equals(type) && sharedUrl != null) {
						result = getTextUri(Html.fromHtml(sharedUrl).toString());
					}
				} else {
					Parcelable parce = intent.getParcelableExtra(Intent.EXTRA_STREAM);
					if (parce != null)
						result = (Uri) parce;
				}
			}
		}
		return result;
	}

	private static Uri getTextUri(String sharedUrl) {
		Matcher matcher = Pattern.compile("(http[s]?://)+([\\w-]+\\.)+[\\w-]+([\\w-./?%&=]*)?").matcher(sharedUrl);
		if (matcher.find()) {
			sharedUrl = matcher.group();
			if (!TextUtils.isEmpty(sharedUrl)) {
				return Uri.parse(sharedUrl);
			}
		}
		return null;
	}
	private void parseIntent(Intent i) {

		Uri dat = getIntentUri(i);
		if (dat == null)
			resultFinish(RESULT_FAILED);

		String datString = dat.toString();
		if (!datString.equals(dat.toString()))
			dat = Uri.parse(datString);

		mUri = dat;

		mNeedLock = i.getBooleanExtra("lockScreen", false);
		mDisplayName = i.getStringExtra("displayName");
		mFromStart = i.getBooleanExtra("fromStart", false);
		mSaveUri = i.getBooleanExtra("saveUri", true);
		mStartPos = i.getFloatExtra("startPosition", -1.0f);
		mLoopCount = i.getIntExtra("loopCount", 1);
		mParentId = i.getIntExtra("parentId", 0);
		mSubPath = i.getStringExtra("subPath");
		mSubShown = i.getBooleanExtra("subShown", true);
		mIsHWCodec = i.getBooleanExtra("hwCodec", false);
	}

	private void manageReceivers() {
		if (!mReceiverRegistered) {
			mScreenReceiver = new ScreenReceiver();
			registerReceiver(mScreenReceiver, SCREEN_FILTER);
			mBatteryReceiver = new BatteryReceiver();
			registerReceiver(mBatteryReceiver, BATTERY_FILTER);
			mHeadsetPlugReceiver = new HeadsetPlugReceiver();
			registerReceiver(mHeadsetPlugReceiver, HEADSET_FILTER);
			mReceiverRegistered = true;
		} else {
			try {
				if (mScreenReceiver != null)
					unregisterReceiver(mScreenReceiver);
				if (mHeadsetPlugReceiver != null)
					unregisterReceiver(mHeadsetPlugReceiver);
				if (mBatteryReceiver != null)
					unregisterReceiver(mBatteryReceiver);
			} catch (IllegalArgumentException e) {
			}
			mReceiverRegistered = false;
		}
	}

	public static String getName(String uri) {
		String path = getPath(uri);
		if (path != null)
			return new File(path).getName();
		return null;
	}
	public static String getPath(String uri) {
		if (TextUtils.isEmpty(uri))
			return null;
		if (uri.startsWith("file://") && uri.length() > 7)
			return Uri.decode(uri.substring(7));
		return Uri.decode(uri);
	}


	private void setFileName() {
		if (mUri != null) {
			String name = null;
			if (mUri.getScheme() == null || mUri.getScheme().equals("file"))
				name = getName(mUri.toString());
			else
				name = mUri.getLastPathSegment();
			if (name == null)
				name = "null";
			if (mDisplayName == null)
				mDisplayName = name;
			mMediaController.setFileName(mDisplayName);
		}
	}

	private void applyResult(int resultCode) {
		vPlayerHandler.removeMessages(BUFFER_PROGRESS);
		Intent i = new Intent();
		i.putExtra("filePath", mUri.toString());
		if (isInitialized()) {
			i.putExtra("position", (double) vPlayer.getCurrentPosition() / vPlayer.getDuration());
			i.putExtra("duration", vPlayer.getDuration());
			savePosition();
		}
		switch (resultCode) {
		case RESULT_FAILED:
			if (toast != null) {
				toast.setText(R.string.video_cannot_play);
				toast.setDuration(Toast.LENGTH_SHORT);
				toast.show();
			} else {
				toast = Toast.makeText(this,R.string.video_cannot_play,
						Toast.LENGTH_SHORT);
				toast.show();
			}
			break;
		case RESULT_CANCELED:
		case RESULT_OK:
			break;
		}
		setResult(resultCode, i);
	}

	private void resultFinish(int resultCode) {
		applyResult(resultCode);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH && resultCode != RESULT_FAILED) {
            Log.e("result","kill");
			android.os.Process.killProcess(android.os.Process.myPid());
		} else {
			finish();
		}
	}

	private void release() {
		if (vPlayer != null) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
				android.os.Process.killProcess(android.os.Process.myPid());
			} else {
				vPlayer.release();
				vPlayer.releaseContext();
			}
		}
	}

	private void reOpen(Uri path, String name, boolean fromStart) {
		if (isInitialized()) {
			savePosition();
			vPlayer.release();
			vPlayer.releaseContext();
		}
		Intent i = getIntent();
		i.putExtra("lockScreen", mMediaController.isLocked());
		i.putExtra("startPosition", lastposition);
		i.putExtra("fromStart", fromStart);
		i.putExtra("displayName", name);
		i.setData(path);
		parseIntent(i);
		mUri = path;
		if (mViewRoot != null)
			mViewRoot.invalidate();
		mOpened.set(false);
	}

	public void reOpen() {
		reOpen(mUri, mDisplayName, false);
	}

	protected void startPlayer() {
		if (isInitialized() && mScreenReceiver.screenOn && !vPlayer.isBuffering()) {
			if (!vPlayer.isPlaying()) {
				vPlayer.start();
			}
		}
	}

	protected void stopPlayer() {
		if (isInitialized()) {
			vPlayer.stop();
		}
	}

	private void setBatteryLevel() {
		if (mMediaController != null)
			mMediaController.setBatteryLevel(mBatteryLevel);
	}

	private class BatteryReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
			int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100);
			int percent = scale > 0 ? level * 100 / scale : 0;
			if (percent > 100)
				percent = 100;
			mBatteryLevel = String.valueOf(percent) + "%";
			setBatteryLevel();
		}
	}

	public class HeadsetPlugReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent != null && intent.hasExtra("state")) {
				int state = intent.getIntExtra("state", -1);
				if (state == 0) {
					mHeadsetPlaying = isPlaying();
					stopPlayer();
				} else if (state == 1) {
					if (mHeadsetPlaying)
						startPlayer();
				}
			}
		};
	}

	private class ScreenReceiver extends BroadcastReceiver {
		private boolean screenOn = true;

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
				screenOn = false;
				stopPlayer();
			} else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
				screenOn = true;
			}
		}
	}

	private void loadVPlayerPrefs() {
		if (!isInitialized())
			return;
		vPlayer.setBuffer(VP.DEFAULT_BUF_SIZE);
		vPlayer.setVideoQuality(VP.DEFAULT_VIDEO_QUALITY);
		vPlayer.setDeinterlace(VP.DEFAULT_DEINTERLACE);
		vPlayer.setVolume(VP.DEFAULT_STEREO_VOLUME, VP.DEFAULT_STEREO_VOLUME);
		vPlayer.setSubEncoding(VP.DEFAULT_SUB_ENCODING);
		MarginLayoutParams lp = (MarginLayoutParams) mSubtitleContainer.getLayoutParams();
		lp.bottomMargin = (int) VP.DEFAULT_SUB_POS;
		mSubtitleContainer.setLayoutParams(lp);
		vPlayer.setSubShown(mSubShown);
		setTextViewStyle(mSubtitleText);
		if (!TextUtils.isEmpty(mSubPath))
			vPlayer.setSubPath(mSubPath);
		if (mVideoView != null && isInitialized())
			setVideoLayout();
	}

	private void setTextViewStyle(OutlineTextView v) {
		v.setTextColor(VP.DEFAULT_SUB_COLOR);
		v.setTypeface(VP.getTypeface(VP.DEFAULT_TYPEFACE_INT), VP.DEFAULT_SUB_STYLE);
		v.setShadowLayer(VP.DEFAULT_SUB_SHADOWRADIUS, 0, 0, VP.DEFAULT_SUB_SHADOWCOLOR);
	}

	private boolean isInitialized() {
		return (mCreated && vPlayer != null && vPlayer.isInitialized());
	}

	private Handler mSubHandler = new Handler() {
		Bundle data;
		String text;
		byte[] pixels;
		int width = 0, height = 0;
		Bitmap bm = null;
		int oldType = SUBTITLE_TEXT;

		@Override
		public void handleMessage(Message msg) {
			data = msg.getData();
			switch (msg.what) {
			case SUBTITLE_TEXT:
				if (oldType != SUBTITLE_TEXT) {
					mSubtitleImage.setVisibility(View.GONE);
					mSubtitleText.setVisibility(View.VISIBLE);
					oldType = SUBTITLE_TEXT;
				}
				text = data.getString(VP.SUB_TEXT_KEY);
				mSubtitleText.setText(text == null ? "" : text.trim());
				break;
			case SUBTITLE_BITMAP:
				if (oldType != SUBTITLE_BITMAP) {
					mSubtitleText.setVisibility(View.GONE);
					mSubtitleImage.setVisibility(View.VISIBLE);
					oldType = SUBTITLE_BITMAP;
				}
				pixels = data.getByteArray(VP.SUB_PIXELS_KEY);
				if (bm == null || width != data.getInt(VP.SUB_WIDTH_KEY) || height != data.getInt(VP.SUB_HEIGHT_KEY)) {
					width = data.getInt(VP.SUB_WIDTH_KEY);
					height = data.getInt(VP.SUB_HEIGHT_KEY);
					bm = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
				}
				if (pixels != null)
					bm.copyPixelsFromBuffer(ByteBuffer.wrap(pixels));
				mSubtitleImage.setImageBitmap(bm);
				break;
			}
		}
	};

	private AtomicBoolean mOpened = new AtomicBoolean(Boolean.FALSE);
	private boolean mSurfaceCreated = false;
	private boolean mServiceConnected = false;
	private Object mOpenLock = new Object();
	private static final int OPEN_FILE = 0;
	private static final int OPEN_START = 1;
	private static final int OPEN_SUCCESS = 2;
	private static final int OPEN_FAILED = 3;
	private static final int HW_FAILED = 4;
	private static final int LOAD_PREFS = 5;
	private static final int BUFFER_START = 11;
	private static final int BUFFER_PROGRESS = 12;
	private static final int BUFFER_COMPLETE = 13;
	private static final int CLOSE_START = 21;
	private static final int CLOSE_COMPLETE = 22;
	private static final int SUBTITLE_TEXT = 0;
	private static final int SUBTITLE_BITMAP = 1;
	private Handler vPlayerHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case OPEN_FILE:
                Log.i("Video","open");
				synchronized (mOpenLock) {
					if (!mOpened.get() && vPlayer != null) {
						mOpened.set(true);
						vPlayer.setVPlayerListener(vPlayerListener);
						if (vPlayer.isInitialized())
							mUri = vPlayer.getUri();

						if (mVideoView != null)
							vPlayer.setDisplay(mVideoView.getHolder());
						if (mUri != null)
							vPlayer.initialize(mUri, mDisplayName, mSaveUri, getStartPosition(), vPlayerListener, mParentId, mIsHWCodec);
					}
				}
				break;
			case OPEN_START:
				mVideoLoadingText.setText(R.string.video_layout_loading);
				setVideoLoadingLayoutVisibility(View.VISIBLE);
				break;
			case OPEN_SUCCESS:
				loadVPlayerPrefs();
				setVideoLoadingLayoutVisibility(View.GONE);
				setVideoLayout();
				vPlayer.start();
				attachMediaController();
				break;
			case OPEN_FAILED:
				resultFinish(RESULT_FAILED);
				break;
			case BUFFER_START:
				setVideoLoadingLayoutVisibility(View.VISIBLE);
				vPlayerHandler.sendEmptyMessageDelayed(BUFFER_PROGRESS, 1000);
				break;
			case BUFFER_PROGRESS:
				if (vPlayer.getBufferProgress() >= 100) {
					setVideoLoadingLayoutVisibility(View.GONE);
				} else {
					mVideoLoadingText.setText(getString(R.string.video_layout_buffering_progress, vPlayer.getBufferProgress()));
					vPlayerHandler.sendEmptyMessageDelayed(BUFFER_PROGRESS, 1000);
					stopPlayer();
				}
				break;
			case BUFFER_COMPLETE:
				setVideoLoadingLayoutVisibility(View.GONE);
				vPlayerHandler.removeMessages(BUFFER_PROGRESS);
				break;
			case CLOSE_START:
				mVideoLoadingText.setText(R.string.closing_file);
				setVideoLoadingLayoutVisibility(View.VISIBLE);
				break;
			case CLOSE_COMPLETE:
				mCloseComplete = true;
				break;
			case HW_FAILED:
				if (mVideoView != null) {
					mVideoView.setVisibility(View.GONE);
					mVideoView.setVisibility(View.VISIBLE);
					mVideoView.initialize(VideoActivity.this, VideoActivity.this, false);
				}
				break;
			case LOAD_PREFS:
				loadVPlayerPrefs();
				break;
			}
		}
	};

    public static boolean isNative(String uri) {
        uri = Uri.decode(uri);
        return uri != null && (uri.startsWith("/") || uri.startsWith("content:") || uri.startsWith("file:"));
    }

	private void setVideoLoadingLayoutVisibility(int visibility) {
		if (mVideoLoadingLayout != null && mLoadingProgressView != null) {
			if (visibility == View.VISIBLE)
				mLoadingProgressView.startAnimation(mLoadingAnimation);
			mVideoLoadingLayout.setVisibility(visibility);
		}
	}

	private PlayerService.VPlayerListener vPlayerListener = new PlayerService.VPlayerListener() {
		@Override
		public void onHWRenderFailed() {
			if (Build.VERSION.SDK_INT < 11 && mIsHWCodec) {
				vPlayerHandler.sendEmptyMessage(HW_FAILED);
				vPlayerHandler.sendEmptyMessageDelayed(HW_FAILED, 200);
			}
		}

		@Override
		public void onSubChanged(String sub) {
			Message msg = new Message();
			Bundle b = new Bundle();
			b.putString(VP.SUB_TEXT_KEY, sub);
			msg.setData(b);
			msg.what = SUBTITLE_TEXT;
			mSubHandler.sendMessage(msg);
		}

		@Override
		public void onSubChanged(byte[] pixels, int width, int height) {
			Message msg = new Message();
			Bundle b = new Bundle();
			b.putByteArray(VP.SUB_PIXELS_KEY, pixels);
			b.putInt(VP.SUB_WIDTH_KEY, width);
			b.putInt(VP.SUB_HEIGHT_KEY, height);
			msg.setData(b);
			msg.what = SUBTITLE_BITMAP;
			mSubHandler.sendMessage(msg);
		}

		@Override
		public void onOpenStart() {
			vPlayerHandler.sendEmptyMessage(OPEN_START);
		}

		@Override
		public void onOpenSuccess() {
			vPlayerHandler.sendEmptyMessage(OPEN_SUCCESS);
		}

		@Override
		public void onOpenFailed() {
			vPlayerHandler.sendEmptyMessage(OPEN_FAILED);
		}

		@Override
		public void onBufferStart() {
			vPlayerHandler.sendEmptyMessage(BUFFER_START);
			stopPlayer();
		}

		@Override
		public void onBufferComplete() {
			player_back.setVisibility(View.GONE);
			vPlayerHandler.sendEmptyMessage(BUFFER_COMPLETE);
			if (vPlayer != null && !vPlayer.needResume())
				startPlayer();
		}

		@Override
		public void onPlaybackComplete() {
			mEnd = true;
			if (mLoopCount == 0 || mLoopCount-- > 1) {
				vPlayer.start();
				vPlayer.seekTo(0);
			} else {
				resultFinish(RESULT_OK);
			}
		}

		@Override
		public void onCloseStart() {
			vPlayerHandler.sendEmptyMessage(CLOSE_START);
		}

		@Override
		public void onCloseComplete() {
			vPlayerHandler.sendEmptyMessage(CLOSE_COMPLETE);
		}

		@Override
		public void onVideoSizeChanged(int width, int height) {
			if (mVideoView != null) {
				setVideoLayout();
			}
		}

		@Override
		public void onDownloadRateChanged(int kbPerSec) {
			if (!isNative(mUri.toString()) && mMediaController != null) {
				mMediaController.setDownloadRate(String.format("%dKB/s", kbPerSec));
			}
		}

	};

	private int mVideoMode = VideoView.VIDEO_LAYOUT_SCALE;

	private void setVideoLayout() {
		mVideoView.setVideoLayout(mVideoMode, VP.DEFAULT_ASPECT_RATIO, vPlayer.getVideoWidth(), vPlayer.getVideoHeight(), vPlayer.getVideoAspectRatio());
	}

	private void savePosition() {
		if (vPlayer != null && mUri != null) {
			if (mEnd)
				lastposition=1.0f;
			else
				lastposition=(float) (vPlayer.getCurrentPosition() / (double) vPlayer.getDuration());
		}
	}

	private float getStartPosition() {
		if (mFromStart)
			return 1.1f;
		if (mStartPos <= 0.0f || mStartPos >= 1.0f)
			return lastposition;
		return mStartPos;
	}

	@Override
	public int getBufferPercentage() {
		if (isInitialized())
			return (int) (vPlayer.getBufferProgress() * 100);
		return 0;
	}

	@Override
	public long getCurrentPosition() {
		if (isInitialized())
			return vPlayer.getCurrentPosition();
		return (long) (getStartPosition() * vPlayer.getDuration());
	}

	@Override
	public long getDuration() {
		if (isInitialized())
			return vPlayer.getDuration();
		return 0;
	}

	@Override
	public boolean isPlaying() {
		if (isInitialized())
			return vPlayer.isPlaying();
		return false;
	}

	@Override
	public void pause() {
		if (isInitialized())
			vPlayer.stop();
	}

	@Override
	public void seekTo(long arg0) {
		if (isInitialized())
			vPlayer.seekTo((float) ((double) arg0 / vPlayer.getDuration()));
	}

	@Override
	public void start() {
		if (isInitialized())
			vPlayer.start();
	}

	@Override
	public void previous() {
	}

	@Override
	public void next() {
	}

	private static final int VIDEO_MAXIMUM_HEIGHT = 2048;
	private static final int VIDEO_MAXIMUM_WIDTH = 2048;

	@Override
	public float scale(float scaleFactor) {
		float userRatio = VP.DEFAULT_ASPECT_RATIO;
		int videoWidth = vPlayer.getVideoWidth();
		int videoHeight = vPlayer.getVideoHeight();
		float videoRatio = vPlayer.getVideoAspectRatio();
		float currentRatio = mVideoView.mVideoHeight / (float) videoHeight;

		currentRatio += (scaleFactor - 1);
		if (videoWidth * currentRatio >= VIDEO_MAXIMUM_WIDTH)
			currentRatio = VIDEO_MAXIMUM_WIDTH / (float) videoWidth;

		if (videoHeight * currentRatio >= VIDEO_MAXIMUM_HEIGHT)
			currentRatio = VIDEO_MAXIMUM_HEIGHT / (float) videoHeight;

		if (currentRatio < 0.5f)
			currentRatio = 0.5f;

		mVideoView.mVideoHeight = (int) (videoHeight * currentRatio);
		mVideoView.setVideoLayout(mVideoMode, userRatio, videoWidth, videoHeight, videoRatio);
		return currentRatio;
	}

	@Override
	public void toggleVideoMode(int mode) {
		mVideoMode = mode;
		setVideoLayout();
	}

	@Override
	public void stop(int pressbacktime) {
		if(pressbacktime<2){
			if (toast != null) {
				toast.setText(R.string.anotherback_finish);
				toast.setDuration(Toast.LENGTH_SHORT);
				toast.show();
			} else {
				toast = Toast.makeText(this,R.string.anotherback_finish,
						Toast.LENGTH_SHORT);
				toast.show();
			}
		}else{
			onBackPressed();
		}
	}

	@Override
	public long goForward() {
		return 0;
	}

	@Override
	public long goBack() {
		return 0;
	}

	@Override
	public void removeLoadingView() {
		mVideoLoadingLayout.setVisibility(View.GONE);
		player_back.setVisibility(View.GONE);
	}

	@Override
	public void onSurfaceCreated(SurfaceHolder holder) {
		mSurfaceCreated = true;
		if (mServiceConnected)
			vPlayerHandler.sendEmptyMessage(OPEN_FILE);
		if (vPlayer != null)
			vPlayer.setDisplay(holder);
	}

	@Override
	public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		if (vPlayer != null) {
			setVideoLayout();
		}
	}

	@Override
	public void onSurfaceDestroyed(SurfaceHolder holder) {
		if (vPlayer != null && vPlayer.isInitialized()) {
			if (vPlayer.isPlaying()) {
				vPlayer.stop();
				vPlayer.setState(PlayerService.STATE_NEED_RESUME);
			}
			vPlayer.releaseSurface();
			if (vPlayer.needResume())
				vPlayer.start();
		}
	}

	@Override
	public void setVideoQuality(int quality) {
		// TODO Auto-generated method stub
		if (isInitialized())
			vPlayer.setVideoQuality(quality);
	}

}
