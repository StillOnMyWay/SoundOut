import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import javax.sound.sampled.*;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Stream2
{
	static float sampling = 44100;
	static Complex[] complexArray;
	static int numSamples;
	static double max = 0.0;
	private static ArrayList<freq> target_freq;
	private static double[] real;
	private static double[] imag;
	private static ArrayList<Double> allFreq;
	
	public static void main(String args[]) throws IOException
	{
		final byte b[]/*=new byte[16384]*/;
    
//		System.out.println("Starting recording...");
		try{
			AudioFormat format =new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,44100,16,1,2,44100,false);
			DataLine.Info info =new DataLine.Info(TargetDataLine.class,format);
			if (!AudioSystem.isLineSupported(info))System.out.println("[-] line not supported");

			final TargetDataLine targetline=(TargetDataLine)AudioSystem.getLine(info);
			targetline.open();
//			System.out.println("[+] Starting recording");
			targetline.start();
			b = new byte[44100];
			
			AudioInputStream stream=new AudioInputStream(targetline);
//			System.out.println("[+] Handling input data stream...");
				
			stream.read(b);
//			Thread.sleep(150);
			

//			runner.start();
//			// 	First run is for initialization of this program to work out the target area			
//			runner.sleep(350);
			
			double[] rearray = con(b); 
			double [] testarray = StdAudio.read("Toney.wav");
//			StdAudio.play(testarray);
			double[] imarray = new double[rearray.length];//Create an array of 0s for the imaginary array
						
			Fft.transform(rearray,imarray); //Invocation of first method of FFT algorithm	

			freq[] mPop = findPopFreq(rearray,imarray);
//			System.out.println("Frequencies Incoming!");
			
//			for(freq e : mPop)
//			{
//				if(e==null)
//					continue;
//				System.out.println(e);
//			}
			
			//Initialization steps DONE!
			
			//Get range from Bark Scale in an array of two ints e.g. if the frequency was 453.43, it would fall in the 3rd bark scale
			//with return value [300,510], where 300 is the lower and upper limit is 510.
			
			int scale [] = bark();//Gives you the range of frequencies which we will focus on for 20 iterations of the loop, aiming
			//within which we're aiming to cancel all the frequencies which fall into a given closeness to the highest one.
			//We're choosing a threshold of the top 6 frequencies within a magnitude of 10% of the highest one
			//E.g. If the top frequency is 500 Hz with magnitude 40, then we'd look for the top 6 values which fall within the range 36-40 (i.e 10%)
			
			//TODO Use ArrayList, because this means we won't waste additional array space.
			target_freq = (ArrayList<freq>)top6(Arrays.copyOfRange(mPop,1, mPop.length),scale[0],scale[1]);
			
			System.out.println("Targeted Frequencies within range "+scale[0] +" to "+ scale[1]+ " are " + target_freq);
			byte [] barry = new byte[8192];
			freq[] nFreq;
			
			int c=0;
			long now = System.nanoTime();
			
			while(c<100) //Run until program stops
			{
				stream.read(barry);
				System.out.println("\nReading Data... at "+c);
				real = con(barry);
				imag = new double[real.length];
//				StdAudio.play(real);
				Fft.transform(real, imag);
				nFreq = findPopFreq(real, imag);
				scale = bark();
				target_freq = (ArrayList<freq>) top6(Arrays.copyOfRange(nFreq,1, nFreq.length),scale[0],scale[1]);
				System.out.println("Targeted Frequencies within range "+scale[0] +" to "+ scale[1]+ " are " + target_freq);
				
				c++;
			}
			System.out.println("Time Elapsed is "+ TimeUnit.NANOSECONDS.toMillis((System.nanoTime()-now)));
		}
		
		
		catch(Exception e)//Catches Audio Format read exceptions etc.
		{
			System.out.println(e.getMessage());
		}		
  	}

	public static byte[] conback(double[] data) 
	{//TODO To convert to 
		
		byte [] byteOut = new byte[data.length*2];
		short s;
		for(int x = 0,y=0;x<data.length;x++,y=y+2)
		{
			s = (short) ((32767.0 * data[x])*(1/data.length));
	        byteOut[y] = (byte) s;
	        byteOut[y+1] = (byte) (s >> 8);
		}
	    return byteOut;
    }
	private static List<freq> top6(freq[] mFreq, int lower, int upper)
	{
		ArrayList <freq> topFreq = new ArrayList<freq>();
				
		// 1. sort using Comparable
	    Arrays.sort(mFreq);
		
	    topFreq.add(mFreq[0]);//This would be the most correlated frequency, i.e. the greatest magnitude
//    	System.out.print(","+mFreq[0].frequency);
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
		
		System.out.println("The most popular frequency is "+resultsarray[maxloc]);
		
		
		return resultsarray;
	}
	public static int[] bark()
	{
		int[] scale=new int[2];
		if(max>0&&max<100)
		{
			scale[0]=0;
			scale[1]=100;
			
		}
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
		else if (max>769&&max<920)
		{
			scale[0]=770;
			scale[1]=920;
		}
		else if (max>919&&max<1080)
		{
			scale[0]=920;
			scale[1]=1080;
			
		}
		else if (max>1079&&max<1270)
		{
			scale[0]=1080;
			scale[1]=1270;
			
		}else if (max>1269&&max<1480)
		{
			scale[0]=1270;
			scale[1]=1480;
		}
		else if (max>1479&&max<1720)
		{
			scale[0]=1480;
			scale[1]=1720;
			
		}
		else if (max>1719&&max<2000)
		{
			scale[0]=1720;
			scale[1]=2000;
			
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
	
}