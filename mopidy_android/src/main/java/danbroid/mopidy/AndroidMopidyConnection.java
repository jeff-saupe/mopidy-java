package danbroid.mopidy;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import danbroid.mopidy.api.Call;

/**
 * Created by dan on 18/12/17.
 */
public class AndroidMopidyConnection extends MopidyConnection {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AndroidMopidyConnection.class);
	private static final int MSG_CHECK_QUEUE = 1001;

	private final Handler handler = new Handler(Looper.getMainLooper()) {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case MSG_CHECK_QUEUE:
					expireCalls();
					break;
			}
		}
	};



	@Override
	public void call(final Call call) {
		if (onUIThread()) {
			sendCall(call);
			return;
		}

		handler.post(new Runnable() {
			@Override
			public void run() {
				sendCall(call);
			}
		});
	}

	@Override
	protected void sendCall(Call call) {
		handler.sendEmptyMessageDelayed(MSG_CHECK_QUEUE, getTimeout());
		super.sendCall(call);
	}

	static boolean onUIThread() {
		return Looper.myLooper() == Looper.getMainLooper();
	}

	@Override
	public void onMessage(final String text) {
		handler.post(new Runnable() {
			@Override
			public void run() {
				processMessage(text);
			}
		});
	}
}
