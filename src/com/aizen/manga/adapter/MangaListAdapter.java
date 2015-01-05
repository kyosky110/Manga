package com.aizen.manga.adapter;

import java.io.File;
import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.aizen.manga.R;
import com.aizen.manga.module.Manga;
import com.aizen.manga.util.ImageManager;

public class MangaListAdapter extends BaseAdapter {

	private Context context;
	private ArrayList<Manga> mangaListItems;
	private LayoutInflater listContainer;
	private int resource;
	private String path;

	public final class ListItemView {
		public ImageView mangaCover;
		public TextView mangaName, mangaUpdateTo, mangaUpdateDate, mangaStatus;
		public RatingBar mangaMark;
	}

	
	public MangaListAdapter(Context context, int resource,ArrayList<Manga> mangaListItems) {
		this.listContainer = LayoutInflater.from(context);
		this.context = context;
		this.resource = resource;
		this.mangaListItems = mangaListItems;
		this.path = context.getCacheDir().getAbsolutePath(); 
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mangaListItems.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ListItemView listItemView = null;
		if (convertView==null) {
			listItemView = new ListItemView();
			convertView = listContainer.inflate(resource, null);
			
			listItemView.mangaCover = (ImageView)convertView.findViewById(R.id.mangaCover);
			listItemView.mangaName = (TextView)convertView.findViewById(R.id.mangaName);
			listItemView.mangaUpdateTo = (TextView)convertView.findViewById(R.id.mangaUpdateTo);
			listItemView.mangaMark = (RatingBar)convertView.findViewById(R.id.mangaMark);
			listItemView.mangaUpdateDate = (TextView)convertView.findViewById(R.id.mangaUpdateDate);
			listItemView.mangaStatus = (TextView)convertView.findViewById(R.id.mangaStatus);
		//	listItemView.mangaIsLike = (TextView)convertView.findViewById(R.id.mangaIsLike);
			
			convertView.setTag(listItemView);
		} else {
			listItemView = (ListItemView)convertView.getTag();
		}
		
		Manga m = mangaListItems.get(position);
		if(m == null){
			return convertView;
		}
		
		if(m.getCover() == null){
			try {
				listItemView.mangaCover.setImageBitmap(ImageManager.getBitmapFromURL(m.getCoverURL(),path));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}else{
			listItemView.mangaCover.setImageBitmap(m.getCover());
		}
		listItemView.mangaName.setText(m.getName());
		listItemView.mangaUpdateTo.setText(m.getUpdateto());
		listItemView.mangaMark.setRating(Float.parseFloat(m.getMark())/2);
		listItemView.mangaUpdateDate.setText(m.getUpdateDate());
		listItemView.mangaStatus.setText(m.isStatus()?"正在连载":"已完结");
		//listItemView.mangaIsLike.setText("喜欢");
		
		return convertView;
	}

}
