package danbroid.mopidy.app.fragments;

import android.net.nsd.NsdServiceInfo;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;

import danbroid.mopidy.app.R;
import danbroid.mopidy.app.content.ContentProvider;
import danbroid.mopidy.app.interfaces.MainView;
import danbroid.mopidy.app.util.MopidyServerDiscovery;
import danbroid.mopidy.model.Base;
import danbroid.mopidy.model.Ref;
import danbroid.mopidy.util.ServiceDiscoveryHelper;

/**
 * Created by dan on 10/12/17.
 */
@EFragment(R.layout.content_list)
public class ContentListFragment extends Fragment implements ServiceDiscoveryHelper.Listener {
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

	@Bean
	ContentProvider contentProvider;

	@Bean
	MopidyServerDiscovery serverDiscovery;

	private RecyclerView.Adapter<MediaItemViewHolder> adapter;

	@Override
	public void onServiceAdded(NsdServiceInfo serviceInfo) {
		refresh();
	}

	public void refresh() {
		contentProvider.browse(uri, this);
	}

	@Override
	public void onServiceRemoved(NsdServiceInfo serviceInfo) {
		refresh();
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
			((MainView) getActivity()).onItemSelected(item);
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


		contentProvider.browse(uri, this);

	}


	public static ContentListFragment newInstance(String uri) {
		return ContentListFragment_.builder().arg(ARG_URI, uri).build();
	}

	@Click(R.id.fab)
	void fabClicked() {
		log.debug("fabClicked()");
	}


	public void setContent(Base[] content) {
		log.debug("setContent(): size: " + content.length);
		this.data = content;

		spinner.setVisibility(data.length > 0 ? View.GONE : View.VISIBLE);
		recyclerView.setVisibility(data.length > 0 ? View.VISIBLE : View.GONE);

		adapter.notifyDataSetChanged();
	}

	@Override
	public void onResume() {
		super.onResume();
		if (ContentProvider.URI_SERVERS.equals(uri)) {
			serverDiscovery.addListener(this);
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		if (ContentProvider.URI_SERVERS.equals(uri)) {
			serverDiscovery.removeListener(this);
		}
	}
}
