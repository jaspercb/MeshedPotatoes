package protatoes.com.meshpotatoes;

import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.SystemClock;
import android.util.Log;

/**
 * Created by jasper on 2015-10-04.
 */
public class ImageSender {
    private static int bittime = 200;
    private static boolean transmitting = false;
    private static boolean stop = false;
    private static Camera cam;
    private static Camera.Parameters p;

    public static void sendBit(boolean bit, int ms){
        long start = System.currentTimeMillis();

        if (bit) {
            Log.v("bitstring",new String(new char[ms/bittime]).replace("\0", "1"));
            p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
        }
        else {
            Log.v("bitstring",new String(new char[ms/bittime]).replace("\0", "0"));
            p.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
        }
        cam.setParameters(p);
        SystemClock.sleep(Math.max(ms-System.currentTimeMillis()+start,0));
    }

    public static void transferNum(int num, int n){
        //sends n-bit integer
        int cpy = num;
        int ticksleft = n;
        assert ((num<=(1<<n))&&(num>=0));

        while (cpy>0){
            sendBit((cpy%2)==1, bittime);
            cpy/=2;
            ticksleft-=1;
        }
        sendBit(false, bittime*ticksleft);
    }

    public static void beginTransfer(Bitmap img) {
        cam = Camera.open();
        p = cam.getParameters();
        int color;
        transmitting = true;
        //handshake protocol, 1 bytetime on/1 bytetime off
        sendBit(true,8*bittime);
        Log.v("bitstring", " ");
        sendBit(false,8*bittime);
        Log.v("bitstring", " ");

        //send width of image (2 bytes, 4 seconds)
        int width = img.getWidth();
        transferNum(width, 16);
        Log.v("bitstring", " ");
        //send height of image (2 bytes, 4 seconds)
        int height = img.getHeight();
        transferNum(height, 16);
        Log.v("bitstring", " ");
        for(int i=0; i<height; i++){
            for(int j=0; j<width; j++){
                color = img.getPixel(j, i);
                sendBit((color&(1<<23))!=0, bittime);
                sendBit((color&(1<<22))!=0, bittime);
                sendBit((color&(1<<15))!=0, bittime);
                sendBit((color&(1<<14))!=0, bittime);
                sendBit((color&(1<<7))!=0, bittime);
                sendBit((color&(1<<6))!=0, bittime);
                Log.v("bitstring", " ");
            }
        }
        cam.release();
        transmitting = false;

    }
}