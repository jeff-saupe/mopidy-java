package saupe.mopidy.api;

import lombok.extern.slf4j.Slf4j;

/*
 * See: https://github.com/mopidy/mopidy/blob/develop/mopidy/core/mixer.py
 */

@Slf4j
public class Mixer extends Api {
    protected Mixer(Api parent) {
        super(parent, "mixer.");
    }

    /**
     * Get the volume.
     *
     * @return Integer in range [0..100] or Null if unknown
     */
    public Call<Integer> getVolume() {
        return createCall("get_volume", Integer.class);
    }

    /**
     * Set the volume.
     *
     * @param volume Integer in range [1..100]
     * @return Returns True if call is successful, otherwise False
     */
    public Call<Boolean> setVolume(int volume) {
        return createCall("set_volume", Boolean.class)
                .addParam("volume", volume);
    }

    /**
     * Get mute state.
     *
     * @return True if muted, False if unmuted, Null if unknown
     */
    public Call<Boolean> getMute() {
        return createCall("get_mute", Boolean.class);
    }

    /**
     * Set mute state.
     *
     * @param mute True to mute, False to unmute.
     * @return True if call is successful, otherwise False.
     */
    public Call<Boolean> setMute(boolean mute) {
        return createCall("set_mute", Boolean.class)
                .addParam("mute", mute);
    }
}