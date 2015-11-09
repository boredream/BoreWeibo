package com.boredream.boreweibo.api;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import android.app.Dialog;
import android.content.Context;
import android.widget.Toast;

import com.boredream.boreweibo.utils.Logger;
import com.boredream.boreweibo.utils.ToastUtils;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;

public class SimpleRequestListener implements RequestListener {

	private Context context;
	private Dialog progressDialog;

	public SimpleRequestListener(Context context, Dialog progressDialog) {
		this.context = context;
		this.progressDialog = progressDialog;
	}
	
	public void onComplete(String response) {
		onAllDone();
		Logger.show("REQUEST onComplete", response);
	}

	public void onComplete4binary(ByteArrayOutputStream responseOS) {
		onAllDone();
		Logger.show("REQUEST onComplete4binary", responseOS.size() + "");
	}

	public void onIOException(IOException e) {
		onAllDone();
		ToastUtils.showToast(context, e.getMessage(), Toast.LENGTH_SHORT);
		Logger.show("REQUEST onIOException", e.toString());
	}

	public void onError(WeiboException e) {
		onAllDone();
		ToastUtils.showToast(context, e.getMessage(), Toast.LENGTH_SHORT);
		Logger.show("REQUEST onError", e.toString());
	}
	
	public void onAllDone() {
		if(progressDialog != null) {
			progressDialog.dismiss();
		}
	}

}
