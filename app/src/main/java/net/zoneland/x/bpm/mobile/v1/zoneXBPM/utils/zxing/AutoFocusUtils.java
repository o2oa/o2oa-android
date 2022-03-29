package net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.zxing;

import android.graphics.Rect;
import android.hardware.Camera;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.ResultPoint;
import com.google.zxing.common.DetectorResult;
import com.google.zxing.qrcode.detector.Detector;


import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.zxing.camera.CameraManager;

import java.util.Map;

/**
 * Created by fancyLou on 2022-03-29.
 * Copyright © 2022 o2android. All rights reserved.
 */
public class AutoFocusUtils {
    private AutoFocusUtils(){}

    public static void autoFocus(BinaryBitmap bitmap, Map<DecodeHintType, Object> mHints){
        try {
            DetectorResult detectorResult = new Detector(bitmap.getBlackMatrix()).detect(mHints);
            CameraManager cameraManager = CameraManager.get();
            ResultPoint[] p = detectorResult.getPoints();
            //计算扫描框中的二维码的宽度，两点间距离公式
            float point1X = p[0].getX();
            float point1Y = p[0].getY();
            float point2X = p[1].getX();
            float point2Y = p[1].getY();
            int len =(int) Math.sqrt(Math.abs(point1X-point2X)
                    *Math.abs(point1X-point2X)+Math.abs(point1Y-point2Y)*Math.abs(point1Y-point2Y));
            Rect frameRect = cameraManager.getFramingRect();
            if(frameRect!=null){
                int frameWidth = frameRect.right-frameRect.left;
                Camera camera = cameraManager.getCamera();
                Camera.Parameters parameters = camera.getParameters();
                int maxZoom = parameters.getMaxZoom();
                int zoom = parameters.getZoom();
                if(parameters.isZoomSupported()){
                    if(len <= frameWidth/4) {//二维码在扫描框中的宽度小于扫描框的1/4，放大镜头
                        if (zoom == 0) {
                            zoom = maxZoom / 2;
                        } else if (zoom <= maxZoom - 10) {
                            zoom = zoom + 10;
                        } else {
                            zoom = maxZoom;
                        }
                        parameters.setZoom(zoom);
                        camera.setParameters(parameters);
                    }
                }
            }
        } catch (NotFoundException e) {
            e.printStackTrace();
        } catch (FormatException e) {
            e.printStackTrace();
        }
    }
}
