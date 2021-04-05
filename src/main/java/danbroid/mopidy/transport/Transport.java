package danbroid.mopidy.transport;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Transport {
	private static final Logger log = LoggerFactory.getLogger(Transport.class);

	public static final int ERROR_CLOSE_CALLED = 1001;

	@Getter
	protected String url;
	protected Callback callback;

	@Getter
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

}