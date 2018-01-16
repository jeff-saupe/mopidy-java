package danbroid.mopidy.service;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ResultReceiver;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.session.MediaControllerCompat;

/**
 * Created by dan on 17/01/18.
 */
public class MopidyClient {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MopidyClient.class);


	public static class Call extends ResultReceiver {
		private final String command;
		private final Activity activity;
		private Bundle args;

		public Call(Activity activity, String command) {
			super(new Handler(Looper.getMainLooper()));
			this.command = command;
			this.activity = activity;
		}

		public void call() {
			MediaControllerCompat controller = getController();
			if (controller == null) {
				log.error("controller is null");
				return;
			}
			controller.sendCommand(command, getArgs(), this);
		}

		public Call setArg(String name, String value) {
			getArgs().putString(name, value);
			return this;
		}

		public Call setArg(String name, boolean value) {
			getArgs().putBoolean(name, value);
			return this;
		}

		public Call setArg(String name, int value) {
			getArgs().putInt(name, value);
			return this;
		}

		public Bundle getArgs() {
			return args == null ? args = new Bundle() : args;
		}

		@Override
		protected void onReceiveResult(int resultCode, Bundle resultData) {
			if (resultCode == MopidyBackend.RESULT_CODE_SUCCESS) {
				onSuccess(resultData);
			} else {
				onError(resultCode, resultData);
			}
		}

		protected void onError(int resultCode, Bundle resultData) {
			log.error("onError(): code: {} data: {}", resultCode, resultData);
		}

		protected void onSuccess(Bundle resultData) {
			log.debug("onSuccess(): {}", resultData);
		}

		public MediaControllerCompat getController() {
			return MediaControllerCompat.getMediaController(activity);
		}
	}

	public static class Connect extends Call {

		public Connect(Activity activity, String url) {
			super(activity, MopidyBackend.COMMAND_CONNECT);
			setArg(MopidyBackend.ARG_URL, url);
		}

		@Override
		protected void onSuccess(Bundle resultData) {
			onSuccess(resultData.getString(MopidyBackend.ARG_VERSION));
		}

		protected void onSuccess(String version) {
			log.debug("onSuccess() version: {}", version);
		}
	}

	public static class AddToTracklist extends Call {

		public AddToTracklist(Activity activity, MediaBrowserCompat.MediaItem item) {
			super(activity, MopidyBackend.COMMAND_ADD_TO_TRACKLIST);
			setArg(MopidyBackend.ARG_MEDIAID, item.getMediaId());
			setArg(MopidyBackend.ARG_PLAYABLE, item.isPlayable());
			replace(false);
			position(-1);
		}


		public AddToTracklist position(int position) {
			setArg(MopidyBackend.ARG_POSITION, position);
			return this;
		}

		public AddToTracklist replace(boolean replace) {
			setArg(MopidyBackend.ARG_REPLACE, replace);
			return this;
		}
	}


}
