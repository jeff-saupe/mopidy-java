package danbroid.mopidy.activities;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ResultReceiver;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.session.MediaControllerCompat;

import danbroid.mopidy.service.MopidyBackend;

/**
 * Created by dan on 17/01/18.
 */
public class MopidyClient {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MopidyClient.class);

	private final Activity activity;
	private final Handler handler = new Handler(Looper.getMainLooper());

	public MopidyClient(Activity activity) {
		this.activity = activity;
	}

	public Call<Void> removeFromTracklist(MediaBrowserCompat.MediaItem items[]) {
		return new RemoveFromTracklist(items);
	}


	public class CallResultReceiver<T> {
		protected void onError(int code, String message, Exception error) {
			log.error("error code: " + code + " " + message, error);
		}
	}

	public class Call<T> extends CallResultReceiver<T> {
		private final String command;
		public final Class<? extends T> resultType;
		private Bundle args;
		private CallResultReceiver<T> resultReceiver = this;


		public Call(String command) {
			this(command, null);
		}

		public Call(String command, Class<? extends T> resultType) {
			super();
			this.command = command;
			this.resultType = resultType;
		}

		public void call() {
			MediaControllerCompat controller = getController();
			if (controller == null) {
				log.error("controller is null");
				return;
			}

			controller.sendCommand(command, getArgs(), new ResultReceiver(handler) {
				@Override
				protected void onReceiveResult(int resultCode, Bundle resultData) {
					Call.this.processResult(resultCode, resultData);
				}
			});
		}

		public Call<T> setResultReceiver(CallResultReceiver<T> resultReceiver) {
			this.resultReceiver = resultReceiver;
			return this;
		}

		protected void onSuccess(T result) {
			log.debug("onSuccess(): {}", result);
		}

		protected void onSuccess(Bundle resultData) {

			if (resultType == Void.class) {
				onSuccess((T) null);
				return;
			}

			if (resultData != null && resultData.containsKey("result")) {
				onSuccess((T) resultData.get("result"));
				return;
			}

			log.debug("onSuccess(): {}", resultData);
		}

		protected void processResult(int resultCode, Bundle resultData) {

			if (resultCode == MopidyBackend.RESULT_CODE_SUCCESS) {
				onSuccess(resultData);
			} else {
				String message = resultData.getString("message", null);
				Exception error = resultData.containsKey("error") ? (Exception) resultData.getSerializable("error") : null;
				onError(resultCode, message, error);
			}

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

		public Call setArg(String name, Long value) {
			getArgs().putLong(name, value);
			return this;
		}

		public Call items(MediaBrowserCompat.MediaItem items[]) {
			String ids[] = new String[items.length];
			for (int n = 0; n < ids.length; n++) {
				ids[n] = items[n].getMediaId();
			}
			getArgs().putStringArray(MopidyBackend.ARG_MEDIA_IDS, ids);
			return this;
		}

		public Bundle getArgs() {
			return args == null ? args = new Bundle() : args;
		}

	}

	private class Connect extends Call<String> {

		public Connect(String url) {
			super(MopidyBackend.COMMAND_CONNECT, String.class);
			setArg(MopidyBackend.ARG_URL, url);
		}
	}

	public MediaControllerCompat getController() {
		return MediaControllerCompat.getMediaController(activity);
	}

	public class AddToTracklist extends Call {

		public AddToTracklist(MediaBrowserCompat.MediaItem item) {
			super(MopidyBackend.COMMAND_ADD_TO_TRACKLIST);
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

	public class RemoveFromTracklist extends Call {

		public RemoveFromTracklist(MediaBrowserCompat.MediaItem items[]) {
			super(MopidyBackend.COMMAND_REMOVE_FROM_TRACKLIST);
			items(items);
		}

	}


	protected class ClearTracklist extends Call {
		public ClearTracklist() {
			super(MopidyBackend.COMMAND_TRACKLIST_CLEAR);
		}
	}

	public class ShuffleTracklist extends Call {

		public ShuffleTracklist() {
			super(MopidyBackend.COMMAND_SHUFFLE_PLAYLIST);
		}

		public ShuffleTracklist start(Long start) {
			setArg("start", start);
			return this;
		}

		public ShuffleTracklist end(Long end) {
			setArg("end", end);
			return this;
		}
	}

	/**
	 * @return The version of the server
	 */
	public Call<String> connect(String url) {
		return new Connect(url);
	}

	public ShuffleTracklist shuffleTracklist() {
		return new ShuffleTracklist();
	}

	public Call<Void> clearTracklist() {
		return new ClearTracklist();
	}

	public AddToTracklist addToTracklist(MediaBrowserCompat.MediaItem item) {
		return new AddToTracklist(item);
	}

	public Call<Void> replaceTracklist(MediaBrowserCompat.MediaItem item) {
		AddToTracklist addToTracklist = new AddToTracklist(item);
		return new ClearTracklist() {
			@Override
			protected void onSuccess(Object result) {
				log.error("tracklist cleared .. adding item: {}",item);
				addToTracklist.call();
			}
		};
	}


}
