package danbroid.mopidy;

import com.google.gson.JsonElement;

import danbroid.mopidy.interfaces.CallContext;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Slf4j
public abstract class ResponseHandler<T> {
	public abstract void onResponse(CallContext context, T result);

	public void onError(int code, String message, JsonElement data) {
		log.error("processError() code: " + code + " {}: data: {}", message, data);
	}
}