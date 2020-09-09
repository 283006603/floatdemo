package com.example.zxk.mywindowdemo;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

/**
 * Created by ZXK on 2017/10/30.
 */

public class WindowController{

    private final int HIDE = 0x001;
    private final int SHOW = 0x002;
    private final int REFRESH = 0x003;
    private final int CLICK = 0x004;

    private static WindowController instance;
    private Context context;
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams wParamsFloat;
    //最小滑动距离
    private int mTouchSlop;
    //上下两个布局
    private RelativeLayout layoutFloat, contentFloat;
    private ImageView ima_float;

    private int lastX, lastY;
    private int downX, downY;
    //down 时的时间戳
    private int downTime;
    //屏幕宽高
    private int screenWidth, screenHeight;
    //top xy坐标
    private int topY, topX;
    //top 的宽高
    private int topWidth, topHeight;

    //拖动后左右边要恢复的原始位置
    private int leftWidthXLocation, rightWidthXLocation, LeftRightHeightLocation;

    //状态栏的高度
    private int statusBarHeight;
    //展开 or 收起
    private boolean isShow = false;

    RelativeLayout.LayoutParams lp1;

    //悬浮球位置
    private boolean location = true;

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            switch(msg.what){
                case HIDE:
                    hideBottom();
                    break;
                case SHOW:
                    showBottom();
                    break;
                case REFRESH:
                    mWindowManager.updateViewLayout(layoutFloat, wParamsFloat);
                    break;
                case CLICK:
                    click();
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    };

