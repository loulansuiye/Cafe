package com.baidu.cafe.local;

import java.util.ArrayList;

import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;

/**
 * @author ranfang@baidu.com, luxiaoyu01@baidu.com
 * @date 2012-9-17
 * @version
 * @todo
 */
public class FPSTracer {
    private final static int interval      = 1000;
    private static long      mFpsStartTime = -1;
    private static long      mFpsPrevTime  = -1;
    private static int       mFpsNumFrames = 0;
    private static float     mTotalFps     = 0;
    private static int       mFpsCount     = 0;

    public static void trace(final LocalLib local) {
        final boolean threadDisable = true;
        new Thread(new Runnable() {

            @Override
            public void run() {
                int time = 0;
                ArrayList<View> decorViews = new ArrayList<View>();
                while (threadDisable) {
                    time++;
                    try {
                        Thread.sleep(interval);
                    } catch (InterruptedException e) {
                        // eat it
                    }

                    View decorView = local.getWindowDecorViews()[0];
                    if (!decorViews.contains(decorView)) {
                        Log.i("FPS", "add listener at " + decorView);
                        ViewTreeObserver observer = decorView.getViewTreeObserver();
                        observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {

                            @Override
                            public boolean onPreDraw() {
                                countFPS();
                                return true;
                            }
                        });
                        decorViews.add(decorView);
                    }

                    // print fps average 1s
                    float averageFPS = 0 == mFpsCount ? 0 : mTotalFps / mFpsCount;
                    Log.d("FPS", time + "s: " + averageFPS);
                    modifyFPS(-1);
                    modifyFPSCount(0);
                }
            }

        }).start();
    }

    private static void countFPS() {
        long nowTime = System.currentTimeMillis();
        if (mFpsStartTime < 0) {
            mFpsStartTime = mFpsPrevTime = nowTime;
            mFpsNumFrames = 0;
        } else {
            long frameTime = nowTime - mFpsPrevTime;
            //            Log.d("FPS", "Frame time:\t" + frameTime);
            mFpsPrevTime = nowTime;
            int interval = 1000;
            if (frameTime < interval) {
                float fps = (float) 1000 / frameTime;
                //                Log.d("FPS", "FPS:\t" + fps);
                modifyFPS(fps);
                modifyFPSCount(-1);
            } else {
                // discard frameTime > interval
            }
        }
    }

    /**
     * @param fps
     *            -1 means reset mFps to 0; otherwise means add fps to mFps
     */
    synchronized private static void modifyFPS(float fps) {
        if (-1 == fps) {
            mTotalFps = 0;
        } else {
            mTotalFps += fps;
        }
    }

    /**
     * @param count
     *            -1 means mFpsCount increase; otherwise means set mFpsCount to
     *            count
     */
    synchronized private static void modifyFPSCount(int count) {
        if (-1 == count) {
            mFpsCount++;
        } else {
            mFpsCount = count;
        }
    }
}