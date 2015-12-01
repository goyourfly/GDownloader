### A download manager for android
> It is easy to used for download large file, you don't need to care about how the thread and io.

### Display：
![Display](https://github.com/goyourfly/GDownloader/blob/master/art/art.gif)


### JCenter:
    compile 'com.goyourfly:GDownloader:1.0.0'    

### Get Start：
- Init:

	`DownloadModule.init(context,path,maxTask,NameGenerator);`
	`DownloadModule.getInstance().registerListener(DownloadListener);`

- Start

	`DownloadModule.download(url);`

- Pause

	`DownloadModule.pause(url);`

- Cancel

	`DownloadModule.cancel(url);`

- Shutdown

	`DownloadModule.shutdown();`


- Callback

		public interface DownloadListener {
    	    public void onPreStart(String url);
	
    	    public void onStart(String url, long totalLength, long localLength);
	
    	    public void onProgress(String url, long totalLength, long downloadedBytes);
	
    	    public void onPause(String url);
	
    	    public void onWaiting(String url);
	
    	    public void onCancel(String url);
	
    	    public void onFinish(String url);
	
    	    public void onError(String url, String err);
    	}

- Custom NameGenerator
	
	We provide the default name generator `HashCodeNameGenerator`,if you want to custom NameGenerator, you need implement class NameGenerator and override the `getName` function. 
