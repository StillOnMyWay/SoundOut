

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import javax.sound.sampled.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ModularStream
{
	static float sampling = 44100;
	static Complex[] complexArray;
	static int numSamples;
	static double max = 0.0;
	private static ArrayList<freq> target_freq;
	private static double[] real,imag,tReal,tImag;
        
        
        
	static AudioFormat format =new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,44100,16,1,2,44100,false);
        TargetDataLine microphone;
        static AudioInputStream audioInputStream;
        static SourceDataLine sourceDataLine;
         ByteArrayOutputStream out;
         
         
         public void waitTwoSecs(){
            long start=System.currentTimeMillis();
            long stop=start+2000;
//            this is just so i can have a delay between recording and playback
            while(stop-start>0)
            {
                start=System.currentTimeMillis();
            }
         }
                 
                 
         public static void main(String args[]) throws IOException
	{
            ModularStream s=new ModularStream();
            s.record();
            s.waitTwoSecs();
            s.playback();
            s.finish();
            
            double [] inv_phase,magnitude = new double[s.out.toByteArray().length/2];
            real = toDoubleArray(s.out.toByteArray());
            imag = new double[real.length];
	    Fft.transform(real, imag);
            inv_phase = findPhase();
            magnitude = findMagnitude();
            tReal = findReal(inv_phase,magnitude);
            tImag = findImag(inv_phase,magnitude);
            s.waitTwoSecs();
            System.out.println("Playing transform\n");
            s.playbackByteArray(toByteArray(tReal));
            
            //Fft.inverseTransform(tReal,tImag);
           
  	}
	public void record(){
             System.out.println("[+] Recording has began!!");
            try {
            microphone = AudioSystem.getTargetDataLine(format);

            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
            microphone = (TargetDataLine) AudioSystem.getLine(info);
            microphone.open(format);

            out = new ByteArrayOutputStream();
            int numBytesRead;
            int CHUNK_SIZE = 1024;
            byte[] data = new byte[microphone.getBufferSize() / 5];
            microphone.start();

            int bytesRead = 0;

            try {
                while (bytesRead < 100000) { // Just so I can test if recording
                                                // my mic works...
                    numBytesRead = microphone.read(data, 0, CHUNK_SIZE);
                    bytesRead = bytesRead + numBytesRead;
                    //System.out.println(bytesRead);
                    out.write(data, 0, numBytesRead);
                }
                
//                for(int i=0;i<5000;i++){
//                
//                System.out.println(out.toByteArray()[i]);
//                }
            } catch (Exception e) {
                e.getMessage();
            }
            
        }
            catch(Exception x)
            {
            x.getMessage();}
            
            System.out.println("[+] Recording has ended!");
        }
        public void playback()
        {
            System.out.println("[+] Playback has began!");
            try{
                  
                  byte audioData[] = out.toByteArray();
            // Get an input stream on the byte array
            // containing the data
            InputStream byteArrayInputStream = new ByteArrayInputStream(
                    audioData);
            audioInputStream = new AudioInputStream(byteArrayInputStream,format, audioData.length / format.getFrameSize());
            DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, format);
            sourceDataLine = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
            sourceDataLine.open(format);
            sourceDataLine.start();
            int cnt = 0;
            byte tempBuffer[] = new byte[10000];
            try {
                while ((cnt = audioInputStream.read(tempBuffer, 0,tempBuffer.length)) != -1) {
                    if (cnt > 0) {
                        // Write data to the internal buffer of
                        // the data line where it will be
                        // delivered to the speaker.
                        sourceDataLine.write(tempBuffer, 0, cnt);
                    }// end if
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            }
            catch(Exception x){
            x.printStackTrace();}
            
            System.out.println("[+] Playback has ended!");
        }
        
        
         public static void playbackByteArray(byte[] arr)
        {
            System.out.println("[+] Playback has began!");
            try{
                  
                  byte audioData[] = arr;
            // Get an input stream on the byte array
            // containing the data
            InputStream byteArrayInputStream = new ByteArrayInputStream(
                    audioData);
            audioInputStream = new AudioInputStream(byteArrayInputStream,format, audioData.length / format.getFrameSize());
            DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, format);
            sourceDataLine = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
            sourceDataLine.open(format);
            sourceDataLine.start();
            int cnt = 0;
            byte tempBuffer[] = new byte[10000];
            try {
                while ((cnt = audioInputStream.read(tempBuffer, 0,tempBuffer.length)) != -1) {
                    if (cnt > 0) {
                        // Write data to the internal buffer of
                        // the data line where it will be
                        // delivered to the speaker.
                        sourceDataLine.write(tempBuffer, 0, cnt);
                    }// end if
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            }
            catch(Exception x){
            x.printStackTrace();}
            
            System.out.println("[+] Playback has ended!");
        }
        
        
        public void finish(){
             sourceDataLine.drain();
             sourceDataLine.close();
             microphone.close();
        }
	

	public static byte[] toByteArray(double[] doubleArray){
	    int times = Double.SIZE / Byte.SIZE;
	    byte[] bytes = new byte[doubleArray.length * times];
	    for(int i=0;i<doubleArray.length;i++){
	        ByteBuffer.wrap(bytes, i*times, times).putDouble(doubleArray[i]);
	    }
	    return bytes;
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
	
	public static double[] toDoubleArray(byte[] data) 
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