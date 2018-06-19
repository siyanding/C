package com.example.admin.c;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.DMatch;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.Features2d;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.Collator;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;
import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;
import static org.opencv.imgcodecs.Imgcodecs.CV_LOAD_IMAGE_COLOR;
import static org.opencv.imgcodecs.Imgcodecs.imread;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {


    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    CameraBridgeViewBase mCVCamera;
    private static final String TAG = "OpenCvCameraActivity";
    private Mat mRgba;
    //按钮组件
    private Button mButton;
    //当前处理状态
    private static int Cur_State = 0;
    //    private String imageUrl;
    static File region;
    static boolean imageReady = false;
    private static String imagePath = "http://bmob-cdn-17499.b0.upaiyun.com/2018/06/05/c9d88ff636744ed98fbf4c85ada65cd0.jpg";
    static Bitmap bitmap;
    protected static final int SUCCESS = 0;
    protected static final int ERROR = 1;
    protected static final int NETWORK_ERROR = 2;

    //use Handle to update main thread(UI thread)
    private static Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SUCCESS:
                    System.out.println("bitmap download success");
                    bitmap = (Bitmap) msg.obj;
//                    imageView.setImageBitmap(bitmap);
//                    region = (File)msg.obj;
                    if (bitmap.equals(null)) {
                        System.out.println("image is null");
                    } else {
                        imageReady = true;
                    }

                    System.out.println("OpenCvCameraActivity: SUCCESS");
                    break;
                case ERROR:
                    System.out.println("ViewGiftActivity: ERROR");
                    break;
                case NETWORK_ERROR:
                    System.out.println("ViewGiftActivity: NETWORK_ERROR");
                    break;
            }
        }

        ;
    };

    /**
     * 通过OpenCV管理Android服务，异步初始化OpenCV
     */
    BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                    Log.i(TAG, "OpenCV loaded successfully");
                    mCVCamera.enableView();
                    break;
                default:
                    break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mCVCamera = (CameraBridgeViewBase) findViewById(R.id.camera_view);
        mCVCamera.setCvCameraViewListener(this);

        mButton = (Button) findViewById(R.id.deal_btn);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Cur_State == 0) {
                    //切换状态
                    Cur_State = 1;
                    System.out.println("Cur_State:" + Cur_State);
                } else {
                    //恢复初始状态
                    Cur_State = 0;
                }
            }

        });
        getImage();
    }


    @Override
    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4);

    }

    @Override
    public void onCameraViewStopped() {
        mRgba.release();

    }

    private Mat srcCompareKeypoints(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        try {
            Mat src = inputFrame.rgba();
            FeatureDetector featureDetector = FeatureDetector.create(FeatureDetector.AKAZE);

            MatOfKeyPoint keypoint1 = new MatOfKeyPoint();
            Imgproc.cvtColor(src, src, Imgproc.COLOR_RGBA2RGB);
            featureDetector.detect(src, keypoint1);
            Features2d.drawKeypoints(src,keypoint1,src);
            Imgproc.cvtColor(src, src, Imgproc.COLOR_RGB2RGBA);
            Imgproc.resize(src,src,src.size());
            return src;
        } catch (Exception e) {
            return null;
        }
    }

    private Mat compareKeypoints(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        try {
            Mat src = inputFrame.rgba();
            Mat srcTrans = new Mat();
            Core.rotate(src,srcTrans,Core.ROTATE_90_CLOCKWISE);
//            Imgproc.resize(src,src,src.size());
            Mat dst = new Mat();
            bitmap = Bitmap.createScaledBitmap(bitmap, src.rows(), src.cols(), false);
            Utils.bitmapToMat(bitmap, dst);

            FeatureDetector featureDetector = FeatureDetector.create(FeatureDetector.BRISK);

            System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
            System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
            System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");

            System.out.println(
                src.height() + "x" + src.width() + " <===> " + dst.height() + "x" + dst.width()
            );


            MatOfKeyPoint keypoint1 = new MatOfKeyPoint();
            Imgproc.cvtColor(srcTrans, srcTrans, Imgproc.COLOR_RGBA2RGB);
            featureDetector.detect(srcTrans, keypoint1);

            MatOfKeyPoint keypoint2 = new MatOfKeyPoint();
            Imgproc.cvtColor(dst, dst, Imgproc.COLOR_RGBA2RGB);
            featureDetector.detect(dst, keypoint2);

            DescriptorExtractor extractor = DescriptorExtractor.create(DescriptorExtractor.BRISK);

            Mat descriptor1 = new Mat();
            extractor.compute(srcTrans, keypoint1, descriptor1);
            Features2d.drawKeypoints(srcTrans, keypoint1, srcTrans);
//            Imgproc.cvtColor(src, src, Imgproc.COLOR_RGB2RGBA);

            Mat descriptor2 = new Mat();
            extractor.compute(dst, keypoint2, descriptor2);
            Features2d.drawKeypoints(dst, keypoint2, dst);
//            Imgproc.cvtColor(dst, dst, Imgproc.COLOR_RGB2RGBA);

            MatOfDMatch matches = new MatOfDMatch();
            DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE);

            matcher.match(descriptor1, descriptor2, matches);

            List<DMatch> mats = matches.toList();//.toArray()
            List<DMatch> goodMatch = new LinkedList<>();

            Collections.sort(mats, new Comparator<DMatch>() {
                @Override
                public int compare(DMatch mats1, DMatch mats2) {
                    Float dist1 = mats1.distance;
                    Float dist2 = mats2.distance;
                    //可以按User对象的其他属性排序，只要属性支持compareTo方法
                    return dist1.compareTo(dist2);
                }
            });
//            System.out.println("matsMin:" + mats.get(0).distance);
//            System.out.println("matsMax:" + mats.get(mats.size()-1).distance);
            goodMatch = mats.subList(0,(int) mats.size()/20);
//            System.out.println("goodMatchMin:" + goodMatch.get(0).distance);
//            System.out.println("goodMatchMax:" + goodMatch.get(mats.size()-1).distance);

            matches.fromList(goodMatch);

            Mat outImage = new Mat();  src.copyTo(outImage);// new Mat( src.rows(), src.cols(), src.type() );
            Features2d.drawMatches(srcTrans, keypoint1, dst, keypoint2, matches, outImage);
            Imgproc.resize(outImage,outImage,src.size());
            return outImage;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * take care of all image processing, this method will be called every time camera frame refreshed
     */
    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        if( Cur_State  == 1 && imageReady ) {//if (Cur_State == 1){//
            return  compareKeypoints(inputFrame);
//            return  srcCompareKeypoints(inputFrame);
        } else {
            Core.rotate(inputFrame.rgba(),mRgba,Core.ROTATE_90_CLOCKWISE);;
            Imgproc.resize(mRgba,mRgba,inputFrame.rgba().size());
            return mRgba;
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "OpenCV library not found!");
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);

        }
    }


    public static void getImage() {
        new Thread() {
            public void run() {
                try {
                    URL url = new URL(imagePath);

                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(5000);
                    System.out.println("respond code --" + conn.getResponseCode());
                    if (conn.getResponseCode() == 200) {

                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                        InputStream in = conn.getInputStream();
                        Bitmap bitmap = BitmapFactory.decodeStream(in, null, options);
                        //use handler send message
                        Message msg = Message.obtain();
                        msg.obj = bitmap;//data being sent
                        msg.what = SUCCESS;//handler can have different actions depends on different message
                        handler.sendMessage(msg);
                        in.close();
                    } else {
                        Message msg = Message.obtain();
                        msg.what = ERROR;
                        handler.sendMessage(msg);
                    }
                } catch (Exception e) {
                    Message msg = Message.obtain();
                    msg.what = NETWORK_ERROR;
                    handler.sendMessage(msg);
                    e.printStackTrace();
                }
            }
        }.start();
    }


