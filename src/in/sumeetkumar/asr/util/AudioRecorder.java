package in.sumeetkumar.asr.util;

import java.io.DataOutputStream;
import java.util.LinkedList;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

public class AudioRecorder extends Thread {
	
	private static final String CLASS_PREFIX = AudioRecorder.class.getName();
	private AudioRecord recorder = null;
	private int bufferSizeInBytes = 0;
	private int sleepInterval = 0;
	private Object syncObject = new Object();
	private boolean isRecording = false;
	
	public static final int SAMPLING_FREQUENCY = 8000;
	

	private class ReadResult{
		public int sampleRead;
		public short[] buffer;
		public long timeStamp;
	}
	
	private LinkedList<ReadResult> queue = new LinkedList<ReadResult>();
	private Thread consumerThread = new Thread(){
		public void run(){
			while(true){
				ReadResult top = null;
				
				synchronized(syncObject){
					
					if(queue.size() > 0){
						top = queue.poll();
					}else{
						if(!isRecording){
							onRecordEnded();
							Log.i(CLASS_PREFIX, "Consumer thread ended.");
							break;
						}
					}
				}
				
				if(top != null){
					dataArrival(top.timeStamp, top.buffer, top.sampleRead, top.buffer.length);
				}
				
				try {
					sleep(50);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		}
	};
	
	public AudioRecorder(){
		super();
		bufferSizeInBytes = AudioRecord.getMinBufferSize(
				SAMPLING_FREQUENCY, 
                AudioFormat.CHANNEL_IN_MONO, 
                AudioFormat.ENCODING_PCM_16BIT);
		//this.sleepInterval = sleepInterval;
	}
	
	public int getBufferSizeInBytes(){
		return this.bufferSizeInBytes;
	}
	
	public int getSleepInterval(){
		return this.sleepInterval;
	}
	
	
	
	public void run(){
		this.consumerThread.start();
		        
		while(isRecording && bufferSizeInBytes > 0) {
			
			if(recorder.getRecordingState() != AudioRecord.RECORDSTATE_RECORDING)
				break;
		
			try{
				short[] buffer = new short[bufferSizeInBytes / 2];
				int numSamplesRead = recorder.read(buffer, 0, buffer.length);
				
				if(numSamplesRead == AudioRecord.ERROR_INVALID_OPERATION) {
					continue;
				}
				else if(numSamplesRead == AudioRecord.ERROR_BAD_VALUE) {
					continue;
				}

				ReadResult result = new ReadResult();
				result.buffer = buffer;
				result.sampleRead = numSamplesRead;
				result.timeStamp = System.currentTimeMillis();
				
				synchronized(this.syncObject){
					queue.add(result);
				}
				
				//Log.i("Audio", "hi");
			}catch(Exception recordException){
				Log.d(LogUtil.TAG, recordException.toString());
				recordException.printStackTrace();

			}
			
		}
		
		this.recorder.stop();
		this.recorder.release();

	}
	
	public void startRecord(){
		try{
			this.isRecording = false;
			if(this.recorder != null){
				if(recorder.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING){
					return;
				}
			}else{
				if(initRecorder() == null)
					return;
			}
			recorder.startRecording();
			this.start();
			this.isRecording = true;
			
		}catch(Exception ex){
			ex.printStackTrace();
			Log.d(LogUtil.TAG, ex.toString());
		}
	}
	
	
	public void stopRecord(){
		this.isRecording = false;
	}
	
	protected void dataArrival(long timestamp, short[] data, int length, int frameLength){
		
	}
	
	protected void onRecordEnded(){
		
	}
	
	
	private AudioRecord initRecorder(){
		try{
			bufferSizeInBytes = AudioRecord.getMinBufferSize(
					SAMPLING_FREQUENCY, 
	                AudioFormat.CHANNEL_IN_MONO, 
	                AudioFormat.ENCODING_PCM_16BIT);
			
			this.recorder = new AudioRecord(
					MediaRecorder.AudioSource.DEFAULT,
					SAMPLING_FREQUENCY,
					AudioFormat.CHANNEL_IN_MONO,
					AudioFormat.ENCODING_PCM_16BIT,
					bufferSizeInBytes * 4);
		}catch(IllegalArgumentException ex){
			ex.printStackTrace();
			Log.d(LogUtil.TAG, ex.toString());
		}
		return this.recorder;
	}
}
