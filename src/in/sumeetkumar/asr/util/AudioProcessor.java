package in.sumeetkumar.asr.util;

import in.sumeetkumar.asr.data.KeyValuePair;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;


import android.util.Base64;
import android.util.Log;

public class AudioProcessor extends AudioRecorder {
	private ArrayList<KeyValuePair<Long, short[]>> samples = new ArrayList<KeyValuePair<Long, short[]>>(30);

	
	private double rmsThreadshold = 9;
	private int pickedSampleIndex = 0;
	private int frameIndex = 0;
	public final int SAMPLE_PER_SECOND = 2;
	

	public AudioProcessor(){
		this(9);
		
	}
	
	public AudioProcessor(double rmsThreshold){
		super();
		
		this.rmsThreadshold = rmsThreshold;
		this.pickedSampleIndex = AudioRecorder.SAMPLING_FREQUENCY / (this.getBufferSizeInBytes() / 2) / SAMPLE_PER_SECOND - 1;
		
		/*
		int nnumberofFilters = 24;
		int nlifteringCoefficient = 22;
		boolean oisLifteringEnabled = true;
		boolean oisZeroThCepstralCoefficientCalculated = false;
		int nnumberOfMFCCParameters = 12; //without considering 0-th
		double dsamplingFrequency = (double)AudioRecorder.SAMPLING_FREQUENCY;
		
		int nFFTLength = 512;  // 8000Hz * 0.064s
		
		if (oisZeroThCepstralCoefficientCalculated) {
		  //take in account the zero-th MFCC
		  nnumberOfMFCCParameters = nnumberOfMFCCParameters + 1;
		}
		else {
		  nnumberOfMFCCParameters = nnumberOfMFCCParameters;
		}
		*/
		
		/*
		this.mfcc = new MFCC(nnumberOfMFCCParameters,
		                     dsamplingFrequency,
		                     nnumberofFilters,
		                     nFFTLength,
		                     oisLifteringEnabled,
		                     nlifteringCoefficient,
		                     oisZeroThCepstralCoefficientCalculated);
		                     */
		
		/*
		this.mfcc = new MFCC2(FFT_SIZE, MFCCS_VALUE, 
				MEL_BANDS, (double)AudioRecorder.SAMPLING_FREQUENCY);
				*/
	}
	
	protected void dataArrival(long timestamp, short[] data, int length, int frameLength){
		
//		if(this.frameIndex % this.pickedSampleIndex == 1){
//			
			synchronized(samples){
				samples.add(new KeyValuePair<Long, short[]>(Long.valueOf(timestamp), data));
			}
			//Log.i("Frame", "Frame added");
			
//		}
		
		frameIndex++;
		if(frameIndex == Integer.MAX_VALUE - 1)
			this.frameIndex = 0;
//		Log.i("Frame", "Frame: " + frameIndex);
	}
	
	@Override
	public void stopRecord() {
		super.stopRecord();
	};
	
	public double getRMSThreshold(){
		return this.rmsThreadshold;
	}
	
	public ArrayList<KeyValuePair<Long, short[]>> getSamples(){
		synchronized(samples){
			return new ArrayList<KeyValuePair<Long, short[]>>(this.samples);
		}
	}
	
	public void clearSamples(){
		samples = new ArrayList<KeyValuePair<Long, short[]>>(30);
	}
	
}
