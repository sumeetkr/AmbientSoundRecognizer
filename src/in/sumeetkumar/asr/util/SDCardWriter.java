package in.sumeetkumar.asr.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import edu.mit.media.funf.util.LogUtil;

import android.os.Environment;
import android.text.StaticLayout;
import android.util.Log;
import android.widget.Toast;

public class SDCardWriter {

	public static String FILE_NAME = "mfccs.txt";
	
	public static void generateNoteOnSD(String sFileName, String sBody)
	{
	    try
	    {
	        File root = new File(Environment.getExternalStorageDirectory(), "ASR");
	        if (!root.exists()) 
	        {
	            root.mkdirs();
	        }

	        File gpxfile = new File(root, sFileName);
	        FileWriter writer = new FileWriter(gpxfile);
	        writer.append(sBody);
	        writer.flush();
	        writer.close();

//	        Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
	    }
	    catch(IOException e)
	    {
	    	Log.d(LogUtil.TAG, "RunningApplications: " + e.toString());
	        e.printStackTrace();
	    }
	}  
}
