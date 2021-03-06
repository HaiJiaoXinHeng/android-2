package net.cyclestreets.util;

import java.io.InputStream;
import java.lang.ref.WeakReference;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class ImageDownloader {
  static public void get(final String url,
						             final ImageView imageView) {
		final BitmapDownloaderTask task = new BitmapDownloaderTask(imageView);
		task.execute(url);
	} // get

	//////////////////////////
	static class BitmapDownloaderTask extends AsyncTask<String, Void, Bitmap> {
		private final WeakReference<ImageView> imageViewReference;

		public BitmapDownloaderTask(final ImageView imageView) {
			imageViewReference = new WeakReference<>(imageView);
 		} // BitmapDownloaderTask

		@Override
		protected Bitmap doInBackground(String... params) {
			return downloadBitmap(params[0]);
		} // doInBackground

		@Override
		protected void onPostExecute(final Bitmap bitmap) {
 			if (isCancelled())
				return;

      if (bitmap == null)
        return;

			if(imageViewReference == null)
				return;
			
			final ImageView imageView = imageViewReference.get();
			if (imageView == null) 
				return;

      final WindowManager wm = (WindowManager)imageView.getContext().getSystemService(Context.WINDOW_SERVICE);
      final int device_height = wm.getDefaultDisplay().getHeight();
      final int device_width = wm.getDefaultDisplay().getWidth();
      final int height = (device_height > device_width)
          ? device_height / 10 * 5
          : device_height / 10 * 6;
      final int width = imageView.getWidth();
      imageView.setLayoutParams(new LinearLayout.LayoutParams(width, height));

      final int imageWidth = bitmap.getWidth();
      final int imageHeight = bitmap.getHeight();
      final float aspect = (float)imageWidth/(float)imageHeight;

      if (aspect > 1) {
        // landscape, so adjust height
        final int viewHeight = imageView.getHeight();
        int newViewHeight = (int)(viewHeight/aspect);
        imageView.setMaxHeight(newViewHeight);
      } //

      imageView.setAnimation(null);

      imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
			imageView.setImageBitmap(bitmap);
		} // onPostExecute

		private Bitmap downloadBitmap(final String url) {
			final HttpClient client = new DefaultHttpClient();
			final HttpGet getRequest = new HttpGet(url);
			getRequest.setHeader("User-Agent", "CycleStreets Android/1.0");

			try {
				HttpResponse response = client.execute(getRequest);
				final int statusCode = response.getStatusLine().getStatusCode();
				if (statusCode != HttpStatus.SC_OK) 
					return null;

				final HttpEntity entity = response.getEntity();
				if (entity == null) 
					return null;
				
				try {
					final InputStream inputStream = entity.getContent();
					return Bitmaps.loadStream(inputStream);
				} finally	{
					entity.consumeContent();
				} // finally
			} catch (Exception e)	{
				getRequest.abort();
			} // catch
			return null;
		} // downloadBitmap
	} // BitmapDownloaderTask 
} // ImageDownloader
