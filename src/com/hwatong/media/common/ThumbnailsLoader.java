package com.hwatong.media.common;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v4.util.LruCache;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;

/**
 * 加载缩略图
 * @author ds
 *
 */
public class ThumbnailsLoader {

	private static String TAG = "ThumbnailsLoader";

	private Context mContext;
	private LruCache<String, Bitmap> mMemoryCache;
	private final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
	private final int cacheSize = maxMemory / 8;
	private Map<Integer, Bitmap> mBitMap = new HashMap<Integer, Bitmap>();

	private int viewWidth = 120;
	private int viewHeight = 120;

	public ThumbnailsLoader(Context context) {
		mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
			protected int sizeOf(String key, Bitmap bitmap) {
				// The cache size will be measured in kilobytes rather than
				// number of items.
				return bitmap.getByteCount() / 1024;
			}
		};

		mContext = context;
	}

	private void addBitmapToMemoryCache(String key, Bitmap bitmap) {
		if (getBitmapFromMemCache(key) == null) {
			mMemoryCache.put(key, bitmap);
		}
	}

	private Bitmap getBitmapFromMemCache(String key) {
		return mMemoryCache.get(key);
	}

	private void setDefaultImage() {
		if (mBitMap.size() == 0) {
			Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.music_icon_small);
			mBitMap.put(R.drawable.music_icon_small, bitmap);
			bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.picture_grid_icon);
			mBitMap.put(R.drawable.picture_grid_icon, bitmap);
			bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.music_icon_small);
			mBitMap.put(R.drawable.music_icon_small, bitmap);
		}
	}

	public void loadBitmap(String path, int res, ImageView imageView) {

		setDefaultImage();

		final Bitmap bitmap = getBitmapFromMemCache(path);
		if (bitmap != null) {
			imageView.setImageBitmap(bitmap);
			return;
		}
		if (cancelPotentialWork(path, imageView)) {
			final BitmapWorkerTask task = new BitmapWorkerTask(imageView);
			final AsyncDrawable asyncDrawable = new AsyncDrawable(mContext.getResources(), mBitMap.get(res), task);
			imageView.setImageDrawable(asyncDrawable);
			task.execute(path, String.valueOf(res));
		}
	}

	static class AsyncDrawable extends BitmapDrawable {
		private final WeakReference<BitmapWorkerTask> bitmapWorkerTaskReference;

		public AsyncDrawable(Resources res, Bitmap bitmap, BitmapWorkerTask bitmapWorkerTask) {
			super(res, bitmap);
			bitmapWorkerTaskReference = new WeakReference<BitmapWorkerTask>(bitmapWorkerTask);
		}

		public BitmapWorkerTask getBitmapWorkerTask() {
			return bitmapWorkerTaskReference.get();
		}
	}

	private boolean cancelPotentialWork(String p, ImageView imageView) {
		final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);

		if (bitmapWorkerTask != null) {
			final String path = bitmapWorkerTask.path;
			if (!path.equals(p)) {
				// Cancel previous task
				bitmapWorkerTask.cancel(true);
			} else {
				// The same work is already in progress
				return false;
			}
		}
		// No task associated with the ImageView, or an existing task was
		// cancelled
		return true;
	}

	private static BitmapWorkerTask getBitmapWorkerTask(ImageView imageView) {
		if (imageView != null) {
			final Drawable drawable = imageView.getDrawable();
			if (drawable instanceof AsyncDrawable) {
				final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
				return asyncDrawable.getBitmapWorkerTask();
			}
		}
		return null;
	}

	class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {
		private final WeakReference<ImageView> imageViewReference;
		private String path = "";

		public BitmapWorkerTask(ImageView imageView) {
			// Use a WeakReference to ensure the ImageView can be garbage
			// collected
			imageViewReference = new WeakReference<ImageView>(imageView);
		}

		// Decode image in background.
		@Override
		protected Bitmap doInBackground(String... params) {
			Bitmap bitmap = null;
			path = params[0];
			int res = Integer.valueOf(params[1]);
			if (!TextUtils.isEmpty(path) && !isCancelled()) {
				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inSampleSize = 2;
				if (res == R.drawable.music_icon_small) {
					bitmap = ThumbnailUtils.createVideoThumbnail(path, MediaStore.Images.Thumbnails.MICRO_KIND);
				} else if (res == R.drawable.picture_grid_icon) {
					// bitmap = BitmapFactory.decodeFile(path, options);
					bitmap = decodeSampledBitmapFromResource(path, viewWidth, viewHeight);
				} else if (res == R.drawable.music_icon_small) {
					bitmap = getAlbumArtBmp(path);
				}

				bitmap = ThumbnailUtils.extractThumbnail(bitmap, 100, 80, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);

				if (bitmap != null) {
					addBitmapToMemoryCache(path, bitmap);
				}
			}
			return bitmap;
		}

		// Once complete, see if ImageView is still around and set bitmap.
		@Override
		protected void onPostExecute(Bitmap bitmap) {
			if (isCancelled()) {
				bitmap = null;
			}

			if (imageViewReference != null && bitmap != null) {
				final ImageView imageView = imageViewReference.get();
				if (imageView != null) {
					imageView.setImageBitmap(bitmap);
				}
			}
		}

		private Bitmap getAlbumArtBmp(String path) {
			if (isCancelled()) {
				return null;
			}

			Bitmap bitmap = null;
			int album_id = -1;
			Cursor c = mContext.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, new String[] { MediaStore.Audio.Media.ALBUM_ID },
					MediaStore.Audio.Media.DATA + "=?", new String[] { path }, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
			if (c.moveToFirst()) {
				album_id = c.getInt(c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));
			}
			c.close();
			c = null;

			if (isCancelled()) {
				return null;
			}

			if (album_id != -1) {
				String mUriAlbums = "content://media/external/audio/albums";
				String[] projection = new String[] { "album_art" };
				Cursor cur = mContext.getContentResolver().query(Uri.parse(mUriAlbums + "/" + Integer.toString(album_id)), projection, null, null, null);
				String album_art = null;
				if (cur.getCount() > 0 && cur.getColumnCount() > 0) {
					cur.moveToNext();
					album_art = cur.getString(0);
				}
				cur.close();
				cur = null;

				if (album_art != null) {
					bitmap = BitmapFactory.decodeFile(album_art);
				}
			}

			return bitmap;
		}
	}

	private Bitmap decodeSampledBitmapFromResource(String path, int reqWidth, int reqHeight) {
		if (Constant.DEBUG)
			Log.d(TAG, "decodeSampledBitmapFromResource:path=" + path + ", reqWidth=" + reqWidth + ", reqHeight=" + reqHeight);
		// 第一次解析将inJustDecodeBounds设置为true，来获取图片大小
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		// BitmapFactory.decodeResource(res, resId, options);
		BitmapFactory.decodeFile(path, options);
		if (Constant.DEBUG)
			Log.d(TAG, "decodeSampledBitmapFromResource: options.outHeight=" + options.outHeight + ", options.outWidth=" + options.outWidth);
		// 位图占用大于50M的内存（16bit）时就不再加载图片（5000*5000*16/8=50M）
		if (options.outWidth * options.outHeight > 5000 * 5000)
			return null;
		// 调用上面定义的方法计算inSampleSize值
		options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
		// 使用获取到的inSampleSize值再次解析图片
		options.inJustDecodeBounds = false;
		try {
			return BitmapFactory.decodeFile(path, options);
		} catch (Exception e) {
			e.printStackTrace();
			Log.e(TAG, "decodeSampledBitmapFromResource: e=" + e + ", options.outHeight=" + options.outHeight + ", options.outWidth=" + options.outWidth);
		}

		return null;
	}

	private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
		if (Constant.DEBUG)
			Log.d(TAG, "calculateInSampleSize: reqWidth=" + reqWidth + ", reqHeight=" + reqHeight);
		// 源图片的高度和宽度
		final int height = options.outHeight;
		final int width = options.outWidth;

		int inSampleSize = 1;
		if (height > reqHeight || width > reqWidth) {
			// 计算出实际宽高和目标宽高的比率
			final int heightRatio = Math.round((float) height / (float) reqHeight);
			final int widthRatio = Math.round((float) width / (float) reqWidth);
			// 选择宽和高中最小的比率作为inSampleSize的值，这样可以保证最终图片的宽和高
			// 一定都会大于等于目标的宽和高。
			inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
		}
		if (Constant.DEBUG)
			Log.d(TAG, "calculateInSampleSize: inSampleSize=" + inSampleSize);
		return inSampleSize;
	}
}
