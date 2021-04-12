package danbroid.mopidy;

import com.google.gson.JsonElement;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class ResponseHandler<T> {
	public abstract void onResponse(T result);

	public void onError(int code, String message, JsonElement data) {
		log.error("processError() code: " + code + " {}: data: {}", message, data);
	}
}