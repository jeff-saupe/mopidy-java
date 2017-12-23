package danbroid.mopidy.app.interfaces;

import android.net.Uri;

import danbroid.mopidy.model.Ref;

/**
 * Created by dan on 10/12/17.
 */
public interface ContentView {
	void setContent(Ref content[]);

	Uri getUri();

	void refresh();


}
