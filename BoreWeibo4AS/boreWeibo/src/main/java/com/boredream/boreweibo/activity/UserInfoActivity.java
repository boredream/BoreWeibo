package com.boredream.boreweibo.activity;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLayoutChangeListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

import com.boredream.boreweibo.BaseActivity;
import com.boredream.boreweibo.R;
import com.boredream.boreweibo.adapter.StatusAdapter;
import com.boredream.boreweibo.api.SimpleRequestListener;
import com.boredream.boreweibo.entity.Status;
import com.boredream.boreweibo.entity.User;
import com.boredream.boreweibo.entity.response.StatusTimeLineResponse;
import com.boredream.boreweibo.utils.ImageOptHelper;
import com.boredream.boreweibo.utils.TitleBuilder;
import com.boredream.boreweibo.widget.Pull2RefreshListView;
import com.boredream.boreweibo.widget.Pull2RefreshListView.OnPlvScrollListener;
import com.boredream.boreweibo.widget.UnderlineIndicatorView;
import com.google.gson.Gson;
import com.handmark.pulltorefresh.library.ILoadingLayout;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnLastItemVisibleListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;

public class UserInfoActivity extends BaseActivity implements 
	OnClickListener, OnCheckedChangeListener {
	
	// 标题栏
	private View title;
	private ImageView titlebar_iv_left;
	private TextView titlebar_tv;
	// headerView - 用户信息
	private View user_info_head;
	private ImageView iv_avatar;
	private TextView tv_name;
	private TextView tv_follows;
	private TextView tv_fans;
	private TextView tv_sign;
	// shadow_tab - 顶部悬浮的菜单栏
	private View shadow_user_info_tab;
	private RadioGroup shadow_rg_user_info;
	private UnderlineIndicatorView shadow_uliv_user_info;
	private View user_info_tab;
	private RadioGroup rg_user_info;
	private UnderlineIndicatorView uliv_user_info;
	// headerView - 添加至列表中作为header的菜单栏
	private ImageView iv_user_info_head;
	private Pull2RefreshListView plv_user_info;
	private View footView;
	// 用户相关信息
	private boolean isCurrentUser;
	private User user;
	private String userName;
	// 个人微博列表
	private List<Status> statuses = new ArrayList<Status>();
	private StatusAdapter statusAdapter;
	private long curPage = 1;
	// 背景图片最小高度
	private int minImageHeight = -1;
	// 背景图片最大高度
	private int maxImageHeight = -1;

	private int curScrollY;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_user_info);

		userName = getIntent().getStringExtra("userName");
		if(TextUtils.isEmpty(userName)) {
			isCurrentUser = true;
			user = application.currentUser;
		}

		initView();
		
		loadData();
	}

	private void initView() {
		title = new TitleBuilder(this)
			.setTitleBgRes(R.drawable.userinfo_navigationbar_background)
			.setLeftImage(R.drawable.navigationbar_back_sel)
			.setLeftOnClickListener(this)
			.build();
		// 获取标题栏信息,需要时进行修改
		titlebar_iv_left = (ImageView) title.findViewById(R.id.titlebar_iv_left);
		titlebar_tv = (TextView) title.findViewById(R.id.titlebar_tv);
		
		initInfoHead();
		initTab();
		initListView();
	}

	// 初始化用户信息
	private void initInfoHead() {
		iv_user_info_head = (ImageView) findViewById(R.id.iv_user_info_head);
		
		user_info_head = View.inflate(this, R.layout.user_info_head, null);
		iv_avatar = (ImageView) user_info_head.findViewById(R.id.iv_avatar);
		tv_name = (TextView) user_info_head.findViewById(R.id.tv_name);
		tv_follows = (TextView) user_info_head.findViewById(R.id.tv_follows);
		tv_fans = (TextView) user_info_head.findViewById(R.id.tv_fans);
		tv_sign = (TextView) user_info_head.findViewById(R.id.tv_sign);
	}

	// 初始化菜单栏
	private void initTab() {
		// 悬浮显示的菜单栏
		shadow_user_info_tab = findViewById(R.id.user_info_tab);
		shadow_rg_user_info = (RadioGroup) findViewById(R.id.rg_user_info);
		shadow_uliv_user_info = (UnderlineIndicatorView) findViewById(R.id.uliv_user_info);
		
		shadow_rg_user_info.setOnCheckedChangeListener(this);
		shadow_uliv_user_info.setCurrentItemWithoutAnim(1);
		
		// 添加到列表中的菜单栏
		user_info_tab = View.inflate(this, R.layout.user_info_tab, null);
		rg_user_info = (RadioGroup) user_info_tab.findViewById(R.id.rg_user_info);
		uliv_user_info = (UnderlineIndicatorView) user_info_tab.findViewById(R.id.uliv_user_info);
		
		rg_user_info.setOnCheckedChangeListener(this);
		uliv_user_info.setCurrentItemWithoutAnim(1);
	}
	
	@SuppressLint("NewApi")
	private void initListView() {
		plv_user_info = (Pull2RefreshListView) findViewById(R.id.plv_user_info);
		initLoadingLayout();
		footView = View.inflate(this, R.layout.footview_loading, null);
		final ListView lv = plv_user_info.getRefreshableView();
		statusAdapter = new StatusAdapter(this, statuses);
		plv_user_info.setAdapter(statusAdapter);
		lv.addHeaderView(user_info_head);
		lv.addHeaderView(user_info_tab);
		plv_user_info.setOnRefreshListener(new OnRefreshListener<ListView>() {

			@Override
			public void onRefresh(PullToRefreshBase<ListView> refreshView) {
				loadStatuses(1);
			}
		});
		plv_user_info.setOnLastItemVisibleListener(
				new OnLastItemVisibleListener() {

					@Override
					public void onLastItemVisible() {
						loadStatuses(curPage + 1);
					}
				});
		plv_user_info.setOnPlvScrollListener(new OnPlvScrollListener() {
			@Override
			public void onScrollChanged(int l, int t, int oldl, int oldt) {
				
				int scrollY = curScrollY = t;
				
				if(minImageHeight == -1) {
					minImageHeight = iv_user_info_head.getHeight();
				}
				
				if(maxImageHeight == -1) {
					Rect rect = iv_user_info_head.getDrawable().getBounds();
					maxImageHeight = rect.bottom - rect.top;
				}
				
				if(minImageHeight - scrollY < maxImageHeight) {
					iv_user_info_head.layout(0, 0, iv_user_info_head.getWidth(), 
							minImageHeight - scrollY);
				} else {
					iv_user_info_head.layout(0, 
							-scrollY - (maxImageHeight - minImageHeight), 
							iv_user_info_head.getWidth(), 
							-scrollY - (maxImageHeight - minImageHeight) + iv_user_info_head.getHeight());
				}
			}
		});
		
		iv_user_info_head.addOnLayoutChangeListener(new OnLayoutChangeListener() {

			@Override
			public void onLayoutChange(View v, int left, int top, int right, int bottom, 
					int oldLeft, int oldTop, int oldRight, int oldBottom) {
				if(curScrollY == bottom - oldBottom) {
					iv_user_info_head.layout(0, 0, 
							iv_user_info_head.getWidth(), 
							oldBottom);
				}
			}
		});
		plv_user_info.setOnScrollListener(new OnScrollListener() {
			
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				
			}
			
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				iv_user_info_head.layout(0, 
						user_info_head.getTop(), 
						iv_user_info_head.getWidth(), 
						user_info_head.getTop() + iv_user_info_head.getHeight());
				
				if(user_info_head.getBottom() < title.getBottom()) {
					shadow_user_info_tab.setVisibility(View.VISIBLE);
					title.setBackgroundResource(R.drawable.navigationbar_background);
					titlebar_iv_left.setImageResource(R.drawable.navigationbar_back_sel);
					titlebar_tv.setVisibility(View.VISIBLE);
				} else {
					shadow_user_info_tab.setVisibility(View.GONE);
					title.setBackgroundResource(R.drawable.userinfo_navigationbar_background);
					titlebar_iv_left.setImageResource(R.drawable.userinfo_navigationbar_back_sel);
					titlebar_tv.setVisibility(View.GONE);
				}
			}
		});
	}

	private void initLoadingLayout() {
		ILoadingLayout loadingLayout = plv_user_info.getLoadingLayoutProxy();
		loadingLayout.setPullLabel("");
		loadingLayout.setRefreshingLabel("");
		loadingLayout.setReleaseLabel("");
		loadingLayout.setLoadingDrawable(new ColorDrawable(R.color.transparent));
	}

	private void loadData() {
		if(isCurrentUser) {
			// 如果是当前授权用户,直接设置信息
			setUserInfo();
		} else {
			// 如果是查看他人,调用获取用户信息接口
			loadUserInfo();
		}
		
		// 加载用户所属微博列表
		loadStatuses(1);
	}

	private void setUserInfo() {
		if(user == null) {
			return;
		}
		tv_name.setText(user.getName());
		titlebar_tv.setText(user.getName());
		imageLoader.displayImage(user.getAvatar_large(), new ImageViewAware(iv_avatar),
				ImageOptHelper.getAvatarOptions());
		tv_follows.setText("关注 " + user.getFriends_count());
		tv_fans.setText("粉丝 " + user.getFollowers_count());
		tv_sign.setText("简介:" + user.getDescription());
	}
	
	private void loadUserInfo() {
		weiboApi.usersShow("", userName,
				new SimpleRequestListener(this, null){

					@Override
					public void onComplete(String response) {
						super.onComplete(response);
						
						// 获取用户信息并设置
						user = new Gson().fromJson(response, User.class);
						setUserInfo();
					}
			
		});
	}
	
	private void loadStatuses(final long requestPage) {
		weiboApi.statusesUser_timeline(
				user == null ? -1 : user.getId(), 
				userName, 
				requestPage,
				new SimpleRequestListener(this, null) {

					@Override
					public void onComplete(String response) {
						super.onComplete(response);
						
						showLog("status comments = " + response);
						
						if(requestPage == 1) {
							statuses.clear();
						}

						addStatus(gson.fromJson(response, StatusTimeLineResponse.class));
					}
					
					@Override
					public void onAllDone() {
						super.onAllDone();
						
						plv_user_info.onRefreshComplete();
					}

				});
	}
	
	private void addStatus(StatusTimeLineResponse response) {
		for(Status status : response.getStatuses()) {
			if(!statuses.contains(status)) {
				statuses.add(status);
			}
		}
		statusAdapter.notifyDataSetChanged();
		
		if(curPage < response.getTotal_number()) {
			addFootView(plv_user_info, footView);
		} else {
			removeFootView(plv_user_info, footView);
		}
	}
	
	private void addFootView(PullToRefreshListView plv, View footView) {
		ListView lv = plv.getRefreshableView();
		if(lv.getFooterViewsCount() == 1) {
			lv.addFooterView(footView);
		}
	}
	
	private void removeFootView(PullToRefreshListView plv, View footView) {
		ListView lv = plv.getRefreshableView();
		if(lv.getFooterViewsCount() > 1) {
			lv.removeFooterView(footView);
		}
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.titlebar_iv_left:
			finish();
			break;
		default:
			break;
		}
	}

	private void syncRadioButton(RadioGroup group, int checkedId) {
		int index = group.indexOfChild(group.findViewById(checkedId));
		
		if(shadow_user_info_tab.getVisibility() == View.VISIBLE) {
			shadow_uliv_user_info.setCurrentItem(index);
			
			((RadioButton)rg_user_info.findViewById(checkedId)).setChecked(true);
			uliv_user_info.setCurrentItemWithoutAnim(index);
		} else {
			uliv_user_info.setCurrentItem(index);
			
			((RadioButton)shadow_rg_user_info.findViewById(checkedId)).setChecked(true);
			shadow_uliv_user_info.setCurrentItemWithoutAnim(index);
		}
		
	}
	
	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		// 同步悬浮和列表中的标题栏状态
		syncRadioButton(group, checkedId);
		
