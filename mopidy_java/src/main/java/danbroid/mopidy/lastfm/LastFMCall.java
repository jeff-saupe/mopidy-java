package danbroid.mopidy.lastfm;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.ResourceBundle;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Created by dan on 21/12/17.
 */
public class LastFMCall implements Callback {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LastFMCall.class);

	private static final String URL_BASE = "http://ws.audioscrobbler.com/2.0/";

	public static String API_KEY;

	static {
		try {
			API_KEY = ResourceBundle.getBundle("lastfm").getString("api_key");
		} catch (Exception e) {
			log.error("You must specify the lastfm api key as api_key in lastfm.properties");
			throw e;
		}
	}

	private static final HttpUrl URL = HttpUrl.parse(URL_BASE).newBuilder()
			.addQueryParameter("api_key", API_KEY)
			.build();

	public static OkHttpClient HTTP_CLIENT = new OkHttpClient();

	private final HttpUrl.Builder builder;
	private Call call;
	private Response response;

	protected LastFMCall(String method) {
		super();
		builder = URL.newBuilder()
				.addQueryParameter("method", method)
				.addQueryParameter("format", "json");
	}


	protected HttpUrl.Builder getBuilder() {
		return builder;
	}

	public LastFMCall callAsync() {
		buildCall().enqueue(this);
		return this;
	}

	public Response call() throws IOException {
		onResponse(call = buildCall(), call.execute());
		return response;
	}

	protected Call buildCall() {
		HttpUrl url = getBuilder().build();
		log.trace("call(): {}", url);
		call = HTTP_CLIENT.newCall(new Request.Builder().url(url).build());
		return call;
	}

	@Override
	public void onResponse(Call call, okhttp3.Response response) throws IOException {
		String text = response.body().string();
		log.trace("onResponse(): {}", text);
		danbroid.mopidy.lastfm.Response oResponse = getGson()
				.fromJson(text, danbroid.mopidy.lastfm.Response.class);
		onResponse(oResponse);
	}

	protected void onResponse(Response response) {
		this.response = response;
	}

	public void cancel() {
		if (call != null) {
			call.cancel();
			call = null;
		}
	}

	@Override
	public void onFailure(Call call, IOException e) {
		log.error(e.getMessage(), e);
	}

	public Gson getGson() {
		return new GsonBuilder().create();
	}
}
