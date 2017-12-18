package danbroid.mopidy.util;

import android.os.Handler;
import android.os.Looper;

import danbroid.mopidy.ResponseHandler;
import danbroid.mopidy.interfaces.CallContext;

/**
 * Created by dan on 14/12/17.
 */
public abstract class UIResponseHandler<T> extends ResponseHandler<T> {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(UIResponseHandler.class);

	private static final Handler HANDLER = new Handler(Looper.getMainLooper());

	public final void onResponse(final CallContext context, final T result) {
		HANDLER.post(new Runnable() {
			@Override
			public void run() {
				onUIResponse(context, result);
			}
		});
	}

	 public abstract void onUIResponse(CallContext context, T result);

}
