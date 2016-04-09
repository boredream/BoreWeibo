package com.boredream.boreweibo.widget;

import android.content.Context;
import android.util.AttributeSet;

import com.handmark.pulltorefresh.library.PullToRefreshListView;

public class Pull2RefreshListView extends PullToRefreshListView {

	public Pull2RefreshListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public Pull2RefreshListView(
			Context context,
			com.handmark.pulltorefresh.library.PullToRefreshBase.Mode mode,
			com.handmark.pulltorefresh.library.PullToRefreshBase.AnimationStyle style) {
		super(context, mode, style);
		// TODO Auto-generated constructor stub
	}

	public Pull2RefreshListView(Context context,
			com.handmark.pulltorefresh.library.PullToRefreshBase.Mode mode) {
		super(context, mode);
		// TODO Auto-generated constructor stub
	}

	public Pull2RefreshListView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}


	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		super.onScrollChanged(l, t, oldl, oldt);
		if(onPlvScrollListener != null) {
			onPlvScrollListener.onScrollChanged(l, t, oldl, oldt);
		}
	}
	
	private OnPlvScrollListener onPlvScrollListener;
	
	public void setOnPlvScrollListener(OnPlvScrollListener onPlvScrollListener) {
		this.onPlvScrollListener = onPlvScrollListener;
	}

	public static interface OnPlvScrollListener {
		void onScrollChanged(int l, int t, int oldl, int oldt);
	}
}
