package ru.atomofiron.regextool.adapters;

import android.content.Context;
import android.os.Parcelable;
import android.view.View;

import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import java.util.List;

import ru.atomofiron.regextool.R;

public class ViewPagerAdapter extends PagerAdapter {

	Context co;
	List<View> pages = null;

	public ViewPagerAdapter(Context co, List<View> pages){
		this.co = co;
		this.pages = pages;
	}

	@Override
	public Object instantiateItem(View collection, int position){
		View v = pages.get(position);
		((ViewPager) collection).addView(v, 0);
		return v;
	}

	@Override
	public void destroyItem(View collection, int position, Object view){
		((ViewPager) collection).removeView((View) view);
	}

	@Override
	public int getCount(){
		return pages.size();
	}

	@Override
	public boolean isViewFromObject(View view, Object object){
		return view.equals(object);
	}

	@Override
	public void finishUpdate(View arg0){
	}

	@Override
	public void restoreState(Parcelable arg0, ClassLoader arg1){
	}

	@Override
	public Parcelable saveState(){
		return null;
	}

	@Override
	public void startUpdate(View arg0){
	}

	public CharSequence getPageTitle(int position) {
		return co.getResources().getStringArray(R.array.tab_titles)[position];
	}
}
