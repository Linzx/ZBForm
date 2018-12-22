package com.zbform.penform.view;

import android.animation.Animator;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewParent;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zbform.penform.animation.AnimationHelper;
import com.zbform.penform.R;

/**
 */
public class TransitionView extends RelativeLayout {

    private View v_spread; // 播放扩散动画的view
    private View v_line;
    private TextView tv_sign_up;
    private TextView tv_success;
    private View parent;
    public boolean mSuccess = false;
    public boolean mSignEnd = false;
    private float mStartScale;
    private int mOldWidth;

    private OnAnimationEndListener mOnAnimationEndListener;
    private OnAnimationEndListener mOnLoginFailEndListener;

    public TransitionView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TransitionView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        //允许绘制背景，及执行onDraw()方法
//        setWillNotDraw(false);

        init();
    }

    private void init() {
        View mRootView = inflate(getContext(), R.layout.view_transtion, this);

        v_spread = mRootView.findViewById(R.id.v_spread);
        v_line = mRootView.findViewById(R.id.v_line);
        tv_sign_up = (TextView) mRootView.findViewById(R.id.tv_sign_up);
        tv_success = (TextView) mRootView.findViewById(R.id.tv_success);
    }

    public void setParent(View v) {
        parent = v;
    }

    /**
     */
    public void startLoginAni() {
        mSuccess = false;
        mSignEnd = false;
        this.setVisibility(View.VISIBLE);

        tv_sign_up.setTranslationX(0);
        tv_sign_up.setVisibility(View.INVISIBLE);
        tv_success.setVisibility(View.INVISIBLE);
        v_line.setVisibility(View.INVISIBLE);

        //缩放动画
        AnimationHelper.spreadAni(v_spread, getScale(), new AnimationHelper.SimpleAnimatorListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                //结束后播放sign up 文字显示入场动画
                startSignUpInAni();
            }
        });
    }

    public void startLoginFailAni() {

        AnimationHelper.spreadAni(v_spread, mStartScale, new AnimationHelper.SimpleAnimatorListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (mOnLoginFailEndListener != null){
                    mOnLoginFailEndListener.onEnd();
                }
                TransitionView.this.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void startSignUpInAni() {
        AnimationHelper.signUpTextInAni(tv_sign_up, new AnimationHelper.SimpleAnimatorListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                //开启线条播放动画
                startLineAni();
            }
        });
    }

    private void startLineAni() {
        AnimationHelper.lineExpendAni(v_line, new AnimationHelper.SimpleAnimatorListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                //开启success文字入场动画
                mSignEnd = true;
                if(mSuccess) {
                    startSuccessAni();
                } else {
                    startLoginFailAni();
                }

            }
        });
    }

    public void startSuccessAni() {
        AnimationHelper.successInAni(tv_success, tv_sign_up, new AnimationHelper.SimpleAnimatorListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                //回调
                if (mOnAnimationEndListener != null) {
                    mOnAnimationEndListener.onEnd();
                }
            }
        });
    }

    //计算扩散动画最终放大比例
    private float getScale() {
        //原始扩散圆的直径
        mOldWidth = v_spread.getMeasuredWidth();
        Log.i("whd","mOldWidth="+mOldWidth);

        int width = parent == null ? getMeasuredWidth() : parent.getMeasuredWidth();
        int height = parent == null ? getMeasuredWidth() : parent.getMeasuredHeight();

        //扩散圆最终扩散的圆的半径
        float finalDiameter = (int) (Math.sqrt(width * width + height * height));
        mStartScale = mOldWidth / (finalDiameter-1);
        //因为圆未居中，所以加1
        return finalDiameter / mOldWidth + 1;
    }

    public void setOnAnimationEndListener(OnAnimationEndListener onAnimationEndListener) {
        this.mOnAnimationEndListener = onAnimationEndListener;
    }

    public void setOnLoginFailEndListener(OnAnimationEndListener onAnimationEndListener) {
        this.mOnLoginFailEndListener = onAnimationEndListener;
    }

    /**
     */
    public interface OnAnimationEndListener {
        void onEnd();
    }
}
