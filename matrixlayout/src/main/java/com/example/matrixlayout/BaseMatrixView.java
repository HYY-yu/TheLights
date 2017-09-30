package com.example.matrixlayout;

import android.content.Context;

/**
 * Created by yufeng on 17-9-30.
 */

public class BaseMatrixView extends MatrixView {

    public BaseMatrixView(Context context) {
        super(context);
    }

    @Override
    public MatrixView copy() {
        return new BaseMatrixView(getContext());
    }
}
