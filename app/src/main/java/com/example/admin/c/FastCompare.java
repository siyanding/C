package com.example.admin.c;

import android.graphics.Bitmap;
import android.util.Log;

import org.apache.commons.io.FileUtils;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvException;
import org.opencv.core.CvType;
import org.opencv.core.DMatch;
import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FastFeatureDetector;
import org.opencv.features2d.Feature2D;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.Features2d;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import static org.opencv.imgcodecs.Imgcodecs.imread;

public class FastCompare {
//    public static void main(String[] args){
//        OpenCVLoader.initDebug();
//        FeatureSurfBruteforce("D:\\personal\\courses\\cs554\\hw2\\man.png","D:\\personal\\courses\\cs554\\hw2\\manBand.png");
//    }

    public static Mat FeatureSurfBruteforce(String srcString, String dstString){//(Mat src, Bitmap dstFile){//return Mat
//        Mat outImage = new Mat();
//        try{
//        System.out.println("enter imageCompare");
//        Mat dst = new Mat(dstFile.getWidth(), dstFile.getHeight(), CvType.CV_8UC1);
////        try {
////            byte[] byteDst;
////            byteDst = FileUtils.readFileToByteArray(dstFile);
////
////            dst.put(0, 0, byteDst);
//////            Mat dst = Imgcodecs.imdecode(new MatOfByte(byteDst), Imgcodecs.CV_LOAD_IMAGE_UNCHANGED);
////            System.out.println("imageComparedst: " + dst.toString());
////        } catch (IOException e) {
////            e.printStackTrace();
////        }
//
////            System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
////        Mat tmp = new Mat (bmp.getWidth(), bmp.getHeight(), CvType.CV_8UC1);
//        Utils.bitmapToMat(dstFile, dst);

        Mat outImage = new Mat();
        try{
        Mat src = imread(srcString);
        Mat dst = imread(dstString);


            MatOfKeyPoint keypoint1 = new MatOfKeyPoint();
            MatOfKeyPoint keypoint2 = new MatOfKeyPoint();
            FeatureDetector siftDetector =FeatureDetector.create(FeatureDetector.AKAZE);
//            if(dst.empty()){
//                System.out.println("dst is empty");
//                return null ;
//            }


            siftDetector.detect(src,keypoint1);
            siftDetector.detect(dst,keypoint2);

            DescriptorExtractor extractor=DescriptorExtractor.create(DescriptorExtractor.AKAZE);

            Mat descriptor1 = new Mat(src.rows(),src.cols(),src.type());
            extractor.compute(src, keypoint1, descriptor1);
            Mat descriptor2 = new Mat(dst.rows(),dst.cols(),dst.type());
            extractor.compute(dst, keypoint2, descriptor2);

            MatOfDMatch matches = new MatOfDMatch();
            DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE);

            matcher.match(descriptor1,matches);
//            matcher.match(descriptor1,descriptor2,matches);


            Features2d.drawMatches(src, keypoint1, dst, keypoint2, matches, dst);
    }catch(Exception e){
        System.out.println("例外:"+e);
    }
            Imgcodecs.imwrite("C:\\Users\\Admin\\Desktop\\test.png", outImage);

        return outImage;

    }

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
