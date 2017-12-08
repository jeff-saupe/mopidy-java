package danbroid.mopidy;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import danbroid.mopidy.interfaces.Constants;

/**
 * Created by dan on 8/12/17.
 */
public abstract class Call<T> {
	public static final String JSONRPC_VERSION = "2.0";


	private JsonElement response;
	private CallContext context;

	public final void processResult(CallContext context, JsonElement response) {
		this.context = context;
		this.response = response;
		result = parseResult(response);
		if (handler != null) handler.onResponse(this);
	}


	protected CallContext getContext() {
		return context;
	}

	public interface ResponseHandler<T> {
		void onResponse(Call<T> call);
	}

	String method;
	int requestID;
	protected T result;
	protected ResponseHandler<T> handler;


	public Call(String method) {
		this.method = method;
	}


	public JsonObject toJSON() {
		JsonObject request = new JsonObject();
		request.addProperty(Constants.Key.JSONRPC, JSONRPC_VERSION);
		request.addProperty(Constants.Key.ID, requestID);
		request.addProperty(Constants.Key.METHOD, method);
		return request;
	}


	@Override
	public String toString() {
		return toJSON().toString();
	}


	protected abstract T parseResult(JsonElement response);

	public T getResult() {
		return result;
	}

	public JsonElement getResponse() {
		return response;
	}

	public void setResult(T result) {
		this.result = result;
	}

	public Call<T> setHandler(ResponseHandler<T> handler) {
		this.handler = handler;
		return this;
	}
}
