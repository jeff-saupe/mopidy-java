package danbroid.mopidy;

import danbroid.mopidy.interfaces.CallContext;
import danbroid.mopidy.model.Track;

public class Main {

    public static void main(String[] args) {
        org.apache.log4j.BasicConfigurator.configure();

        MopidyConnection connection = new MopidyConnection();
        connection.setURL("192.168.178.60", 6680);
        connection.start();


        connection.getPlayback().getCurrentTrack().call(new ResponseHandler<Track>() {
            @Override
            public void onResponse(CallContext context, Track result) {

                System.out.println(result.getName());
            }
        });
    }

}
