package com.example.matrixlayout;

import android.content.Context;
import android.view.View;

/**
 * 对MatrixView有特殊需求可继承MatrixView实现copy()方法  (注意 MatrixView 必须为正方形)
 *
 * Created by yufeng on 17-9-27.
 */

public abstract class MatrixView extends View {

    private int matrixX;
    private int matrixY;

    public MatrixView(Context context) {
        super(context);
    }

    public int getMatrixX() {
        return matrixX;
    }

    public void setMatrixX(int matrixX) {
        this.matrixX = matrixX;
    }

    public int getMatrixY() {
        return matrixY;
    }

    public void setMatrixY(int matrixY) {
        this.matrixY = matrixY;
    }

    /**
     * 深拷贝一个相同的View
     */
    public abstract MatrixView copy();
}