//    public void urlToFile(final String imagePath) {
//        new Thread() {
//            public void run() {
//                File file = getOutputMediaFile(MEDIA_TYPE_IMAGE);
//
//                OutputStream os = null;
//                try {
//
//                    URL url = new URL(imagePath);
//                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//                    conn.setRequestMethod("GET");
//                    conn.setConnectTimeout(5 * 1000);
//                    if (conn.getResponseCode()==200){
//                        System.out.println("response 200 !!!!!!!");
//                        InputStream inStream = conn.getInputStream();
//
//                        os = new FileOutputStream(file);
//                        int bytesRead = 0;
//                        byte[] buffer = new byte[8192];
//                        while ((bytesRead = inStream.read(buffer, 0, 8192)) != -1) {
//                            os.write(buffer, 0, bytesRead);
//                        }
//                        //use handler send message
//                        Message msg = Message.obtain();
//                        msg.obj = file;//data being sent
//                        msg.what = SUCCESS;//handler can have different actions depends on different message
//                        handler.sendMessage(msg);
//
//                        os.close();
//                        inStream.close();
//                    }else {
//                        Message msg = Message.obtain();
//                        msg.what = ERROR;
//                        handler.sendMessage(msg);
//                    }
//
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        }.start();
//    }
//
//
//    /**
//     * Create a File for saving an image or video
//     */
//    private static File getOutputMediaFile(int type) {
//        // To be safe, you should check that the SDCard is mounted
//        // using Environment.getExternalStorageState() before doing this.
//
//        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
//                Environment.DIRECTORY_PICTURES), "MyCameraApp");
//        // This location works best if you want the created images to be shared
//        // between applications and persist after your app has been uninstalled.
//
//        // Create the storage directory if it does not exist
//        if (!mediaStorageDir.exists()) {
//            if (!mediaStorageDir.mkdirs()) {
//                Log.d("MyCameraApp", "failed to create directory");
//                return null;
//            }
//        }


//        // Create a media file name
//        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
//        File mediaFile;
//        if (type == MEDIA_TYPE_IMAGE) {
//            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
//                    "IMG_" + timeStamp + ".jpg");
//        } else if (type == MEDIA_TYPE_VIDEO) {
//            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
//                    "VID_" + timeStamp + ".mp4");
//        } else {
//            return null;
//        }
//
//        return mediaFile;
//    }

//    /**
//     * A native method that is implemented by the 'native-lib' native library,
//     * which is packaged with this application.
//     */
//    public native String stringFromJNI();
}
