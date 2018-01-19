package danbroid.mopidy.transport;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

/**
 * Created by dan on 21/12/17.
 */
public class WebSocketTransport extends Transport {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(WebSocketTransport.class);

	private WebSocket socket;

	@Override
	public void send(String request) {
		socket.send(request);
	}


	class LoggingInterceptor implements Interceptor {
		@Override
		public Response intercept(Interceptor.Chain chain) throws IOException {
			Request request = chain.request();

			long t1 = System.nanoTime();
			log.info(String.format("Sending request %s on %s%n%s",
					request.url(), chain.connection(), request.headers()));

			Response response = chain.proceed(request);

			long t2 = System.nanoTime();
			log.info(String.format("Received response for %s in %.1fms%n%s",
					response.request().url(), (t2 - t1) / 1e6d, response.headers()));

			return response;
		}
	}

	@Override
	protected synchronized void open() {
		if (socket != null) throw new IllegalArgumentException("socket already exists");

		OkHttpClient client = new OkHttpClient.Builder()
				.addInterceptor(new LoggingInterceptor())
				.readTimeout(5, TimeUnit.SECONDS)
				.retryOnConnectionFailure(true)
				.connectTimeout(5, TimeUnit.SECONDS).build();

		Request request = new Request.Builder()
				.url(url)
				.addHeader("Accept-Encoding", "gzip")
				.addHeader("Sec-WebSocket-Extensions","permessage-deflate")
				.build();

		this.socket = client.newWebSocket(request, new WebSocketListener() {

			@Override
			public void onFailure(WebSocket webSocket, Throwable t, Response response) {
				callback.onError(t);
			}

			@Override
			public void onMessage(WebSocket webSocket, String text) {
				callback.onMessage(text);
			}
		});

	}


	@Override
	public synchronized void close() {
		if (socket == null) return;
		socket.close(ERROR_CLOSE_CALLED, "close() called");
		socket = null;
	}


}
