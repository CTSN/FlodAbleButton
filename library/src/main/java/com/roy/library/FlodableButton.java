package com.roy.library;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import com.roy.library.util.MeasureUtil;


/**
 * Created by roy on 2017/7/31.
 */

public class FlodableButton extends ViewGroup {

    private FlodableButton sfb;
    private WindowManager wm;
    private float widWidth;

    private String text = "";     //文本
    private int bacColor;       //控件背景色
    private int circleleColor;  //小圆颜色
    private int textColor;      //文本颜色
    private float textSize;   //文本大小
    private float speed;    //拉伸速度
    private float degrees;  //旋转度数


    private final static int IS_SLIDE_DECREASE = 0;  //递减状态
    private static final int IS_SLIDE_INCREASE = 1;  //递增状态

    private boolean isIncrease = true;

    private float width;
    private float height;
    private float center;
    private float x;

    private float y = 20;    //圆环宽度
    private float y_x;      //圆环宽度比

    private View child;     //textview
    private int tWidth;     //文本宽度
    private int tHeight;    //文本高度
    private float tX;       //文本宽度变化值
    private float tX_x;     //文本拉伸变化比

    private Bitmap openIcon, closeIcon;
    private float iconWidth;  //图片宽度

    private float d_x;       //旋转比
    private float rotateDegrees;    //旋转差值

    private Paint paint;
    private RectF rectF;

