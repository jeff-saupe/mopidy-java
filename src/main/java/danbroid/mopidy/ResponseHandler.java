package danbroid.mopidy;

import com.google.gson.JsonElement;

import danbroid.mopidy.interfaces.CallContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ResponseHandler<T> {
	private static final Logger log = LoggerFactory.getLogger(ResponseHandler.class);

	public abstract void onResponse(CallContext context, T result);

	public void onError(int code, String message, JsonElement data) {
		log.error("processError() code: " + code + " {}: data: {}", message, data);
	}
}