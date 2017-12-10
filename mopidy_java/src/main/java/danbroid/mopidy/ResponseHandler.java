package danbroid.mopidy;

import com.google.gson.JsonElement;

import danbroid.mopidy.interfaces.CallContext;

public abstract class ResponseHandler<T> {
	private static final org.slf4j.Logger
			log = org.slf4j.LoggerFactory.getLogger(ResponseHandler.class);

	public abstract void onResponse(CallContext context, T result);

	public void onError(int code, String message, JsonElement data) {
		log.error("onError() code: " + code + " {}: data: {}", message, data);
	}
}