import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.ByteBuffer;

import javax.sound.sampled.*;
import javax.sound.sampled.AudioFileFormat.Type;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.awt.EventQueue;
public class Sound3
{
	static float sampling = 44100;
	static Complex[] complexArray;
	static int numSamples;
	static double max = 0.0;
	private static ArrayList<freq> target_freq;
	private static double[] real,imag,tReal,tImag;
	private static double maxAmp;
	
	public static void main(String args[]) throws IOException
	{
		
		final byte b[]/*=new byte[16384]*/;
    
		System.out.println("Starting recording...");
		try{
			AudioFormat format =new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,44100,16,1,2,44100,false);
			DataLine.Info info =new DataLine.Info(TargetDataLine.class,format);
			if (!AudioSystem.isLineSupported(info))System.out.println("[-] line not supported");

			final TargetDataLine targetline=(TargetDataLine)AudioSystem.getLine(info);
			targetline.open();
			System.out.println("[+] Starting recording");
			targetline.start();
			
			AudioInputStream stream=new AudioInputStream(targetline);
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			byte[] data = new byte[targetline.getBufferSize() / 5];
			System.out.println("[+] Handling input data stream...");
			long start=System.currentTimeMillis();			//records the sound for 10 seconds
			long stop=System.currentTimeMillis()+3000;
			int inbytes=0;
			while(stop-start>0)
			{
				inbytes=targetline.read(data, 0, 1024);
				out.write(data, 0, inbytes);
				start=System.currentTimeMillis();
				//System.out.println("time left"+(stop-start));
			}
			System.out.println("The loop ended");
			//double temp[]=con(b);
			//byte bb[]=toByteArray(temp);
			//AudioInputStream source;
		    //AudioInputStream pcm;
			
//			byte audioData[] = out.toByteArray(); 
//			b=out.toByteArray();
//			double tdouble[]=new double[audioData.length];
//			for(int i=0;i<audioData.length;i++)
//			{
//				tdouble[i]=(double)audioData[i];
//			}
//			for(int i=0;i<audioData.length;i++)
//			{
//				audioData[i]=(byte)tdouble[i];
//			}
//			InputStream b_in1 = new ByteArrayInputStream(audioData);		//creates a stream from the bytes
//			AudioInputStream stream21 = new AudioInputStream(b_in1, format, audioData.length/format.getFrameSize());  //creates audio stream from the inputstream
//			System.out.println("Streams created");
//			File file1 = new File("C:/Users/Jermaine/Desktop/file10.wav");		//change this line to your name
//			//AudioSystem.write(stream21, Type.WAVE, file1);			//writes the byter arry to a wav file but it does not play and it does not stop writing
//			new Play().playSound(stream21);
//			System.out.println("File Written");
	        
	       
	        //FileOutputStream streamd= new FileOutputStream(file1);
	        
		
			
			
//			// 	First run is for initialization of this program to work out the target area			
//			runner.sleep(350);
			
			//double[] rearray = con(b); 
			//double[] imarray = new double[rearray.length];//Create an array of 0s for the imaginary array
						
			//Fft.transform(rearray,imarray); //Invocation of first method of FFT algorithm	
			
//						
			//findPopFreq(rearray,imarray);
//			System.out.println("Frequencies Incoming!");
			
//			for(freq e : mPop)
//			{
//				if(e==null)
//					continue;
//				System.out.println(e);
//			}
			
			//Initialization steps DONE!
			
			//within which we're aiming to cancel all the frequencies which fall into a given closeness to the highest one.
			//We're choosing a threshold of the top 6 frequencies within a magnitude of 10% of the highest one
			//E.g. If the top frequency is 500 Hz with magnitude 40, then we'd look for the top 6 values which fall within the range 36-40 (i.e 10%)
			
			//Use ArrayList, because this means we won't waste additional array space.
//			target_freq = (ArrayList<freq>)top6(Arrays.copyOfRange(mPop,1, mPop.length),scale[0],scale[1]);
//			
//			System.out.println("Targeted Frequencies within range are " + target_freq);
			
			byte [] barry = new byte[8192];
			double [] inv_phase,magnitude = new double[barry.length/2];
			int c=0;
			long now = System.nanoTime();
			while(c<1) //Run until program stops
			{
				
				//records again for another ten seconds
				// everywhere i see u with a single read i capture for 10 seconds because a single read will not work
				start=System.currentTimeMillis();
				stop=System.currentTimeMillis()+3000;
				ByteArrayOutputStream out2= new ByteArrayOutputStream();
				System.out.println("The second file barry is recording");
				while(stop-start>0)
				{
					inbytes=targetline.read(data, 0, 1024);
					out2.write(data, 0, inbytes);
					start=System.currentTimeMillis();
				}
				
				//same as the explanation above
				byte audioData2[]=out2.toByteArray();
				barry=out2.toByteArray();
				InputStream b_in2 = new ByteArrayInputStream(audioData2);
				AudioInputStream stream22 = new AudioInputStream(b_in2, format, audioData2.length/format.getFrameSize());
				File file2 = new File("C:/Users/Jermaine/Desktop/file2.wav");
		        //AudioSystem.write(stream22, Type.WAVE, file2);
				System.out.println("The second file barry is playing");
				new Play().playSound(stream22);
			
				
				System.out.println("\nReading Data... at "+c);				
				real = jCond(audioData2);
				imag = new double[real.length];
				Fft.transform(real, imag);
				inv_phase = findPhase();
				magnitude = findMagnitude();
				tReal = findReal(inv_phase,magnitude);
				tImag = findImag(inv_phase,magnitude);
				Fft.inverseTransform(tReal,tImag);
//				System.out.println("Targeted Frequencies within range are " + target_freq);
				c++;
			}
			
			findMax(tReal,tImag);
			//Attempted tp play back stream. Failed.
			targetline.close();
			//i was trying to play it here but...
			for(int g = 0;g<tReal.length;g++)//Scale Data
			{
				tReal[g]=tReal[g]/maxAmp;
			}
			byte[] datal=jConb(tReal);
			InputStream b_in4 = new ByteArrayInputStream(datal);
			AudioInputStream stream24 = new AudioInputStream(b_in4, format, datal.length/format.getFrameSize());
			new Play().playSound(stream24);
			
			
			//File file = new File("C:/Users/Jermaine/Desktop/file3.wav");
	        //AudioSystem.write(stream, Type.WAVE, file);
//			SourceDataLine sourceLine =  AudioSystem.getSourceDataLine(format);
//            sourceLine.open(format);
//            sourceLine.start();
//            int numbytes=0;
//            byte[] abData = new byte[128000];
//            while(numbytes!=-1)
//            {
//            	
//            	numbytes=stream2.read(abData);
//            	System.out.println(numbytes);
//            	sourceLine.write(abData, 0, numbytes);
//            	
//            }
//            
			
			
			
			
			
//			DataLine.Info info2 =new DataLine.Info(Clip.class,format);
//			Clip chip = (Clip)AudioSystem.getLine(info2);
//			
//			chip.open(format, toByteArray(tReal),0,0);
//			chip.start();
//			System.out.println("Time Elapsed is "+ TimeUnit.NANOSECONDS.toMillis((System.nanoTime()-now))+" ms");
//		
		}
		