    private WindowController(Context context){
        this.context = context;
        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        //获取status_bar_height资源的ID
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if(resourceId > 0){
            //根据资源ID获取响应的尺寸值
            statusBarHeight = context.getResources().getDimensionPixelSize(resourceId);
        }
        screenWidth = mWindowManager.getDefaultDisplay().getWidth();
        //需要减去状态栏高度
        screenHeight = mWindowManager.getDefaultDisplay().getHeight() - statusBarHeight;
        //悬浮窗自身控件的宽高，要与布局一样
        topWidth = WindowHelper.dip2px(context, 60);
        topHeight = WindowHelper.dip2px(context, 60);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    public static WindowController getInstance(Context context){
        if(instance == null){
            synchronized(WindowController.class){
                if(instance == null){
                    instance = new WindowController(context);
                }
            }
        }
        return instance;
    }

    /**
     * 初始化
     */
    public void init(){
        leftWidthXLocation = WindowHelper.dip2px(context, 40);//40为左右下屏幕间距宽度，和布局要对应上
        rightWidthXLocation = screenWidth - topWidth - WindowHelper.dip2px(context, 40);
        LeftRightHeightLocation = screenHeight - topHeight - WindowHelper.dip2px(context, 40);
        initTop();
        //监听触摸事件,实现拖动和点击。
        initListener();
        mWindowManager.addView(layoutFloat, wParamsFloat);
    }

    /**
     * 初始化top视图
     */

    private void initTop(){
        //悬浮窗布局
        layoutFloat = (RelativeLayout) LayoutInflater.from(context).inflate(R.layout.window_float, null);
        ima_float = layoutFloat.findViewById(R.id.top);
        //内容布局
        contentFloat = (RelativeLayout) LayoutInflater.from(context).inflate(R.layout.window_content, null);
        //悬浮窗布局动态添加内容布局,并初始化内容布局的位置
        lp1 = new RelativeLayout.LayoutParams(WindowHelper.dip2px(context, 400), WindowHelper.dip2px(context, 500));
        wParamsFloat = new WindowManager.LayoutParams();
        wParamsFloat.width = WindowManager.LayoutParams.MATCH_PARENT;
        wParamsFloat.height = WindowManager.LayoutParams.MATCH_PARENT;
        //弹窗类型
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            wParamsFloat.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        }else{
            wParamsFloat.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        //以左上角为基准
        wParamsFloat.gravity = Gravity.START | Gravity.TOP;
        wParamsFloat.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        //如果不加,背景会是一片黑色。
        wParamsFloat.format = PixelFormat.RGBA_8888;
    }

    private void initListener(){
        layoutFloat.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
               if(isShow){
                   hideBottom();
               }
            }
        });

        ima_float.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event){
                switch(event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        Log.e("ZXK", "MotionEvent.ACTION_DOWN");
                        downTime = (int) System.currentTimeMillis();
                        lastX = (int) event.getRawX();
                        lastY = (int) event.getRawY();
                        //保留相对距离，后面可以通过绝对坐标算出真实坐标
                        downX = (int) event.getX();
                        downY = (int) event.getY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if(Math.abs(event.getRawX() - lastX) > 0 || Math.abs(event.getRawY() - lastY) > 0){
                            topX = (int) (event.getRawX() - downX);
                            //需要减去状态栏高度
                            topY = (int) (event.getRawY() - statusBarHeight - downY);
                            //top左右不能越界
                            if(topX < 0){
                                topX = 0;
                            }else if((topX + topWidth) > screenWidth){
                                topX = screenWidth - topWidth;
                            }
                            //top上下不能越界
                            if(topY < 0){
                                topY = 0;
                            }else if((topY + topHeight) > screenHeight){
                                topY = screenHeight - topHeight;
                            }
                            lastX = (int) event.getRawX();
                            lastY = (int) event.getRawY();
                           /* ima_float.setX(topX);
                            ima_float.setY(topY);*/
                            realViewPosition(topX,screenHeight-topY);
                            handler.sendEmptyMessage(REFRESH);
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        int currentTime = (int) System.currentTimeMillis();
                        if(currentTime - downTime < 200 && Math.abs(event.getRawX() - lastX) < mTouchSlop && Math.abs(event.getRawY() - lastY) < mTouchSlop){
                            handler.sendEmptyMessage(CLICK);
                        }
                       /* //看是左边还是右边滑动
                        if(ima_float.getX() >= screenWidth / 2){
                            location = false;
                            ima_float.setX(rightWidthXLocation);
                        }else if(ima_float.getX() < screenWidth / 2){
                            location = true;
                            ima_float.setX(leftWidthXLocation);
                        }
                        ima_float.setY(LeftRightHeightLocation);
                        mWindowManager.updateViewLayout(layoutFloat, wParamsFloat);


                        realViewPosition(location);*/
                        break;
                }
                return true;
            }
        });
    }

    private void realViewPosition(int x,int y){
        RelativeLayout.LayoutParams layoutParams=new RelativeLayout.LayoutParams(topWidth,topWidth);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

        layoutParams.leftMargin=x;
        layoutParams.bottomMargin=y;

        ima_float.setLayoutParams(layoutParams);
    }

    /**
     * 收起
     */
    private void hideBottom(){
        try{
            layoutFloat.removeView(contentFloat);
        }catch(Exception e){
            e.printStackTrace();
        }
        isShow = false;
    }

    /**
     * 展开
     */
    private void showBottom(){
        if(location){
            lp1.removeRule(RelativeLayout.ALIGN_RIGHT);
            lp1.addRule(RelativeLayout.ALIGN_LEFT, R.id.top);//与某元素左边对齐
        }else{
            lp1.removeRule(RelativeLayout.ALIGN_RIGHT);
            lp1.addRule(RelativeLayout.ALIGN_RIGHT, R.id.top);//与某元素右边对齐
        }
        lp1.bottomMargin=WindowHelper.dip2px(context, 40);
        lp1.addRule(RelativeLayout.ABOVE, R.id.top);//在某元素上方
        layoutFloat.addView(contentFloat, lp1);
        isShow = true;
    }

    /**
     * 点击
     */
    private void click(){
        if(isShow){
            handler.sendEmptyMessage(HIDE);
        }else{
            handler.sendEmptyMessage(SHOW);
        }
    }

    public void onDestroy(){
        if(mWindowManager != null){
            try{
                if(isShow){
                    hideBottom();
                }
                mWindowManager.removeView(layoutFloat);
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }
}
