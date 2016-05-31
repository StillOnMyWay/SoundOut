import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.ByteBuffer;

import javax.sound.sampled.*;

import com.sun.xml.internal.messaging.saaj.util.ByteInputStream;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Stream
{
	static float sampling = 44100;
	static Complex[] complexArray;
	static int numSamples;
	static double maxAmp;
	static double max = 0.0;
	private static ArrayList<freq> target_freq;
	private static double[] real,imag,tReal,tImag;
	
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
			b = new byte[8192];
			
			AudioInputStream stream=new AudioInputStream(targetline);
			System.out.println("[+] Handling input data stream...");
				
//			stream.read(b);
//			Thread.sleep(3000);
			
			double[] rearray = con(b); 
			byte []test = conback(rearray);
			double[] imarray = new double[rearray.length];//Create an array of 0s for the imaginary array

//			double [] testarray = StdAudio.read("");
			
			StdAudio.play(rearray);
					
//			Fft.transform(rearray,imarray); //Invocation of first method of FFT algorithm	
			
//			findPopFreq(rearray,imarray);

			byte [] barry = new byte[8192];
			double [] inv_phase,magnitude = new double[barry.length/2];
			int c=0;
			long now = System.nanoTime();
			ArrayList<double []>buffer = new ArrayList<double []>();
			while(c<100) //Run until program stops
			{
				stream.read(barry);
				System.out.println("\nReading Data... at "+c);				
//				Thread.sleep(200);
				real = con(barry);
//				StdAudio.play(real);
				imag = new double[real.length];
				Fft.transform(real, imag);
				inv_phase = findPhase();
				magnitude = findMagnitude(real,imag);
				tReal = findReal(inv_phase,magnitude);
				tImag = findImag(inv_phase,magnitude);
				Fft.inverseTransform(tReal,tImag);
				findMaxAmp(tReal,tImag);
				buffer.add(tReal);
//				StdAudio.play(scaleIFFT(tReal));
				c++;
			}
			System.out.println("Time Elapsed is "+TimeUnit.NANOSECONDS.toMillis(System.nanoTime()-now));
			for(double []buf:buffer)
				StdAudio.play(buf);
			
			//Attempted to play back stream. Failed.
//			Play man = new Play().playSound(stream)
//			StdAudio.play(scaleIFFT(tReal));
			
			System.out.println("Playing transform\n");
			byte[] staticky = conback(tReal);
            ModularStream.playbackByteArray(conback(tReal));
			
		}
		
		catch(Exception e)//Catches Audio Format read exceptions etc.
		{
			System.out.println(e.getMessage());
		}		
  	}

	private static void findMaxAmp(double[] tReal2, double[] tImag2) 
	{	
		double [] magnitude = findMagnitude(tReal2, tImag2);
		for (int c=0;c<real.length;c++)
		{
			if (magnitude[c]>maxAmp) maxAmp = magnitude[c];			
		}
	}
	
	private static double[] findReal(double[] inv_phase, double[] magnitude) 
	{
		double[] mReal = new double [inv_phase.length];
		
		for(int loop=0;loop<inv_phase.length;loop++)
			if(magnitude[loop]*Math.cos(inv_phase[loop])<0.0000000000001)
				mReal[loop] = 0;
			else
				mReal[loop]=magnitude[loop]*Math.cos(inv_phase[loop]);
		
		return mReal;
	}

	private static double[] findImag(double[] inv_phase, double[] magnitude) 
	{
		double[] mImag = new double [inv_phase.length];
		
		for(int loop=0;loop<inv_phase.length;loop++)
			if(magnitude[loop]*Math.cos(inv_phase[loop])<0.0000000000001)
				mImag[loop]=0.0;
			else
				mImag[loop]=magnitude[loop]*Math.sin(inv_phase[loop]);
		
		return mImag;
	}

	private static double[] findMagnitude(double[] real, double [] imag) {
		double [] magnitude = new double[real.length];
		for (int c=0;c<real.length;c++)
		{
			magnitude[c] = (Math.sqrt( (real[c]* real[c])+ (imag[c]* imag[c])));
		}
		return magnitude;
	}
	
	private static double[] scaleIFFT(double[] realVals)
	{
		System.out.println("Max is "+maxAmp);
//		System.out.print("[");
		for(int i = 0;i<realVals.length;i++)
		{
			realVals[i] = realVals[i]/maxAmp;
//			System.out.print(realVals[i]+",");
		}
//		System.out.print("]");
		return realVals;
	}
	private static double[] findPhase() 
	{
		double [] phase = new double[real.length];
		for (int c=0;c<real.length;c++)
		{
			phase[c] = Math.atan2(imag[c],real[c]);
		}
		reverse(phase);//Inverts the phase
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
	
	private static double[] toDouble8(byte[] mArray)
	{
		double array []= new double[mArray.length];
		for (int i = 0; i < array.length-1; i++) 
        {
			array[i] = (double) mArray[i]/127.0;
        }
		
		return array;
	}
	public static double[] con(byte[] data) 
	{
        int N = data.length;
        double[] d = new double[N/2];
        for (int i = 0; i < N/2; i++) 
        {
            d[i] = ((short) (((data[2*i+1] & 0xFF) << 8) + (data[2*i] & 0xFF))) / ((double)32767.0);
//            System.out.println("\n1 - D is "+d[i]+" "+((data[2*i+1] & 0xFF) << 8)+" "+(data[2*i] & 0xFF));
//            System.out.println("2 - D is "+d[i]+" "+((data[2*i+1])+" "+(data[2*i])));
//            System.out.println("3 - D is "+d[i]+" "+((data[2*i+1] & 0xFF) << 8) + (data[2*i] & 0xFF));
//            System.out.println("4 - D is "+d[i]+" "+(( ( (data[2*i+1] & 0xFF) << 8) + (data[2*i] & 0xFF)) / ((double)1.0)));
        }
        return d;
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

	public static double round(double value, int places) {
	    if (places < 0) throw new IllegalArgumentException();

	    BigDecimal bd = new BigDecimal(value);
	    bd = bd.setScale(places, RoundingMode.HALF_UP);
	    return bd.doubleValue();
	}
	
}