		catch(Exception e)//Catches Audio Format read exceptions etc.
		{
			System.out.println(e.getMessage());
		}		
  	}

	private static void findMax(double[] tReal2, double[] tImag2) 
	{
		double [] magnitude = new double [tReal2.length];
		for (int c=0;c<real.length;c++)
		{
			if(magnitude[c]>maxAmp)maxAmp = magnitude[c];
		}
	}

	public static byte[] toByteArray(double[] doubleArray){
		 byte [] buffer=new byte[doubleArray.length*2]; 
		for (int i=0;i<doubleArray.length;i++)
		{
		short s = (short) (32767.0 * doubleArray[i]);
		buffer[i++] = (byte) s;
	    buffer[i++] = (byte) (s >> 8);
		}
		return buffer;
	}
	
	private static double[] findReal(double[] inv_phase, double[] magnitude) {
		double[] mReal = new double [inv_phase.length];
		
		for(int loop=0;loop<inv_phase.length;loop++)
			mReal[loop]=magnitude[loop]*Math.cos(inv_phase[loop]);
		
		return mReal;
	}

	private static double[] findImag(double[] inv_phase, double[] magnitude) 
	{
		double[] mImag = new double [inv_phase.length];
		
		for(int loop=0;loop<inv_phase.length;loop++)
			mImag[loop]=magnitude[loop]*Math.sin(inv_phase[loop]);
		
		return mImag;
	}

	private static double[] findMagnitude() {
		double [] magnitude = new double[real.length];
		for (int c=0;c<real.length;c++)
		{
			if(magnitude[c]>maxAmp)maxAmp = magnitude[c];
			magnitude[c] = (Math.sqrt( (real[c]* real[c])+ (imag[c]* imag[c])));
		}
		return magnitude;
	}

	private static double[] findPhase() 
	{
		double [] phase = new double[real.length];
		for (int c=0;c<real.length;c++)
		{
			phase[c] = Math.atan2(imag[c],real[c]);
		}
		//reverse(phase);//Inverts the phase
		return phase;
	}

	private static void reverse(double[] phase)
	{
		for(int i = 0; i < phase.length / 2; i++)
		{
		    double temp = phase[i];
		    phase[i] = phase[phase.length - i - 1];
		    phase[phase.length - i - 1] = temp;
		}
	}

	private static List<freq> top6(freq[] mFreq, int lower, int upper)
	{
		ArrayList <freq> topFreq = new ArrayList<freq>();
				
		// 1. sort using Comparable
	    Arrays.sort(mFreq);
		
	    topFreq.add(mFreq[0]);//This would be the most correlated frequency, i.e. the greatest magnitude
	    for (int c = 1;c<6;c++)//Adds a maximum of 5 other frequencies to the ArrayList
	    {
	    	if(mFreq[c].frequency<=upper && mFreq[c].frequency>=lower)
	    	{
		    	if(mFreq[c].magnitude> (mFreq[0].magnitude - mFreq[0].magnitude*0.20))
		    	topFreq.add(mFreq[c]);
	    	}
	    }
	    
		return topFreq;
	}

	private static freq [] findPopFreq(double[] rearray, double[] imarray) 
	{
		// TODO Auto-generated method stub
		freq [] resultsarray = new freq[rearray.length/2];
		int maxloc = 0;
		double magnitude = 0.0;//Magnitude of a specific Frequency. Calculated by the SQRT(real[i]^2 * imaginary[i]^2) 
		double maxMag = 0.0;//Maximum magnitude found of all frequencies
		for (int l=1;l<(rearray.length)/2;l++)
		{
			magnitude = (Math.sqrt( (rearray[l]* rearray[l])+ (imarray[l]* imarray[l])));
			resultsarray[l]= new freq(round((l*sampling)/rearray.length,2),magnitude );
			
			if (maxMag < magnitude)
			{
				maxMag = magnitude;
				max = round((l*sampling)/rearray.length,2);
				maxloc = l;
			}
		}
		
		System.out.println("Length of rearray "+rearray.length + "\nLength of resultsarray" + resultsarray.length);
		
		System.out.println("The most popular frequency is "+resultsarray[maxloc]);
		
		
		return resultsarray;
	}
	public static int[] bark()
	{
		int[] scale=new int[2];
		if(max>99&&max<200)
		{
			scale[0]=100;
			scale[1]=200;
			
		}
		else if (max>199&&max<300)
		{
			scale[0]=200;
			scale[1]=300;
		}
		else if (max>299&&max<510)
		{
			scale[0]=300;
			scale[1]=510;
		}
		else if (max>509&&max<630)
		{
			scale[0]=510;
			scale[1]=630;
			
		}
		else if (max>629&&max<770)
		{
			scale[0]=630;
			scale[1]=770;
			
		}
		return scale;
		
	}
	
	private static class freq implements Comparable<freq>
	{	
		public double getFrequency() 
		{
			return frequency;
		}
		public double getMagnitude() {
			return magnitude;
		}
		double frequency;
		double magnitude;
		
		public freq(double d,double mag)
		{
			frequency=d;
			magnitude=mag;
		}
		@Override
		public String toString()
		{
			return ""+ getFrequency() + " Hz - " + round(getMagnitude(),3);
		}
		
		public int compareTo(freq arg0) 
		{
			int ret=0;
			if(this.magnitude>arg0.magnitude)
				return -1;
			else if (this.magnitude==arg0.magnitude)
				return 0;
			else if (this.magnitude<arg0.magnitude)
				return 1;
			return ret;
		}
	}
	
	public static double[] con(byte[] data) 
	{
        int N = data.length;
        double[] d = new double[N/2];
        for (int i = 0; i < N/2; i++) {
            d[i] = ((short) (((data[2*i+1] & 0xFF) << 8) + (data[2*i] & 0xFF))) / ((double)32767.0);
        }
        return d;
    }

	public static double round(double value, int places) {
	    if (places < 0) throw new IllegalArgumentException();

	    BigDecimal bd = new BigDecimal(value);
	    bd = bd.setScale(places, RoundingMode.HALF_UP);
	    return bd.doubleValue();
	}
	
	public static double[] jCond(byte[] bytes)
	{
		double td[]= new double[bytes.length];
		for(int i=0;i<bytes.length;i++)
		{
			td[i]=(double)bytes[i];
		}
		return td;
	}
public static byte[] jConb(double[] dd)
{
	byte [] audioData=new byte[dd.length];
	for(int i=0;i<dd.length;i++)
	{
		audioData[i]=(byte)(dd[i]/10);
	}
	return audioData;
}
	
}