    private FoldListener foldListener;  //折叠监听
    private OnClickListener onClickListener;  //点击监听

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case IS_SLIDE_DECREASE: //递减状态
                    x -= speed;
                    if (x >= center + speed) {
                        y = y_x * x;            //得到放大值
                        tX = tX_x * x;       //得到textView宽度值
                        rotateDegrees = d_x * x;     //得到旋转角度
                        mHandler.sendEmptyMessageDelayed(IS_SLIDE_DECREASE, 1);
                    } else {
                        //动画结束 恢复默认状态
                        x = center;
                        y = 0;
                        tX = center * 2 + 5;
                        rotateDegrees = 0;
                        setEnabled(true);
                        //折叠回调
                        if (foldListener != null) {
                            foldListener.onFold(isIncrease, sfb);
                        }
                    }
                    break;
                case IS_SLIDE_INCREASE: //递增状态
                    x += speed;
                    if (x < width - center) {
                        y = y_x * x;      //得到缩小值
                        tX = tX_x * x;
                        rotateDegrees = d_x * x;
                        mHandler.sendEmptyMessageDelayed(IS_SLIDE_INCREASE, 1);
                    } else {
                        //动画结束 恢复默认状态
                        x = width - center;
                        y = 20;
                        rotateDegrees = degrees;
                        tX = tWidth + center * 2 + 10;
                        setEnabled(true);

                        //折叠回调
                        if (foldListener != null) {
                            foldListener.onFold(isIncrease, sfb);
                        }
                    }
                    break;
            }
            requestLayout();
            invalidate();
        }
    };

    public FlodableButton(Context context) {
        this(context, null);
    }

    public FlodableButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        widWidth = wm.getDefaultDisplay().getWidth();
        sfb = this;

        TypedArray type = context.obtainStyledAttributes(attrs, R.styleable.FlodableButton);
        bacColor = type.getColor(R.styleable.FlodableButton_bac_color, Color.YELLOW);
        circleleColor = type.getColor(R.styleable.FlodableButton_inner_circle_color, Color.BLACK);
        textColor = type.getColor(R.styleable.FlodableButton_text_color, Color.BLACK);
        textSize = type.getFloat(R.styleable.FlodableButton_text_size, 20);
        text = type.getString(R.styleable.FlodableButton_text);
        speed = type.getFloat(R.styleable.FlodableButton_speed, 80);
        degrees = type.getFloat(R.styleable.FlodableButton_degrees, 90);
        BitmapDrawable dra = (BitmapDrawable) type.getDrawable(R.styleable.FlodableButton_open_icon);
        openIcon = dra != null ? dra.getBitmap() : BitmapFactory.decodeResource(getResources(), R.drawable.icon);
        BitmapDrawable dra2 = (BitmapDrawable) type.getDrawable(R.styleable.FlodableButton_close_icon);
        closeIcon = dra2 != null ? dra2.getBitmap() : BitmapFactory.decodeResource(getResources(), R.drawable.icon_2);
        type.recycle();

        if (speed < 10){
            speed = 10;
        }
        if (textSize < 12){
            textSize = 12;
        }

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.WHITE);

        TextView tv = new TextView(context);
        tv.setText(text);
        tv.setTextSize(textSize);
        tv.setTextColor(textColor);
        addView(tv);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        if (width == 0) {
            //获取TextView控件
            child = this.getChildAt(0);
            measureChild(child, widthMeasureSpec, heightMeasureSpec);
            tWidth = child.getMeasuredWidth();
            tHeight = child.getMeasuredHeight();

            //根据模式设置宽高
            setWidthAdnHeight(widthMeasureSpec,heightMeasureSpec);

            //获取圆的半径
            center = height / 2;
            //矩形右边x坐标
            x = width - center;
            //初始化圆环宽度比
            y_x = y / x;

            //初始化文本范围右下角 x坐标  +10设置间距
            tX = tWidth + center * 2 + 10;
            //初始文本伸缩比
            tX_x = (tWidth + 10) / (width - center * 2);

            //图片宽度
            iconWidth = center - y - 5;
            openIcon = MeasureUtil.zoomImg(openIcon, (int) iconWidth, (int) iconWidth);
            closeIcon = MeasureUtil.zoomImg(closeIcon, (int) iconWidth, (int) iconWidth);

            rotateDegrees = degrees;
            //初始化旋转比
            d_x = rotateDegrees / x;
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private void setWidthAdnHeight(int widthMeasureSpec, int heightMeasureSpec) {
        int hMode = MeasureSpec.getMode(heightMeasureSpec);
        int wMode = MeasureSpec.getMode(widthMeasureSpec);
        if (wMode == MeasureSpec.AT_MOST) {
            if (tWidth < 100){
                width = tWidth + 150;
            }else if(tWidth > 100 && tWidth *1.5f<widWidth -100){
                width = tWidth*1.5f;
            }else{
                width = widWidth-100;
            }
        } else {
            width = MeasureSpec.getSize(widthMeasureSpec);
        }
        if (hMode == MeasureSpec.AT_MOST) {
            height = tHeight*2;
        } else {
            height = MeasureSpec.getSize(heightMeasureSpec);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        paint.setColor(bacColor);
        //画左边圆
        canvas.drawCircle(center, center, center, paint);

        //画矩形
        rectF = new RectF(center, 0, x, height);
        canvas.drawRect(rectF, paint);

        //画右边圆
        canvas.drawCircle(x, center, center, paint);

        //画小圆
        paint.setColor(circleleColor);
        canvas.drawCircle(center, center, center - y, paint);

        canvas.save();
        canvas.rotate((-degrees + rotateDegrees), center, center);
        if (rotateDegrees == 0) {  //旋转完变换图片
            canvas.drawBitmap(closeIcon, center - iconWidth / 2, center - iconWidth / 2, paint);
        } else {
            canvas.drawBitmap(openIcon, center - iconWidth / 2, center - iconWidth / 2, paint);
        }
        canvas.restore();
        super.dispatchDraw(canvas);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        child.layout((int) (center * 2 + 5), (int) (center - tHeight / 2)
                , (int) tX, (int) (center + tHeight / 2));
    }

    private boolean canClick;

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                canClick = judgeCanClick(event.getX(), event.getY());
                if (!canClick)
                    return super.onTouchEvent(event);
                break;
            case MotionEvent.ACTION_UP:
                if (canClick && onClickListener != null) {
                    onClickListener.onClick(sfb);
                }
                break;
        }
        return true;
    }

    private boolean judgeCanClick(float x, float y) {
        boolean canClick;
        if (isIncrease)     //伸展状态
            if (x < width && y < height)
                canClick = true;
            else
                canClick = false;
        else {
            if (x < center * 2 && y < center * 2)  //在圆内
                canClick = true;
            else
                canClick = false;
        }

        return canClick;
    }

    public boolean isIncrease() {
        return isIncrease;
    }

    //递减状态
    private void startDecrease() {
        setEnabled(false);  //滑动时不给点击事件
        isIncrease = false; //记录递增还是递减状态
        mHandler.sendEmptyMessageDelayed(IS_SLIDE_DECREASE, 40);
    }

    //递增状态
    private void startIncrease() {
        setEnabled(false);  //滑动时不给点击事件
        isIncrease = true;  //记录递增还是递减状态
        mHandler.sendEmptyMessageDelayed(IS_SLIDE_INCREASE, 40);
    }

    //外部调用
    public void startScroll() {
        if (isIncrease) {    //判断是否是递增状态
            startDecrease();
        } else {
            startIncrease();
        }
    }

    public void setFoldListener(FoldListener foldListener) {
        this.foldListener = foldListener;
    }

    public interface FoldListener {
        void onFold(boolean isIncrease, FlodableButton sfb);
    }

    public void setOnClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public interface OnClickListener {
        void onClick(FlodableButton sfb);
    }
}
