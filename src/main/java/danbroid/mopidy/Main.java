package danbroid.mopidy;

import danbroid.mopidy.interfaces.CallContext;
import danbroid.mopidy.model.TlTrack;
import danbroid.mopidy.model.Track;

public class Main {

    public static void main(String[] args) {
        org.apache.log4j.BasicConfigurator.configure();

        MopidyConnection connection = new MopidyConnection();
        connection.setURL("192.168.178.60", 6680);
        connection.start();

        connection.getTracklist().clear().call(new ResponseHandler<Void>() {
            @Override
            public void onResponse(CallContext context, Void result) {
                System.out.println("Cleared");
            }
        });
        connection.getTracklist().add("spotify:track:6RIYfiZrX1ZkbDDOqOOGWM").call(new ResponseHandler<TlTrack[]>() {
            @Override
            public void onResponse(CallContext context, TlTrack[] result) {
                if (result != null)
                    System.out.println("added");
            }
        });
        connection.getPlayback().play(1).call(new ResponseHandler<Void>() {
            @Override
            public void onResponse(CallContext context, Void result) {
                System.out.println("Playing");
            }
        });

        connection.getPlayback().getCurrentTrack().call(new ResponseHandler<Track>() {
            @Override
            public void onResponse(CallContext context, Track result) {
                if (result != null)
                    System.out.println(result.getName());
            }
        });

    }

}
