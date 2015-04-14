package union_qp.com.ua.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import android.os.Handler;
import android.util.Log;

public class ZipManager {
	private static final int SLEEP_TIME = 1000;
	private static final int TRIES = 50;
	
	private static final int BUFFER = 10000000;
	
	private String curFile;
	private int singleFileTry = 0;
	
	private OnSkippedFile listener;
	
	public interface OnSkippedFile{
		void onSkip(String fileName);
	}

	public Exception zip( final Handler h, final TreeSet <String> fileSet, final String zipFileName) {
		if (fileSet.size() == 0){
			return null;
		}
		
		return work(h, fileSet, zipFileName);
	}
	private Exception work(Handler h, TreeSet <String> fileSet,  String zipFileName){
		try {
			final BufferedInputStream origin = null;
			final FileOutputStream dest = new FileOutputStream(zipFileName);
			final ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));
			final byte data[] = new byte[BUFFER];
			
			int counter = 0;
			singleFileTry = 0;
			Iterator<String> iterator = fileSet.iterator();
			curFile = iterator.next();
			
			while(true){
				++singleFileTry;
				Thread thread = new Thread(new Runnable() {
					@Override
					public void run() {
						compress(curFile, origin, out, data);
					}
				});
				thread.start();				
				
				wait(thread);
				
				if (singleFileTry == 3){
					Log.d("TAG", "thread.interrupt() and skip");
					thread.interrupt( );
					if (getOnSkippedFile() != null){
						getOnSkippedFile().onSkip(curFile);
					}
				} else if (thread.isAlive()){
					Log.d("TAG", "thread.interrupt()");
					thread.interrupt( );
					continue;
				}
				
				Log.d("TAG", "next file");
				h.sendEmptyMessage(++counter);
				singleFileTry = 0;
				
				if (iterator.hasNext()){
					curFile = iterator.next();
				} else{
					break;
				}
			}

			out.close();
			Log.d("Mylog", " Zip manager finished");
		} catch (Exception e) {
			Log.d("Mylog", "Exception cought");
			e.printStackTrace();
			return e;
		}
		return null;
	}
	
	private void wait(Thread thread){
		int counterTime = 0;
		try {
			while(thread.isAlive() && counterTime < singleFileTry*TRIES){
				Log.d("TAG", "waiting ... ");
				++counterTime;
				Thread.sleep(singleFileTry*SLEEP_TIME/TRIES);
			}
		} catch (InterruptedException e) { }
	}
	
	private void compress(String curFile, BufferedInputStream origin, ZipOutputStream out, byte data[]){
		Log.v("Compress", "Adding: " + curFile);
		try{
			FileInputStream fi = new FileInputStream(curFile);
			origin = new BufferedInputStream(fi, BUFFER);
	
			ZipEntry entry = new ZipEntry(curFile.substring(curFile.lastIndexOf("/") + 1));
			out.putNextEntry(entry);
			int count;
	
			while ((count = origin.read(data, 0, BUFFER)) != -1) {
				out.write(data, 0, count);
			}
			origin.close();
		} catch (Exception e) {
			try {
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException e1) { }
		}
		
	}
	

	public void unzip(String _zipFile, String _targetLocation) {

		//create target location folder if not exist
		dirChecker(_targetLocation);

		try {
			FileInputStream fin = new FileInputStream(_zipFile);
			ZipInputStream zin = new ZipInputStream(fin);
			ZipEntry ze = null;
			while ((ze = zin.getNextEntry()) != null) {

				//create dir if required while unzipping
				if (ze.isDirectory()) {
					dirChecker(ze.getName());
				} else {
					FileOutputStream fout = new FileOutputStream(_targetLocation + ze.getName());
					for (int c = zin.read(); c != -1; c = zin.read()) {
						fout.write(c);
					}

					zin.closeEntry();
					fout.close();
				}

			}
			zin.close();
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	private void dirChecker(String dir) {
		File f = new File(dir);
		if (!f.isDirectory()) {
			f.mkdirs();
		}
	}
	
	public void setOnSkippedFile(OnSkippedFile listener){
		this.listener = listener;
	}

	public OnSkippedFile getOnSkippedFile(){
		return listener;
	}
}