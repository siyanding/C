package com.example.admin.c;

import android.graphics.Bitmap;
import android.util.Log;

import org.apache.commons.io.FileUtils;
import org.opencv.android.Utils;
import org.opencv.core.CvException;
import org.opencv.core.DMatch;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.Features2d;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class ImageCompare {
    public static Mat FeatureSurfBruteforce(Mat src, File dstFile){//return Mat

        System.out.println("enter imageCompare");
        Mat dst = new Mat();
//        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        try {
            byte[] byteDst;
            byteDst = FileUtils.readFileToByteArray(dstFile);

            dst.put(0, 0, byteDst);
//            Mat dst = Imgcodecs.imdecode(new MatOfByte(byteDst), Imgcodecs.CV_LOAD_IMAGE_UNCHANGED);
            System.out.println("imageComparedst: " + dst.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
//        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
//        Mat src = new Mat
//        Mat src = imread(srcString);
//        Mat dst = imread(dstString);
//        Mat dst = Highgui.imread(dstString);
        //System.out.println("SRC STRING IN FEATURE_SURF_BRUTE_FORCE IS (" + srcString.charAt(0) + ")");
        //System.out.println("DST STRING IN FEATURE_SURF_BRUTE_FORCE IS " + dstString);


        FeatureDetector fd = FeatureDetector.create(FeatureDetector.AKAZE);
        DescriptorExtractor de = DescriptorExtractor.create(DescriptorExtractor.AKAZE);
        //FLANN的含义是Fast Library forApproximate Nearest Neighbors
        DescriptorMatcher Matcher = DescriptorMatcher.create(DescriptorMatcher.FLANNBASED);
        //尝试所有可能的匹配，从而使得它总能够找到最佳匹配，这也是Brute Force（暴力法）
        //DescriptorMatcher Matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_L1);
        //DescriptorMatcher Matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE);

        MatOfKeyPoint mkp = new MatOfKeyPoint();
        fd.detect(src, mkp);
        Mat desc = new Mat();
        de.compute(src, mkp, desc);
        Features2d.drawKeypoints(src, mkp, src);


        MatOfKeyPoint mkp2 = new MatOfKeyPoint();
        fd.detect(dst, mkp2);
        Mat desc2 = new Mat();
        de.compute(dst, mkp2, desc2);
        Features2d.drawKeypoints(dst, mkp2, dst);


        // Matching features
        MatOfDMatch Matches = new MatOfDMatch();
        Matcher.match(desc, desc2, Matches);

        double maxDist = Double.MIN_VALUE;
        double minDist = Double.MAX_VALUE;

        DMatch[] mats = Matches.toArray();
        for (int i = 0; i < mats.length; i++) {
            double dist = mats[i].distance;
            if (dist < minDist) {
                minDist = dist;
            }
            if (dist > maxDist) {
                maxDist = dist;
            }
        }
        System.out.println("Min Distance:" + minDist);
        System.out.println("Max Distance:" + maxDist);
        List<DMatch> goodMatch = new LinkedList<>();

        for (int i = 0; i < mats.length; i++) {
            double dist = mats[i].distance;
            if (dist < 3 * minDist && dist < 0.2f) {
                goodMatch.add(mats[i]);
            }
        }
        System.out.println("size:" + goodMatch.size());
        System.out.println("percent:" + goodMatch.size()/(double)mats.length);
        Matches.fromList(goodMatch);
        // Show result
        Mat OutImage = new Mat();
        Features2d.drawMatches(src, mkp, dst, mkp2, Matches, OutImage);
        return  OutImage;
//        if((goodMatch.size()/(double)mats.length)>0.1){
//            return true;
//        }else{
//            return false;
//        }
    }

//    public static BufferedImage mat2Img(Mat in)
//    {
//        BufferedImage out;
//        byte[] data = new byte[in.cols() * in.rows() * (int)in.elemSize()];
//        int type;
//        in.get(0, 0, data);
//
//        if(in.channels() == 1)
//            type = BufferedImage.TYPE_BYTE_GRAY;
//        else
//            type = BufferedImage.TYPE_3BYTE_BGR;
//
//        out = new BufferedImage(in.cols(), in.rows(), type);
//
//        out.getRaster().setDataElements(0, 0, in.cols(), in.rows(), data);
//        return out;
//    }

    public static Bitmap mat2Bitmap(Mat in){

        Bitmap bmp = null;
        try {
            //Imgproc.cvtColor(seedsImage, tmp, Imgproc.COLOR_RGB2BGRA);
//            Imgproc.cvtColor(seedsImage, in, Imgproc.COLOR_GRAY2RGBA, 4);
            bmp = Bitmap.createBitmap(in.cols(), in.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(in, bmp);
        }
        catch (CvException e){
            Log.d("Exception",e.getMessage());
        }
        return bmp;
    }
}
