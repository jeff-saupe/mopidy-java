package danbroid.mopidy.app.fragments;

import android.database.ContentObserver;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import danbroid.mopidy.app.R;
import danbroid.mopidy.app.interfaces.ContentView;
import danbroid.mopidy.app.interfaces.MainView;
import danbroid.mopidy.model.Base;
import danbroid.mopidy.model.Ref;

/**
 * Created by dan on 10/12/17.
 */
@EFragment(R.layout.content_list)
public class ContentListFragment extends Fragment implements ContentView {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ContentListFragment.class);

	private static final String ARG_URI = "uri";

	@ViewById(R.id.recycler_view)
	RecyclerView recyclerView;

	@ViewById(R.id.spinner)
	View spinner;

	@ViewById(R.id.fab)
	View fab;

	@FragmentArg(ARG_URI)
	String uri;


	private RecyclerView.Adapter<MediaItemViewHolder> adapter;


	public void refresh() {
		MainView mainView = getMainView();
		if (mainView != null) mainView.browse(uri, this);
	}

	public MainView getMainView() {
		return (MainView) getActivity();
	}

	class MediaItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
		ImageView imageView;
		TextView titleView;
		TextView descriptionView;
		private Base item;

		public MediaItemViewHolder(ViewGroup itemView) {
			super(itemView);
			imageView = itemView.findViewById(R.id.icon);
			titleView = itemView.findViewById(R.id.title);
			descriptionView = itemView.findViewById(R.id.description);
			itemView.setOnClickListener(this);
		}

		void bind(Base item) {
			this.item = item;
			log.trace("bind(): {}", item);
			if (item instanceof Ref) {
				Ref ref = (Ref) item;
				titleView.setText(ref.getName());
				descriptionView.setText(ref.getType() + " " + ref.getUri());
			}
		}

		@Override
		public void onClick(View v) {
			getMainView().onItemSelected(item);
		}
	}

	private Base[] data = {};


	@AfterViews
	void init() {
		log.debug("init() :{}", uri);
		fab.setVisibility(View.GONE);

		adapter = new RecyclerView.Adapter<MediaItemViewHolder>() {
			@Override
			public long getItemId(int position) {
				return data[position].hashCode();
			}

			@Override
			public MediaItemViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
				return new MediaItemViewHolder(
						(ViewGroup) getLayoutInflater()
								.inflate(R.layout.media_list_item, viewGroup, false));
			}

			@Override
			public void onBindViewHolder(MediaItemViewHolder viewHolder, int position) {
				viewHolder.bind(data[position]);
			}

			@Override
			public int getItemCount() {
				return data.length;
			}
		};

		adapter.setHasStableIds(true);
		recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
		recyclerView.setAdapter(adapter);

		refresh();


	}


	public static ContentListFragment newInstance(String uri) {
		return ContentListFragment_.builder().arg(ARG_URI, uri).build();
	}

	@Click(R.id.fab)
	void fabClicked() {
		log.debug("fabClicked()");
	}

	@UiThread
	public void setContent(Base[] content) {
		log.debug("setContent(): size: " + content.length);
		this.data = content;

		spinner.setVisibility(data.length > 0 ? View.GONE : View.VISIBLE);
		recyclerView.setVisibility(data.length > 0 ? View.VISIBLE : View.GONE);

		adapter.notifyDataSetChanged();
	}


	private final ContentObserver contentObserver = new ContentObserver(null) {
		@Override
		public void onChange(boolean selfChange) {
			log.error("contentObserver.onChange()!");
			refresh();
		}
	};

	@Override
	public void onStart() {
		super.onStart();
		getContext().getContentResolver().registerContentObserver(Uri.parse(uri), false, contentObserver);
	}

	@Override
	public void onStop() {
		super.onStop();
		getContext().getContentResolver().unregisterContentObserver(contentObserver);

	}
}
