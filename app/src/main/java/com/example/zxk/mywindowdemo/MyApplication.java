package com.example.zxk.mywindowdemo;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by ZXK on 2017/10/30.
 */

public class MyApplication extends Application{

    private int activityCount;

    @Override
    public void onCreate(){
        super.onCreate();
        //是否是前台
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks(){
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState){
                Log.d("MyApplication", "onActivityCreated");
            }

            @Override
            public void onActivityStarted(Activity activity){
                Log.d("MyApplication", "onActivityStarted");
                activityCount++;
                if(activityCount == 1){
                    WindowHelper.instance.setForeground(true);
                    WindowHelper.instance.startWindowService(getApplicationContext());
                }
            }

            @Override
            public void onActivityResumed(Activity activity){
                Log.d("MyApplication", "onActivityResumed");
            }

            @Override
            public void onActivityPaused(Activity activity){
                Log.d("MyApplication", "onActivityPaused");
            }

            @Override
            public void onActivityStopped(Activity activity){
                Log.d("MyApplication", "onActivityStopped");//这里注释掉是在系统桌面可移动，本来这里的作用是针对应用的，根据本应用的生命周期回到桌面隐藏的功能
                activityCount--;
                if(activityCount == 0){
                    WindowHelper.instance.setForeground(false);
                    WindowHelper.instance.stopWindowService(getApplicationContext());
                }
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState){
                Log.d("MyApplication", "onActivitySaveInstanceState");
            }

            @Override
            public void onActivityDestroyed(Activity activity){
                Log.d("MyApplication", "onActivityDestroyed");
            }
        });
    }
}
