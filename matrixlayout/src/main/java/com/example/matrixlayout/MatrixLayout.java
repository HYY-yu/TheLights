package com.example.matrixlayout;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.annotation.Px;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

/**
 * 显示一个整数的矩阵，并且根据矩阵的当前状态（每个元素的值）设置对应的 View 的 Background。
 * 比如： 0 代表 关 1 代表 开
 * Created by yufeng on 17-9-27.
 */

public class MatrixLayout extends ViewGroup implements View.OnClickListener, View.OnLongClickListener {

    private MatrixView matrixView;

    private Matrix matrix = new Matrix(new int[][]{
            {0, 0, 0},
            {0, 1, 0},
            {0, 0, 0}
    });
    private boolean init = false;

    public interface MatrixOnClickListener {
        void onClick(MatrixView view, Matrix matrix);
    }

    public interface MatrixOnLongClickListener {
        boolean onLongClick(MatrixView view, Matrix matrix);
    }

    private MatrixOnClickListener onClickListener;
    private MatrixOnLongClickListener onLongClickListener;

    private SparseArray<Drawable> viewStatusList = new SparseArray<>();

    private int divideWidth = 10;

    int matrixW, matrixH;
    private int matrixViewWH = 90; // 正方形

    public MatrixLayout(Context context) {
        this(context, null);
    }

