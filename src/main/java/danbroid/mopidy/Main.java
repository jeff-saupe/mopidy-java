package danbroid.mopidy;

import com.google.gson.JsonObject;
import danbroid.mopidy.interfaces.EventListener;
import danbroid.mopidy.interfaces.PlaybackState;
import danbroid.mopidy.model.TlTrack;
import danbroid.mopidy.model.Track;

import java.util.Arrays;

public class Main {

    public static void main(String[] args) throws InterruptedException {
        org.apache.log4j.BasicConfigurator.configure();

        MopidyClient client = new MopidyClient("192.168.0.5", 6680);
        client.connect();

        client.getCore().getTracklist().clear().call(new ResponseHandler<Void>() {
            @Override
            public void onResponse(Void result) {
                System.out.println("Cleared");
            }
        });
        client.getCore().getTracklist().add("spotify:track:58AShCtZlunqt40w2Guhp5").call(new ResponseHandler<TlTrack[]>() {
            @Override
            public void onResponse(TlTrack[] result) {
                if (result != null)
                    System.out.println("Added");
            }
        });
        client.getCore().getPlayback().play(1).call(new ResponseHandler<Void>() {
            @Override
            public void onResponse(Void result) {
                System.out.println("Playing");
            }
        });

        client.getCore().getPlayback().getCurrentTrack().call(new ResponseHandler<Track>() {
            @Override
            public void onResponse(Track result) {
                if (result != null)
                    System.out.println(result.getName());
            }
        });


        client.addEventListener(new EventListener() {
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
        });

        client.close();
    }
}
