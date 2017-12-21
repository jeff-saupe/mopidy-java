package danbroid.mopidy.api;

import danbroid.mopidy.ResponseHandler;

/**
 * Created by dan on 13/12/17.
 * Implementation complete
 */
public class Mixer extends Api {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Mixer.class);

	protected Mixer(Api parent) {
		super(parent, "mixer.");
	}

	public Call<Boolean> getMute() {
		return createCall("get_mute", Boolean.class);
	}

	public Call<Boolean> setMute(boolean mute, ResponseHandler<Boolean> successHandler) {
		return createCall("set_mute", Boolean.class)
				.addParam("mute", mute);
	}

	//Integer in range [0..100] or :class:`None` if unknown.
	public Call<Integer> getVolume() {
		return createCall("get_volume", Integer.class);
	}

	/**
	 * @param volume integer [1..100] the volume to set
	 */
	public Call<Boolean> setVolume(int volume) {
		return createCall("set_volume", Boolean.class)
				.addParam("volume", volume);

	}
}