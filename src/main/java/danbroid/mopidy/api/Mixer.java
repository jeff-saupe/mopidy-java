package danbroid.mopidy.api;

import danbroid.mopidy.ResponseHandler;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Slf4j
public class Mixer extends Api {
	protected Mixer(Api parent) {
		super(parent, "mixer.");
	}

	/**
	 * Get the volume.
	 * @return Integer in range [0..100] or :class:`None` if unknown
	 */
	public Call<Integer> getVolume() {
		return createCall("get_volume", Integer.class);
	}

	/**
	 * Set the volume.
	 * @param volume Integer in range [1..100]
	 * @return Returns :class:`True` if call is successful, otherwise :class:`False`
	 */
	public Call<Boolean> setVolume(int volume) {
		return createCall("set_volume", Boolean.class)
				.addParam("volume", volume);
	}

	/**
	 * Get mute state.
	 * @return :class:`True` if muted, :class:`False` unmuted, :class:`None` if unknown
	 */
	public Call<Boolean> getMute() {
		return createCall("get_mute", Boolean.class);
	}

	/**
	 * Set mute state.
	 * @param mute :class:`True` to mute, :class:`False` to unmute.
	 * @param successHandler Handler
	 * @return :class:`True` if call is successful, otherwise :class:`False`.
	 */
	public Call<Boolean> setMute(boolean mute, ResponseHandler<Boolean> successHandler) {
		return createCall("set_mute", Boolean.class)
				.addParam("mute", mute);
	}
}