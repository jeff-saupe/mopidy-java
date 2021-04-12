package saupe.mopidy;

import com.google.gson.JsonObject;
import saupe.mopidy.api.Playback;
import saupe.mopidy.api.Tracklist;
import saupe.mopidy.events.EventListener;
import saupe.mopidy.model.PlaybackState;
import saupe.mopidy.model.TlTrack;
import saupe.mopidy.model.Track;

public class Main {

    private static boolean repeat;
    private static boolean single;

    public static void main(String[] args) throws InterruptedException {
        org.apache.log4j.BasicConfigurator.configure();

        MopidyClient client = new MopidyClient("192.168.0.5", 6680);
        client.connect();

        Tracklist tracklist = client.getCore().getTracklist();
        Playback playback = client.getCore().getPlayback();

        tracklist.getRepeat().call(new ResponseHandler<Boolean>() {
            @Override
            public void onResponse(Boolean result) {
                repeat = result;
            }
        });

        tracklist.getSingle().call(new ResponseHandler<Boolean>() {
            @Override
            public void onResponse(Boolean result) {
                single = result;
            }
        });

        tracklist.setRepeat(true).call();
        tracklist.setSingle(true).call();

        tracklist.clear().call(new ResponseHandler<Void>() {
            @Override
            public void onResponse(Void result) {
                System.out.println("Cleared");
            }
        });
        tracklist.add("spotify:track:58AShCtZlunqt40w2Guhp5").call(new ResponseHandler<TlTrack[]>() {
            @Override
            public void onResponse(TlTrack[] result) {
                if (result != null)
                    System.out.println("Added");
            }
        });
        playback.play(1).call(new ResponseHandler<Void>() {
            @Override
            public void onResponse(Void result) {
                System.out.println("Playing");
            }
        });
        playback.getCurrentTrack().call(new ResponseHandler<Track>() {
            @Override
            public void onResponse(Track result) {
                if (result != null)
                    System.out.println(result.getName());
            }
        });


        client.registerListener(new EventListener() {
            @Override
            public void onEvent() {

            }

            @Override
            public void onTrackPlaybackPaused(JsonObject tl_track, long time_position) {

            }

            @Override
            public void onTrackPlaybackResumed(JsonObject tl_track, long time_position) {

            }

            @Override
            public void onTrackPlaybackStarted(JsonObject tl_track) {

            }

            @Override
            public void onTrackPlaybackEnded(JsonObject tl_track, long time_position) {

            }

            @Override
            public void onPlaybackStateChanged(PlaybackState oldState, PlaybackState newState) {
                System.out.println(String.format("From %S to %S", oldState.toString(), newState.toString()));
            }

            @Override
            public void onTracklistChanged() {

            }

            @Override
            public void onPlaylistsLoaded() {

            }

            @Override
            public void onPlaylistChanged(JsonObject playlist) {

            }

            @Override
            public void onPlaylistDeleted(String uri) {

            }

            @Override
            public void onOptionsChanged() {

            }

            @Override
            public void onVolumeChanged(int volume) {

            }

            @Override
            public void onMuteChanged(boolean mute) {

            }

            @Override
            public void onSeeked(long time_position) {

            }

            @Override
            public void onStreamTitleChanged(String title) {

            }
        });

        //client.close();
    }
}
