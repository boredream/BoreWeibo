package com.boredream.boreweibo.utils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.widget.Toast;


public class ImageUtils {
	
	public static final int REQUEST_CODE_FROM_CAMERA = 5001;
	public static final int REQUEST_CODE_FROM_ALBUM = 5002;
	
	/**
	 * 存放拍照图片的uri地址
	 */
	public static Uri imageUriFromCamera;

	/**
	 * 显示获取照片不同方式对话框
	 */
	public static void showImagePickDialog(final Activity activity) {
		String title = "选择获取图片方式";
		String[] items = new String[]{"拍照", "相册"};
		new AlertDialog.Builder(activity)
			.setTitle(title)
			.setItems(items, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					switch (which) {
					case 0:
						pickImageFromCamera(activity);
						break;
					case 1:
						pickImageFromAlbum(activity);
						break;
					default:
						break;
					}
				}
			})
			.show();
	}
	
	/**
	 * 打开相机拍照获取图片
	 */
	public static void pickImageFromCamera(final Activity activity) {
		imageUriFromCamera = createImageUri(activity);
		
		Intent intent = new Intent();
		intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUriFromCamera);
		activity.startActivityForResult(intent, REQUEST_CODE_FROM_CAMERA);
	}
	
	/**
	 * 打开本地相册选取图片
	 */
	public static void pickImageFromAlbum(final Activity activity) {
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_GET_CONTENT);
		intent.setType("image/*");
		activity.startActivityForResult(intent, REQUEST_CODE_FROM_ALBUM);
	}
	
	/**
	 * 打开本地相册选取图片2
	 */
	public static void pickImageFromAlbum2(final Activity activity) {
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_PICK);
		intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		activity.startActivityForResult(intent, REQUEST_CODE_FROM_ALBUM);
	}
	
	/**
	 * 将图片保存到SD中
	 */
	public static void saveFile(Context context, Bitmap bm, String fileName) throws IOException {
		// 未安装SD卡时不做保存
		String storageState = Environment.getExternalStorageState();
		if(!storageState.equals(Environment.MEDIA_MOUNTED)) {
			ToastUtils.showToast(context, "未检测到SD卡", Toast.LENGTH_SHORT);
			return;
		}
		
		// 图片文件保存路径
		File storageDirectory = Environment.getExternalStorageDirectory();
		File path = new File(storageDirectory, "/boreweibo/weiboimg");
		// 图片路径不存在创建之
		if (!path.exists()) {
			path.mkdirs();
		}
		// 图片文件如果不存在创建之
		File myCaptureFile = new File(path, fileName);
		if (!myCaptureFile.exists()) {
			myCaptureFile.createNewFile();
		}
		// 将图片压缩至文件对应的流里,即保存图片至该文件中
		BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(myCaptureFile));
		bm.compress(Bitmap.CompressFormat.JPEG, 100, bos);
		bos.flush();
		bos.close();
	}
	
	/**
	 * 创建一条图片uri,用于保存拍照后的照片
	 */
	private static Uri createImageUri(Context context) {
		String name = "boreWbImg" + System.currentTimeMillis();
		ContentValues values = new ContentValues();
		values.put(MediaStore.Images.Media.TITLE, name);
		values.put(MediaStore.Images.Media.DISPLAY_NAME, name + ".jpeg");
		values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
		Uri uri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
		return uri;
	}
	
	/**
	 * 删除一条图片
	 */
	public static void deleteImageUri(Context context, Uri uri) {
		context.getContentResolver().delete(imageUriFromCamera, null, null);
	}

	/**
	 * 获取图片文件路径
	 */
	public static String getImageAbsolutePath(Context context, Uri uri) {
		Cursor cursor = MediaStore.Images.Media.query(context.getContentResolver(), uri, 
				new String[]{MediaStore.Images.Media.DATA});
		if(cursor.moveToFirst()) {
			return cursor.getString(0);
		}
		return null;
	}
	
	/////////////////////Android4.4以上版本特殊处理如下//////////////////////////////////////
	
	/**
	 * 根据Uri获取图片绝对路径，解决Android4.4以上版本Uri转换
	 * @param context
	 * @param imageUri
	 */
	@TargetApi(Build.VERSION_CODES.KITKAT)
	public static String getImageAbsolutePath19(Activity context, Uri imageUri) {
		if (context == null || imageUri == null)
			return null;
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT 
				&& DocumentsContract.isDocumentUri(context, imageUri)) {
			if (isExternalStorageDocument(imageUri)) {
				String docId = DocumentsContract.getDocumentId(imageUri);
				String[] split = docId.split(":");
				String type = split[0];
				if ("primary".equalsIgnoreCase(type)) {
					return Environment.getExternalStorageDirectory() + "/" + split[1];
				}
			} else if (isDownloadsDocument(imageUri)) {
				String id = DocumentsContract.getDocumentId(imageUri);
				Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
				return getDataColumn(context, contentUri, null, null);
			} else if (isMediaDocument(imageUri)) {
				String docId = DocumentsContract.getDocumentId(imageUri);
				String[] split = docId.split(":");
				String type = split[0];
				Uri contentUri = null;
				if ("image".equals(type)) {
					contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
				} else if ("video".equals(type)) {
					contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
				} else if ("audio".equals(type)) {
					contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
				}
				String selection = MediaStore.Images.Media._ID + "=?";
				String[] selectionArgs = new String[] { split[1] };
				return getDataColumn(context, contentUri, selection, selectionArgs);
			}
		} 
		
		// MediaStore (and general)
		if ("content".equalsIgnoreCase(imageUri.getScheme())) {
			// Return the remote address
			if (isGooglePhotosUri(imageUri))
				return imageUri.getLastPathSegment();
			return getDataColumn(context, imageUri, null, null);
		}
		// File
		else if ("file".equalsIgnoreCase(imageUri.getScheme())) {
			return imageUri.getPath();
		}
		return null;
	}

	private static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
		Cursor cursor = null;
		String column = MediaStore.Images.Media.DATA;
		String[] projection = { column };
		try {
			cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
			if (cursor != null && cursor.moveToFirst()) {
				int index = cursor.getColumnIndexOrThrow(column);
				return cursor.getString(index);
			}
		} finally {
			if (cursor != null)
				cursor.close();
		}
		return null;
	}

	/**
	 * @param uri The Uri to check.
	 * @return Whether the Uri authority is ExternalStorageProvider.
	 */
	private static boolean isExternalStorageDocument(Uri uri) {
		return "com.android.externalstorage.documents".equals(uri.getAuthority());
	}

	/**
	 * @param uri The Uri to check.
	 * @return Whether the Uri authority is DownloadsProvider.
	 */
	private static boolean isDownloadsDocument(Uri uri) {
		return "com.android.providers.downloads.documents".equals(uri.getAuthority());
	}

	/**
	 * @param uri The Uri to check.
	 * @return Whether the Uri authority is MediaProvider.
	 */
	private static boolean isMediaDocument(Uri uri) {
		return "com.android.providers.media.documents".equals(uri.getAuthority());
	}

	/**
	 * @param uri The Uri to check.
	 * @return Whether the Uri authority is Google Photos.
	 */
	private static boolean isGooglePhotosUri(Uri uri) {
		return "com.google.android.apps.photos.content".equals(uri.getAuthority());
	}
}
