package com.qq.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class ImgUtil {
	private static final String TAG = "LoadImageUtil";
	private static ImgUtil instance;
	private static HashMap<String, SoftReference<Bitmap>> imgCaches;
	private static ExecutorService executorThreadPool = Executors
			.newFixedThreadPool(1);
	static {
		instance = new ImgUtil();
		imgCaches = new HashMap<String, SoftReference<Bitmap>>();
	}

	public static ImgUtil getInstance() {
		if (instance != null) {
			return instance;
		}
		return null;
	}

	public void loadBitmap(final String path,
			final OnLoadBitmapListener listener) {
		final Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				Bitmap bitmap = (Bitmap) msg.obj;
				listener.loadImage(bitmap, path);
			}
		};
		new Thread() {

			@Override
			public void run() {
				executorThreadPool.execute(new Runnable() {
					@Override
					public void run() {
						Bitmap bitmap = loadBitmapFromCache(path);
						if (bitmap != null) {
							Message msg = handler.obtainMessage();
							msg.obj = bitmap;
							handler.sendMessage(msg);
						}

					}
				});
			}

		}.start();
	}

	private Bitmap loadBitmapFromCache(String path) {
		if (imgCaches == null) {
			imgCaches = new HashMap<String, SoftReference<Bitmap>>();
		}
		Bitmap bitmap = null;
		if (imgCaches.containsKey(path)) {
			bitmap = imgCaches.get(path).get();
		}
		if (bitmap == null) {
			bitmap = loadBitmapFromLocal(path);
		}
		return bitmap;
	}

	private Bitmap loadBitmapFromLocal(String path) {
		if (path == null) {
			return null;
		}
		BitmapFactory.Options options = new Options();
		options.inJustDecodeBounds = true;
		Bitmap bitmap = BitmapFactory.decodeFile(path, options);
		float height = 800f;
		float width = 480f;
		float scale = 1;
		if (options.outWidth > width && options.outWidth > options.outHeight) {
			scale = options.outWidth / width;
		} else if (options.outHeight > height
				&& options.outHeight > options.outWidth) {
			scale = options.outHeight / height;
		} else {
			scale = 1;
		}
		options.inSampleSize = (int) scale;
		options.inJustDecodeBounds = false;
		//bitmap = BitmapFactory.decodeStream(this.getClass().getClassLoader().getResourceAsStream("assets/"+path));
		path="http://192.168.1.229:8080/webchat/admin/head";
		try{
		URL url = new URL(path);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setConnectTimeout(5000);
		conn.setRequestMethod("GET");
		if (conn.getResponseCode() == 200){
			InputStream inputStream = conn.getInputStream();
			 bitmap = BitmapFactory.decodeStream(inputStream);
			bitmap = decodeBitmap(bitmap);
		}}catch (IOException e){
			e.printStackTrace();
		}


		if (!imgCaches.containsKey(path)) {
			//imgCaches.put(path, new SoftReference<Bitmap>(bitmap));
			addCache(path, bitmap);
		}
		return bitmap;
	}

	private Bitmap decodeBitmap(Bitmap bitmap) {
		int scale = 100;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.JPEG, scale, bos);
		while ((bos.toByteArray().length / 1024) > 30) {
			bos.reset();
			bitmap.compress(Bitmap.CompressFormat.JPEG, scale, bos);
			scale -= 10;
		}
		ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
		bitmap = BitmapFactory.decodeStream(bis);
		return bitmap;
	}
	
	public void addCache(String path,Bitmap bitmap){
		imgCaches.put(path, new SoftReference<Bitmap>(bitmap));
	}
	
	public void reomoveCache(String path){
		imgCaches.remove(path);
	}

	public interface OnLoadBitmapListener {
		void loadImage(Bitmap bitmap, String path);
	}
}