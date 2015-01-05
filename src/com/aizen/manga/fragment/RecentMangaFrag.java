package com.aizen.manga.fragment;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import me.maxwin.view.XListView;
import me.maxwin.view.XListView.IXListViewListener;

import com.aizen.manga.DetailActivity;
import com.aizen.manga.R;
import com.aizen.manga.adapter.MangaListAdapter;
import com.aizen.manga.module.Manga;
import com.aizen.manga.sql.MangaDBManager;
import com.aizen.manga.util.InitViewListener;
import com.aizen.manga.util.NetAnalyse;
import com.aizen.manga.util.TimeSaveToSharePerference;
import com.nhaarman.listviewanimations.itemmanipulation.OnDismissCallback;
import com.nhaarman.listviewanimations.swinginadapters.prepared.SwingBottomInAnimationAdapter;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class RecentMangaFrag extends Fragment implements OnDismissCallback,
		IXListViewListener,InitViewListener {
	
	private static final String REFRESH_TIME_RECENT = "refresh_time_recent";
	MangaListAdapter mangasAdapter;
	SwingBottomInAnimationAdapter swingBottomInAnimationAdapter;
	ArrayList<Manga> mangas = new ArrayList<Manga>();
	XListView mangaListView;
	private Handler handler = new Handler();
	private ExecutorService executorService = Executors.newFixedThreadPool(10);
	private String FRAG_STRING_URL;
	private int page = 1;
	private RelativeLayout statusLayout;
	private ImageView statusImageView;
	private TextView statusTextView;
	private MangaDBManager db;
	// private ProgressDialog dialog;

	private static RecentMangaFrag uniqueRecentMangaFrag = null;
	private View rootView;

	public static RecentMangaFrag newInstance() {
		if (uniqueRecentMangaFrag == null) {
			uniqueRecentMangaFrag = new RecentMangaFrag();
		}
		return uniqueRecentMangaFrag;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
this.rootView = inflater.inflate(R.layout.fragment_manga_list,container, false);
		
		page = 1;
		this.db = MangaDBManager.getInstance(getActivity());
		
		initView();
		
		initData();
		
		return rootView;
	}

	/**
	 * 初始化组件
	 */
	@Override
	public void initView() {
		
		mangaListView = (XListView) rootView.findViewById(R.id.mangaList);
		mangaListView.setPullLoadEnable(true);
		mangaListView.setPullRefreshEnable(false);
		mangaListView.setXListViewListener(this);
		mangaListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// position位置从1开始是因为位置0被headview占用了
				Intent it = new Intent(getActivity(), DetailActivity.class);
				Bundle bundle = new Bundle();
				bundle.putString(MangaInfoFrag.MANGA_LINK_STRING,getActivity().getString(R.string.domain)+mangas.get(position - 1).getLink());
				it.putExtras(bundle); 
				startActivity(it);
				Toast.makeText(getActivity(),mangas.get(position - 1).getLink(), Toast.LENGTH_SHORT).show();
			}
		});
		
		statusLayout = (RelativeLayout) getActivity().findViewById(R.id.ReadDataStatusLayout);
		statusImageView = (ImageView) getActivity().findViewById(R.id.StatusImage);
		statusTextView = (TextView) getActivity().findViewById(R.id.StatusText);
	}

	/**
	 * 初始化数据
	 */
	@Override
	public void initData() {
		
		if(this.mangas == null){
			this.mangas = new ArrayList<Manga>();
		}
		mangas = this.db.queryAllMangas(REFRESH_TIME_RECENT);
		if(this.mangas.size() > 0){
			statusLayout.setVisibility(View.GONE);
		}
		
		int layoutID = R.layout.listview_mangalist;
		mangasAdapter = new MangaListAdapter(getActivity(), layoutID, mangas);
		swingBottomInAnimationAdapter = new SwingBottomInAnimationAdapter(mangasAdapter);
		swingBottomInAnimationAdapter.setInitialDelayMillis(300);
		swingBottomInAnimationAdapter.setAbsListView(mangaListView);
		mangaListView.setAdapter(swingBottomInAnimationAdapter);

		if(!TimeSaveToSharePerference.isRefresh(20, REFRESH_TIME_RECENT, getActivity()) && page == 1){
			return;
		}
		
		FRAG_STRING_URL = getActivity().getResources().getString(R.string.recent_manga_list);
		executorService.submit(new Runnable() {
			@Override
			public void run() {
				try {
					// mangas = null;
					// mangas.add(new Manga("", "naruto", "anben", "9.9",
					// "none"));
					refreshRecMangaList(page);
				} catch (Exception e) {
					somethingWrong();
					e.printStackTrace();
				}
			}
		});
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
//		statusLayout = (RelativeLayout) getActivity().findViewById(R.id.ReadDataStatusLayout);
//		statusImageView = (ImageView) getActivity().findViewById(R.id.StatusImage);
//		statusTextView = (TextView) getActivity().findViewById(R.id.StatusText);
	}

	public void refreshRecMangaList(int pagenum) throws Exception {
		System.out.println("网络请求");
		String recMangaListURL = FRAG_STRING_URL + "-p" + pagenum;
		final ArrayList<Manga> mangaDataList = NetAnalyse.parseHtmlToList(
				recMangaListURL, getActivity().getCacheDir().getAbsolutePath());
		
		//如果有
				if(mangaDataList.size() > 0 && page == 1){
					
					this.db.deleteAll();
					
					for(Manga m:mangaDataList){
						m.setType(REFRESH_TIME_RECENT);
						this.db.add(m);
					}
				}
		
		try {
			handler.post(new Runnable() {
				@Override
				public void run() {
					mangas.addAll(mangaDataList);
					mangasAdapter.notifyDataSetChanged();
					swingBottomInAnimationAdapter.notifyDataSetChanged();
					TimeSaveToSharePerference.setRefreshTime(REFRESH_TIME_RECENT, getActivity());
					// dialog.dismiss();
				}
			});
		} catch (Exception e) {
			somethingWrong();
			e.printStackTrace();
		}
	}

	@Override
	public void onDismiss(final AbsListView listView,
			final int[] reverseSortedPositions) {
		for (int position : reverseSortedPositions) {
			mangas.remove(position);
			mangasAdapter.notifyDataSetChanged();
		}
	}

	@Override
	public void onRefresh() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onLoadMore() {
		page++;
		executorService.submit(new Runnable() {
			@Override
			public void run() {
				try {
					System.out.println(page);
					refreshRecMangaList(page);
				} catch (Exception e) {
					e.printStackTrace();
					somethingWrong();
				}
			}
		});
	}
	
	public void somethingWrong() {
		statusImageView.setImageDrawable(getResources().getDrawable(R.drawable.wrong));
		statusTextView.setText(getResources().getString(R.string.status_text_wrong));
		statusLayout.setVisibility(View.VISIBLE);
	}

}
