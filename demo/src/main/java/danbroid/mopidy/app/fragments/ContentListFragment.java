package danbroid.mopidy.app.fragments;

import android.database.ContentObserver;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import danbroid.mopidy.ResponseHandler;
import danbroid.mopidy.app.R;
import danbroid.mopidy.app.content.ContentProvider;
import danbroid.mopidy.app.interfaces.ContentView;
import danbroid.mopidy.app.interfaces.MainView;
import danbroid.mopidy.app.util.GlideApp;
import danbroid.mopidy.app.util.ImageResolver;
import danbroid.mopidy.interfaces.CallContext;
import danbroid.mopidy.model.Image;
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


	@FragmentArg(ARG_URI)
	String uri;

	@Bean
	ImageResolver imageResolver;

	@Bean
	ContentProvider contentProvider;


	private RecyclerView.Adapter<MediaItemViewHolder> adapter;

	@Override
	public void refresh() {
		log.debug("refresh()");
		MainView mainView = getMainView();
		if (mainView != null) mainView.browse(uri, this);
	}

	public MainView getMainView() {
		return (MainView) getActivity();
	}

	class MediaItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
		ImageView imageView;
		TextView titleView;
		TextView descriptionView;
		private Ref ref;

		public MediaItemViewHolder(ViewGroup itemView) {
			super(itemView);
			imageView = itemView.findViewById(R.id.icon);
			titleView = itemView.findViewById(R.id.title);
			descriptionView = itemView.findViewById(R.id.description);
			itemView.setOnClickListener(this);
			imageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_folder));
			itemView.setOnLongClickListener(this);
		}

		void bind(Ref ref) {
			log.trace("bind(): {}", ref);
			this.ref = ref;
			titleView.setText(ref.getName());
			String description = ref.getUri();
			descriptionView.setText(description);
			Image image = (Image) ref.getExtra();
			if (image != null) {
				String imageUri = image.getUri();
				GlideApp.with(ContentListFragment.this)
						.load(imageUri)
						.fallback(R.drawable.ic_folder)
						.transition(DrawableTransitionOptions.withCrossFade())
						.apply(RequestOptions.bitmapTransform(new RoundedCorners(8)))
						.into(imageView);
			}
		}

		@Override
		public void onClick(View v) {
			getMainView().onItemSelected(ref);
		}

		@Override
		public boolean onLongClick(View v) {
			getMainView().onItemLongClicked(ref,v);
			return true;
		}
	}

	@Override
	public String getUri() {
		return getArguments().getString(ARG_URI);
	}


	private Ref[] data = {};


	@AfterViews
	void init() {
		log.debug("init() :{}", uri);


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

		getActivity().setTitle(uri);
		refresh();


	}


	public static ContentListFragment newInstance(String uri) {
		return ContentListFragment_.builder().arg(ARG_URI, uri).build();
	}


	@UiThread
	public void setContent(Ref[] content) {
		setContent(content, true);
	}

	@UiThread
	public void setContent(final Ref content[], boolean resolveImages) {
		log.debug("setContent(): size: {} resolveImages: {}", content.length, resolveImages);
		this.data = content;

		if (getActivity() == null || !isResumed()) return;

		spinner.setVisibility(data.length > 0 ? View.GONE : View.VISIBLE);
		recyclerView.setVisibility(data.length > 0 ? View.VISIBLE : View.GONE);

		if (!resolveImages) {
			for (int i = 0; i < data.length; i++) {
				Image img = (Image) content[i].getExtra();
				if (img != null && img != ImageResolver.MISSING_IMAGE) {
					adapter.notifyItemChanged(i);
				}
			}
			return;
		}

		adapter.notifyDataSetChanged();


		imageResolver.resolveImages(data, new ResponseHandler<Ref[]>() {
			@Override
			public void onResponse(CallContext context, Ref[] result) {
				setContent(result, false);
			}
		});
	}


	private final ContentObserver contentObserver = new ContentObserver(null) {
		@Override
		public void onChange(boolean selfChange) {
			log.trace("contentObserver.onChange()!");
			refresh();
		}
	};

}
