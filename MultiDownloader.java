import java.io.*;
import java.net.*;

public class MultiDownloader
{
	public static void main(String[] args)
	{
		
		if(args.length != 3)
		{
			System.out.println("Error!\nUsage: $java MultiDownloader thread_num url_to_which_content_you_wanna_download  output_filename");
			System.exit(1);
		}
		
		/* */
		final int DOWNLOAD_THREAD_NUM = Integer.parseInt(args[0]);
		/* url string form which we want to download */
		final String url_str = args[1];
		/* name of downloaded file */
		final String OUT_FILENAME = args[2];
		InputStream[] isArr = new InputStream[DOWNLOAD_THREAD_NUM];
		RandomAccessFile[] outArr = new RandomAccessFile[DOWNLOAD_THREAD_NUM];
		try
		{
			URL url = new URL(url_str);
			long file_length = url.openConnection().getContentLength();
			if(file_length < 0)
			{
				System.out.println("Size of " + url.getFile() + ": " + file_length + "\nSomething wrong with your url or connection? Couldn't download it!");
				System.exit(1);
			}
			else
			{
				System.out.println("Size of " + url.getFile() + ": " + file_length);
			}
			isArr[0] = url.openStream();
			/* Create an empty file, which will be inserted with the content we wanna download */
			outArr[0] = new RandomAccessFile(OUT_FILENAME, "rw");
			/* number of bytes each thread should download */			
			long byte_per_thread = file_length / DOWNLOAD_THREAD_NUM;
			long left = file_length % DOWNLOAD_THREAD_NUM;
			for(int i = 0; i < DOWNLOAD_THREAD_NUM; i++)
			{
				/* Open multiple InputStream according to the same URL obj */
				if(i != 0)
				{
					isArr[i] = url.openStream();
					outArr[i] = new RandomAccessFile(OUT_FILENAME, "rw");
				}
				if(i == DOWNLOAD_THREAD_NUM - 1)
				{
					new DownloadThread(i * byte_per_thread, file_length - 1, isArr[i], outArr[i]).start();
				}
				else
				{
					new DownloadThread(i * byte_per_thread, (i+1) * byte_per_thread - 1, isArr[i], outArr[i]).start();
				}
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
}


class DownloadThread extends Thread
{
	/* download entry point */
	private long start;
	/* download finishing point */
	private long end;
	/* InputStream from which this thread starts downloading */
	private InputStream is;
	/* Write out to the corresponding file */
	private RandomAccessFile raf;
	/* buffer length */
	private final int BUF_LEN = 1024;
	/* internal buffer used by downloading thread */
	private byte[] buf = new byte[BUF_LEN];

	public DownloadThread(long start, long end, InputStream is, RandomAccessFile raf)
	{
		System.out.println("------------ " + getName() + " downloads from " + 
				start + " to " + end + " ------------");
		this.start = start;
		this.end = end;
		this.is = is;
		this.raf = raf;
	}

	public void run()
	{
		try
		{
			/* each thread finds the right entry point */
			is.skip(start);
			/* each thread writes to the right output point */
			raf.seek(start);
			long length = end - start + 1;
			int hasRead = 0;
			while( (hasRead = is.read(buf)) > 0)
				raf.write(buf, 0, hasRead);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		finally
		{
			try
			{
				if(is != null)
					is.close();
				if(raf != null)
					raf.close();
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
			}
			System.out.println(getName() + " ends.");
		}
	}
}
