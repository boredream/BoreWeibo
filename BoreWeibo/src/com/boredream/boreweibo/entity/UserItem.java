package com.boredream.boreweibo.entity;

public class UserItem {

	public UserItem(boolean isShowTopDivider, int leftImg, String subhead, String caption) {
		this.isShowTopDivider = isShowTopDivider;
		this.leftImg = leftImg;
		this.subhead = subhead;
		this.caption = caption;
	}

	private boolean isShowTopDivider;
	private int leftImg;
	private String subhead;
	private String caption;

	public boolean isShowTopDivider() {
		return isShowTopDivider;
	}

	public void setShowTopDivider(boolean isShowTopDivider) {
		this.isShowTopDivider = isShowTopDivider;
	}

	public int getLeftImg() {
		return leftImg;
	}

	public void setLeftImg(int leftImg) {
		this.leftImg = leftImg;
	}

	public String getSubhead() {
		return subhead;
	}

	public void setSubhead(String subhead) {
		this.subhead = subhead;
	}

	public String getCaption() {
		return caption;
	}

	public void setCaption(String caption) {
		this.caption = caption;
	}

}
