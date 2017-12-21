package danbroid.mopidy.api;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import danbroid.mopidy.MopidyConnection;
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
	private final MopidyConnection connection;

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


	/**
	 * When this call was dispatched
	 */
	private long timestamp;

	protected ResponseHandler<T> handler;
	private int id;

	public Call(String method, MopidyConnection connection) {
		this.connection = connection;

		request = new JsonObject();
		request.addProperty(Constants.Key.METHOD, method);
		request.addProperty(Constants.Key.JSONRPC, JSONRPC_VERSION);
		params = new JsonObject();
		request.add(Constants.Key.PARAMS, params);
	}

	public Call<T> setResultType(TypeToken<T> resultType) {
		this.resultType = resultType;
		return this;
	}

	public Call<T> setResultType(Class<T> resultType) {
		return setResultType(TypeToken.get(resultType));
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
		if (resultType == null || resultType.getRawType().isAssignableFrom(Void.class))
			return null;
		return callContext.getGson().fromJson(response, resultType.getType());
	}


	public Call<T> setResponseHandler(ResponseHandler<T> handler) {
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


	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setID(int id) {
		getRequest().addProperty(Constants.Key.ID, id);
		this.id = id;
	}

	public int getID() {
		return id;
	}

	public void call(ResponseHandler<T> handler) {
		setResponseHandler(handler).call();
	}

	public void call() {
		connection.call(this);
	}
}