    public MatrixLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MatrixLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.MatrixLayout);
        divideWidth = array.getDimensionPixelOffset(R.styleable.MatrixLayout_matrix_divider_width, 10);

        array.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // 宽度不限， 看高度： 不限 则使用默认的宽高
        //                  ATMOST 根据传过来的高度设为matrixH ,计算matrixViewWH，并且算出matrixW.
        //                  Exactly 根据传过来的高度设为matrixH ,计算matrixViewWH,并算出matrixW.
        // 宽度 ATMOST， 看高度：不限 根据传过来的宽度设置matrixW，计算MatrixViewWH，并算出matrixH，
        //                     ATMOST 宽高都是ATMOST，比较宽高选择最小值，以此作为matrixW or matrixH 并根据比例缩放，若缩放后不能完全显示，说明要再次缩放。
        //                     Exactly 同ATMOST。
        // 宽度 Exactly ， 同 ATMOST
        // 总之 ，我们的目标是：矩阵要全部显示出来！  （当然，如果View设置了minW,minH 我们也尊重它的意见）
        int w, h;

        matrixW = matrixViewWH * matrix.xLen() + divideWidth * (matrix.xLen() + 1);
        matrixH = matrixViewWH * matrix.yLen() + divideWidth * (matrix.yLen() + 1);
        float matrixRadio = matrix.xLen() / (float) matrix.yLen();

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        if (widthMode == MeasureSpec.UNSPECIFIED) {
            if (heightMode == MeasureSpec.UNSPECIFIED) {
                w = matrixW;
                h = matrixH;
            } else {
                matrixH = h = height;
                matrixW = w = (int) ((matrixH - divideWidth) * matrixRadio) + divideWidth;
                matrixViewWH = (matrixH - divideWidth * (matrix.yLen() + 1)) / matrix.yLen();
            }
        } else {
            if (heightMode == MeasureSpec.UNSPECIFIED) {
                matrixW = w = width;
                matrixH = h = (int) ((matrixW - divideWidth) / matrixRadio) + divideWidth;
                matrixViewWH = (matrixW - divideWidth * (matrix.xLen() + 1)) / matrix.xLen();
            } else {
                w = width;
                h = height;
                if (width < height) {
                    scaleByW(matrixRadio, width);

                    if (matrixH > height) {
                        scaleByH(matrixRadio, height);
                    }
                } else {
                    scaleByH(matrixRadio, height);

                    if (matrixW > width) {
                        scaleByW(matrixRadio, width);
                    }
                }
            }
        }

        if (matrixView != null) {
            int minW = matrixView.getMinimumWidth();
            int minH = matrixView.getMinimumHeight();

            if (minW > 0 || minH > 0) {
                matrixViewWH = Math.max(matrixViewWH, Math.max(minW, minH));
            }
        }

        for (int i = 0; i < matrix.size(); i++) {
            MatrixView view = (MatrixView) getChildAt(i);

            view.measure(MeasureSpec.makeMeasureSpec(matrixViewWH, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(matrixViewWH, MeasureSpec.EXACTLY));
        }

        setMeasuredDimension(w, h);
    }

    private void scaleByH(float matrixRadio, int height) {
        matrixH = height;
        matrixW = (int) ((matrixH - divideWidth) * matrixRadio) + divideWidth;
        matrixViewWH = (matrixH - divideWidth * (matrix.yLen() + 1)) / matrix.yLen();
    }

    private void scaleByW(float matrixRadio, int width) {
        matrixW = width;
        matrixH = (int) ((matrixW - divideWidth) / matrixRadio) + divideWidth;
        matrixViewWH = (matrixW - divideWidth * (matrix.xLen() + 1)) / matrix.xLen();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        //居中布局

        int lastW = (int) ((getMeasuredWidth() - matrixW) * 0.5f) + divideWidth;
        int lastH = (int) ((getMeasuredHeight() - matrixH) * 0.5f) + divideWidth;

        for (int i = 0; i < matrix.size(); i++) {
            MatrixView view = (MatrixView) getChildAt(i);

            int w = view.getMeasuredWidth();
            int h = view.getMeasuredHeight();


            if (i != 0 && i % matrix.xLen() == 0) {
                lastW = (int) ((getMeasuredWidth() - matrixW) * 0.5f) + divideWidth;
                lastH = lastH + h + divideWidth;
            }

            view.layout(lastW, lastH, lastW + w, lastH + h);

            lastW = lastW + w + divideWidth;
        }
    }

    private void initMatrix() {

        for (int i = 0; i < matrix.yLen(); i++) {
            for (int j = 0; j < matrix.xLen(); j++) {
                MatrixView mView = matrixView.copy();

                mView.setMatrixX(i);
                mView.setMatrixY(j);

                mView.setOnClickListener(this);
                mView.setOnLongClickListener(this);

                addView(mView);
            }
        }
    }

    public MatrixLayout setMatrixView(MatrixView matrixView) {
        this.matrixView = matrixView;
        return this;
    }

    public void setDivideWidth(@Px int divideWidth) {
        this.divideWidth = divideWidth;
    }

    public MatrixLayout setMatrixOnClickListener(MatrixOnClickListener onClickListener) {
        this.onClickListener = onClickListener;
        return this;
    }

    public MatrixLayout setMatrixOnLongClickListener(MatrixOnLongClickListener onLongClickListener) {
        this.onLongClickListener = onLongClickListener;
        return this;
    }

    public MatrixLayout setMatrix(Matrix matrix) {
        this.matrix = matrix;
        return this;
    }

    public MatrixLayout setMatrix(int[][] data) {
        return setMatrix(new Matrix(data));
    }

    public Matrix matrix() {
        return matrix;
    }

    public MatrixLayout addStatus(int key, Drawable value) {
        viewStatusList.put(key, value);
        return this;
    }

    public MatrixLayout deleteStatus(int key) {
        viewStatusList.remove(key);
        return this;
    }

    /**
     * 矩阵刷新后，需要调用refresh刷新。
     */
    public void refresh() {
        if (!init) {
            initMatrix();
            init = true;
        }

        for (int i = 0; i < getChildCount(); i++) {
            changeViewBackground(i);
        }
    }

    private void changeViewBackground(int i) {
        MatrixView view = (MatrixView) getChildAt(i);
        Drawable d = viewStatusList.get(matrix.get(view.getMatrixX(), view.getMatrixY()),
                getContext().getResources().getDrawable(R.drawable.bg_not_status));
        view.setBackground(d);
        view.invalidate();
    }

    /**
     * 只刷新上一步被操作(且此操作被记录)的View。 注意这个函数必须在refresh后调用
     */
    public void refreshTransform() {
        matrix.review(new Matrix.Viewer() {
            @Override
            public void oneStep(int step, int x, int y, int oldElem, int newElem, int total) {
                changeViewBackground((x * matrix.xLen()) + y);
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (onClickListener != null) {
            onClickListener.onClick((MatrixView) v, matrix);
        }
    }

    @Override
    public boolean onLongClick(View v) {
        if (onLongClickListener != null) {
            onLongClickListener.onLongClick((MatrixView) v, matrix);
            return true;
        }
        return false;
    }

}
