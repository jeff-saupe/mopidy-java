package danbroid.mopidy.api;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import danbroid.mopidy.ResponseHandler;
import danbroid.mopidy.interfaces.CallContext;
import danbroid.mopidy.interfaces.Constants;

/**
 * Created by dan on 8/12/17.
 */
public class Call<T> {
	public static final String JSONRPC_VERSION = "2.0";

	private static final org.slf4j.Logger
			log = org.slf4j.LoggerFactory.getLogger(Call.class);

	/**
	 * The Java type of the result field of the response
	 */
	private TypeToken<T> resultType;


	/**
	 * The request data
	 */
	private JsonObject request;

	/**
	 * The params field of the request
	 */
	private JsonObject params;

	protected ResponseHandler<T> handler;


	public Call(String method) {
		this(method, (TypeToken<T>) null);
	}

	public Call(String method, Class<T> resultClass) {
		this(method, TypeToken.get(resultClass));
	}

	public Call(String method, TypeToken<T> resultType) {
		this.resultType = resultType;
		request = new JsonObject();
		request.addProperty(Constants.Key.METHOD, method);
		request.addProperty(Constants.Key.JSONRPC, JSONRPC_VERSION);
		params = new JsonObject();
		request.add(Constants.Key.PARAMS, params);
	}

	public final void processResult(CallContext callContext, JsonElement response) {
		try {
			T result = parseResult(callContext, response);
			if (handler != null)
				handler.onResponse(callContext, result);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			if (handler != null)
				handler.onError(0, e.getMessage(), response);
		}
	}

	public void onError(int code, String message, JsonElement data) {
		if (handler != null)
			handler.onError(code, message, data);
		else
			log.error("code: " + code + " message: " + message + " data: " + data);
	}

	public JsonObject getRequest() {
		return request;
	}

	@Override
	public String toString() {
		return request.toString();
	}

	protected T parseResult(CallContext callContext, JsonElement response) {
		if (resultType == null)
			throw new IllegalArgumentException("resultType not provided. You should override this method");

		return callContext.getGson().fromJson(response, resultType.getType());
	}


	public Call<T> setHandler(ResponseHandler<T> handler) {
		this.handler = handler;
		return this;
	}

	public Call<T> addParam(String name, String value) {
		params.addProperty(name, value);
		return this;
	}

	public Call<T> addParam(String name, Boolean value) {
		params.addProperty(name, value);
		return this;
	}

	public Call<T> addParam(String name, Number value) {
		params.addProperty(name, value);
		return this;
	}

	public Call<T> addParam(String name, JsonElement value) {
		params.add(name, value);
		return this;
	}


}
