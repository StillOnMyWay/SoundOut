import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.ByteBuffer;

import javax.sound.sampled.*;
import java.util.ArrayList;
import java.util.Arrays;

public class StreamTester{

	static int sampling = 44100;
	static Complex[] complexArray;
	static int numSamples;
	public static void main(String args[])
	{
		final byte b[]=new byte[16384];
    
		System.out.println("Starting recording...");
		try{
			AudioFormat format =new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,44100,16,2,4,44100,false);
			DataLine.Info info =new DataLine.Info(TargetDataLine.class,format);
			if (!AudioSystem.isLineSupported(info))System.out.println("[-] line not supported");

			final TargetDataLine targetline=(TargetDataLine)AudioSystem.getLine(info);
			targetline.open();
			System.out.println("[+] Starting recording");
			targetline.start();

			Thread runner=new Thread()
			{
				@Override public void run()
				{
					
						AudioInputStream stream=new AudioInputStream(targetline);
						System.out.println("[+] writing wav file...");
						try{
							stream.read(b);	
						}
						catch(Exception e){
							System.out.println(e.getMessage());
						}
						
						File file=new File("rec2.wav");
						try
						{
							//TODO At this step 
							AudioSystem.write(stream,AudioFileFormat.Type.WAVE,file);	
							
						}
						catch(Exception e){System.out.println(e.getMessage());}
						System.out.println("[+] Recording has ended");
				}
			};

			runner.start();
			runner.sleep(10000);
			
			double[]rearray = convert(b);
			convert2(b);
			
//			double[]array2 = toDouble(b);
			for(int x=0,y=0;x<b.length/2;x++,y=y+2)
			{
				System.out.println(b[y] + " " + b[y+1]);
//				System.out.println(array[x]);
			}				
			
			targetline.stop();
			targetline.close();
			
			double[] imarray = new double[rearray.length];
			for(double i :imarray)
				i=0.0;
			
//			Fft.transform(rearray,imarray); //Invocation of first method of FFT algorithm	
			Complex [] complexResult = FFT3.fft(complexArray); //Invocation of 2nd FFT using complex files.
			
			
//			System.out.println("* Real\tImag");
//			for(int x=0,y=0;x<rearray.length;x++,y=y+2)
//			{
//				System.out.println(round(rearray[x],3));
//			}
//			System.out.println("* Real\tImag");
//			for(int x=0,y=0;x<rearray.length;x++,y=y+2)
//			{
//				System.out.println(round(imarray[x],3));
//			}				
//
			
//			freq[] mPop = findPopFreq(rearray,imarray);
			double[] mCPop = findPopFreq(complexResult);
			System.out.println("Frequencies Incoming!");
//			for(freq e : mPop)
//			{
//				System.out.println(e);
//			}
		}
		
		catch(Exception e){
			System.out.println(e.getMessage());
		}
  	}
	
	private static double[] findPopFreq(Complex[] complexResult) 
	{	 
		double mMaxFFTSample = 0.0;
	    int mPeakPos = 0;
	    int mNumberOfFFTPoints = complexResult.length/2;
		double[] absSignal = new double[mNumberOfFFTPoints/2];
	    for(int i = 0; i < (mNumberOfFFTPoints/2); i++)
        {
             absSignal[i] = Math.sqrt(Math.pow(complexResult[i].re(), 2) + Math.pow(complexResult[i].im(), 2));
             if(absSignal[i] > mMaxFFTSample)
             {
                 mMaxFFTSample = absSignal[i];
                 mPeakPos = i;
             } 
        }

	    System.out.println("Maximum Frequency "+mMaxFFTSample+ " is found at "+mPeakPos);
		return absSignal;
	}

	private static freq [] findPopFreq(double[] rearray, double[] imarray) {
		// TODO Auto-generated method stub
		freq [] resultsarray = new freq[rearray.length/2];
		for (int l=1;l<(rearray.length)/2;l++)
		{
			resultsarray[l]= new freq(round((l*sampling)/resultsarray.length,2) + " Hz",(Math.sqrt( (rearray[l]* rearray[l])+ (imarray[l]* imarray[l]) )) );
		}
		
		System.out.println("Length of rearray "+rearray.length + " Length of resultsarray" + resultsarray.length);
		
		System.out.println(resultsarray[5].toString());
		System.out.println(resultsarray[10].toString());
		System.out.println(resultsarray[15].toString());
		
		return resultsarray;
	}

	private static class freq
	{	
		public String getFrequency() {
			return frequency;
		}

		public void setFrequency(String frequency) {
			this.frequency = frequency;
		}

		public double getMagnitude() {
			return magnitude;
		}

		public void setMagnitude(double magnitude) {
			this.magnitude = magnitude;
		}

		String frequency;
		double magnitude;
		
		public freq(String freq,double mag)
		{
			frequency=freq;
			magnitude=mag;
		}
		@Override
		public String toString()
		{
			return ""+ getFrequency() + " - " + round(getMagnitude(),3);
		}
	}
	public static double[] convert(byte[] bytes)
	{
		double dArray[] = new double [(bytes.length/4)];
		//(double) (b2 << 8 | b1 & 0xFF) / 32767.0;
		//Encoding type is stereo.
		
		for (int bi=0,di=0;bi<dArray.length;bi=bi+2,di++)
		{
			dArray[di]= (double) (bytes[bi] << 8 | bytes[bi+1] & 0xFF) / 32768.0;
			//(double)((signal[2*i] & 0xFF) | (signal[2*i+1] << 8)) / 32768.0F;
		}
		System.out.println("The int is ");
		return dArray;
	}
	
	public static void convert2(byte[] bytes)
	{
		double dArray[] = new double [(bytes.length/4)];
		complexArray =  new Complex [(bytes.length/4)];
		//(double) (b2 << 8 | b1 & 0xFF) / 32767.0;
		//Encoding type is stereo.
		
		for (int bi=0,di=0;bi<dArray.length;bi=bi+2,di++)
		{
//			dArray[di]= (double) (bytes[bi] << 8 | bytes[bi+1] & 0xFF) / 32768.0;
			dArray[di]= (double)((bytes[bi] & 0xFF) | (bytes[bi+1] << 8)) / 32768.0F;
			complexArray[di] = new Complex(dArray[di],0.0); 
		}
		System.out.println("The int is ");

	}
//	public static double[] toDouble(byte[] byteArray) 
//	{
//	    double[] micBufferData = new double[byteArray.length/2];
//		final int bytesPerSample = 2;
//		// As it is 16bit PCM     
//		final double amplification = 100.0; // choose a number as you like
//		for (int index = 0, floatIndex = 0;	index < bytesRecorded - bytesPerSample + 1; index +=bytesPerSample, floatIndex++) 
//		{
//			double sample = 0;
//			for (int b = 0; b < bytesPerSample; b++) 
//			{             
//				int v = bufferData[index + b];
//				if (b < bytesPerSample - 1 || bytesPerSample == 1) 
//				{                 
//					v &= 0xFF;             
//				}             
//				sample += v << (b * 8);
//			}         
//			double sample32 = amplification * (sample / 32768.0);
//			micBufferData[floatIndex] = sample32;     
//		}
//		return micBufferData;
//	}
	
	public static double round(double value, int places) {
	    if (places < 0) throw new IllegalArgumentException();

	    BigDecimal bd = new BigDecimal(value);
	    bd = bd.setScale(places, RoundingMode.HALF_UP);
	    return bd.doubleValue();
	}
	
}