package danbroid.mopidy.transport;

/**
 * Created by dan on 21/12/17.
 */
public abstract class Transport {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Transport.class);
	public static final int ERROR_CLOSE_CALLED = 1001;

	protected String url;
	protected Callback callback;

	private boolean connected = false;

	protected Transport(Callback callback) {
		this.callback = callback;
	}

	public abstract void send(String request);

	/**
	 * Notifies the transport that if it supports reconnects then now is a good time.
	 */
	public void reconnect() {

	}

	public interface Callback {
		void onMessage(String message);

		void onError(Throwable t);

		void onConnected();

		void onDisconnected();
	}


	public Transport setCallback(Callback callback) {
		this.callback = callback;
		return this;
	}

	public final void connect(String url) {
		log.info("connect(): {}", url);
		this.url = url;
		open();
	}

	public String getUrl() {
		return url;
	}

	protected abstract void open();


	public abstract void close();

	protected void setConnected(boolean connected) {
		if (this.connected == connected) return;
		this.connected = connected;
		if (connected)
			callback.onConnected();
		else
			callback.onDisconnected();
	}

	public boolean isConnected() {
		return connected;
	}
}
