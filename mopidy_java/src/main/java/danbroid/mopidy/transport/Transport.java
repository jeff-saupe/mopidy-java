package danbroid.mopidy.transport;

/**
 * Created by dan on 21/12/17.
 */
public abstract class Transport {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Transport.class);
	public static final int ERROR_CLOSE_CALLED = 1001;

	protected String url;
	protected Callback callback;

	public abstract void send(String request);

	public interface Callback {
		void onMessage(String message);

		void onError(Throwable t);
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


}
