package in.sumeetkumar.asr.data;


public class AudioData {
	private long time;
	private short [] rawAudio;
	int length;
//	private double featureCepstrum[];
//	private double L1_NORM;
//	private double L2_NORM;
//	private double Linf_NORM;
//	private double[] psdAcrossFrequencyBands;
	
	public AudioData(long time, short[] rawAudio){
		this.time = time;
		this.rawAudio = rawAudio;
		this.length = rawAudio.length;
	}
	
	public short [] getRawAudio() {
		return rawAudio;
	}
	void setRawAudio(short [] rawAudio) {
		this.rawAudio = rawAudio;
	}
	
	public long getTime() {
		return time;
	}
	
	void setTime(long time) {
		this.time = time;
	}

//	public double[] getFeatureCepstrum() {
//		return featureCepstrum;
//	}
//
//	public void setFeatureCepstrum(double featureCepstrum[]) {
//		this.featureCepstrum = featureCepstrum;
//	}
//
//	public double getL1_NORM() {
//		return L1_NORM;
//	}
//
//	public void setL1_NORM(double l1_NORM) {
//		L1_NORM = l1_NORM;
//	}
//
//	public double getL2_NORM() {
//		return L2_NORM;
//	}
//
//	public void setL2_NORM(double l2_NORM) {
//		L2_NORM = l2_NORM;
//	}
//
//	public double getLinf_NORM() {
//		return Linf_NORM;
//	}
//
//	public void setLinf_NORM(double linf_NORM) {
//		Linf_NORM = linf_NORM;
//	}
//
//	public double[] getPsdAcrossFrequencyBands() {
//		return psdAcrossFrequencyBands;
//	}
//
//	public void setPsdAcrossFrequencyBands(double[] psdAcrossFrequencyBands) {
//		this.psdAcrossFrequencyBands = psdAcrossFrequencyBands;
//	}

}
