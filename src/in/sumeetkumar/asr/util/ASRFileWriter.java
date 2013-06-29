package in.sumeetkumar.asr.util;

import in.sumeetkumar.asr.data.IJsonObject;
import in.sumeetkumar.asr.data.KeyValuePair;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

public class ASRFileWriter {
	
	private FileWriter writer;
	
	public ASRFileWriter(String sFileName){
        try {
            File root = new File(Environment.getExternalStorageDirectory(), "ASR");
            if (!root.exists()) 
            {
                root.mkdirs();
            }

            long epoch = System.currentTimeMillis()/1000;
            File gpxfile = new File(root, sFileName+ epoch);
			writer = new FileWriter(gpxfile);
		} catch (IOException e) {
			Log.d(LogUtil.TAG, e.toString());
		}
	}

//	protected Object syncObject = new Object();
//	
//	private void writeSamples(ArrayList<KeyValuePair<Long,short[]>> samples){
//		StringBuilder builder = new StringBuilder(samples.size() * 1024);
//		Log.i(LogUtil.TAG, "Sample size: " + samples.size());
//		
//		for(KeyValuePair<Long,short[]> sample:samples){
//		
//			builder.append(sample.getKey());
//			short[] data = sample.getValue();
//			for(short value:data){
//				builder.append(",").append(value);
//				
//				if(dumpRaw){
//					try {
//						this.outStream.writeShort(value);
//					} catch (IOException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//				}
//			}
//			builder.append("\r\n");
//		}
//		
//		writeData(builder.toString());
//	}
//	
//	private void writeDataToFile() {
//		StringBuilder stringBuilder = new StringBuilder();
//		Iterator<IJsonObject> itr = dataLog.iterator();
//		while (itr.hasNext()) {
//			IJsonObject jsonObject = itr.next();
//			stringBuilder.append(jsonObject.toString());
//			stringBuilder.append('\n');
//		}
//		SDCardWriter.generateNoteOnSD(SDCardWriter.FILE_NAME,
//				stringBuilder.toString());
//
//	}
//	
//	public void writeData(String data){
//		synchronized(syncObject){
//			if(bufferedWriter != null){
//				try {
//					bufferedWriter.write(data);
//				} catch (Exception e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//					Log.d(LogUtil.TAG, e.toString());
//				}
//			}
//		}
//	}
	
	
	public  void appendText( String sBody)
	{
	    try
	    {
	    	if(writer != null){
	    		writer.append(sBody);
	    		writer.write('\n');
	    		writer.flush();
	    	}
	    }
	    catch(IOException e)
	    {
	    	Log.d(LogUtil.TAG, "RunningApplications: " + e.toString());
	        e.printStackTrace();
	    }
	} 
	
	public void close(){
		try {
			writer.flush();
			writer.close();
			writer = null;
			
		} catch (IOException e) {
			Log.d(LogUtil.TAG, e.toString());
			e.printStackTrace();
		}
	}

	@Override
	protected void finalize() throws Throwable {
		// TODO Auto-generated method stub
		super.finalize();
		
		if(writer != null){
			writer.close();
			writer = null;
		}
	}
}
