package danbroid.mopidy.app.ui;


import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.sharedpreferences.Pref;

import java.util.Set;

import danbroid.mopidy.app.R;
import danbroid.mopidy.app.interfaces.MainPrefs_;
import danbroid.mopidy.app.interfaces.MainView;

/**
 * Created by dan on 11/12/17.
 */

@EBean
public class AddServerDialog {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AddServerDialog.class);


	@RootContext
	Context context;
	private AlertDialog dialog;


	TextView hostText;
	TextView portText;
	MainView mainView;

	@Pref
	MainPrefs_ prefs;

	public void show(final MainView mainView) {
		log.trace("show()");

		for (String s : prefs.servers().get()) {
			log.error("SERVER: {}", s);
		}

		this.mainView = mainView;
		this.dialog = new AlertDialog.Builder(context)
				.setTitle("Add Server")
				.setView(R.layout.add_server_dialog)
				.setPositiveButton(android.R.string.ok, null)
				.setNegativeButton(android.R.string.cancel, null)
				.create();

		dialog.setOnShowListener(new DialogInterface.OnShowListener() {

			@Override
			public void onShow(DialogInterface dialogInterface) {

				hostText = dialog.findViewById(R.id.host);
				portText = dialog.findViewById(R.id.port);

				Set<String> servers = prefs.servers().get();
				if (!servers.isEmpty()) {
					String server = servers.iterator().next();
					String parts[] = server.split(":");
					hostText.setText(parts[0]);
					portText.setText(parts[1]);
				}

				Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);

				button.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View view) {
						onSubmit();
					}
				});
			}
		});

		dialog.show();

	}


	public void onSubmit() {
		if (hostText.getText().length() == 0 || portText.getText().length() == 0) return;
		String hostname = hostText.getText().toString();
		int port = Integer.parseInt(portText.getText().toString());
		dialog.cancel();
		String addr = hostname + ":" + port;
		Set<String> servers = prefs.servers().get();
		if (!servers.contains(addr)) {
			servers.add(addr);
			prefs.edit().servers().put(servers).apply();
			mainView.getContent().refresh();
		}

	}
}