//		switch (checkedId) {
//		case R.id.rb_info:
//			if(shadow_user_info_tab.getVisibility() == View.VISIBLE) {
//				rb_info.setChecked(true);
//				uliv_user_info.setCurrentItemWithoutAnim(0);
//				
//				shadow_rb_info.setChecked(true);
//				shadow_uliv_user_info.setCurrentItem(0);
//			} else {
//				rb_info.setChecked(true);
//				uliv_user_info.setCurrentItem(0);
//				
//				shadow_rb_info.setChecked(true);
//				shadow_uliv_user_info.setCurrentItemWithoutAnim(0);
//			}
//			break;
//		case R.id.rb_status:
//			if(shadow_user_info_tab.getVisibility() == View.VISIBLE) {
//				rb_status.setChecked(true);
//				uliv_user_info.setCurrentItemWithoutAnim(1);
//				
//				shadow_rb_status.setChecked(true);
//				shadow_uliv_user_info.setCurrentItem(1);
//			} else {
//				rb_status.setChecked(true);
//				uliv_user_info.setCurrentItem(1);
//				
//				shadow_rb_status.setChecked(true);
//				shadow_uliv_user_info.setCurrentItemWithoutAnim(1);
//			}
//			break;
//		case R.id.rb_photos:
//			if(shadow_user_info_tab.getVisibility() == View.VISIBLE) {
//				rb_photos.setChecked(true);
//				uliv_user_info.setCurrentItemWithoutAnim(2);
//				
//				shadow_rb_photos.setChecked(true);
//				shadow_uliv_user_info.setCurrentItem(2);
//			} else {
//				rb_photos.setChecked(true);
//				uliv_user_info.setCurrentItem(2);
//				
//				shadow_rb_photos.setChecked(true);
//				shadow_uliv_user_info.setCurrentItemWithoutAnim(2);
//			}
//			break;
//		case R.id.rb_manager:
//			if(shadow_user_info_tab.getVisibility() == View.VISIBLE) {
//				rb_manager.setChecked(true);
//				uliv_user_info.setCurrentItemWithoutAnim(3);
//				
//				shadow_rb_manager.setChecked(true);
//				shadow_uliv_user_info.setCurrentItem(3);
//			} else {
//				rb_manager.setChecked(true);
//				uliv_user_info.setCurrentItem(3);
//				
//				shadow_rb_manager.setChecked(true);
//				shadow_uliv_user_info.setCurrentItemWithoutAnim(3);
//			}
//			break;
//
//		default:
//			break;
//		}
	}


}
