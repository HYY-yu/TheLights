package com.example.matrixlayout;

import android.support.annotation.Nullable;

import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Set;

/**
 * 一个矩阵的操作类，提供变换历史记录和复现。
 * Created by yufeng on 17-9-27.
 */

public class Matrix {
    private int[][] matrix;

    //变换历史
    class TransformHistory {
        int x;
        int y;
        int oldElem;
        int newElem;
    }

    public interface Viewer {
        /**
         * @param step    当前步数
         * @param x
         * @param y
         * @param oldElem
         * @param newElem
         * @param total   共有几步
         */
        void oneStep(int step, int x, int y, int oldElem, int newElem, int total);
    }


    private Set<TransformHistory> transformHistorySet; // 表示一组变换操作

    private boolean isTransform = false;

    public void startTransform() {
        isTransform = true;
        //清空
        transformHistorySet = new LinkedHashSet<>();
    }

    public void endTransform() {
        isTransform = false;
        //记录 这组操作
        transformHistoryDeque.addLast(transformHistorySet);
    }

    private Deque<Set<TransformHistory>> transformHistoryDeque = new LinkedList<>();

    public Matrix(int[][] matrix) {
        this.matrix = matrix;
    }

    public int get(int x, int y) {
        return matrix[x][y];
    }

    public int set(int x, int y, int elem) {
        if (isTransform) {
            TransformHistory transformHistory = new TransformHistory();
            transformHistory.x = x;
            transformHistory.y = y;
            transformHistory.oldElem = matrix[x][y];
            transformHistory.newElem = elem;

            //添加到变换历史集合
            transformHistorySet.add(transformHistory);
        }

        return matrix[x][y] = elem;
    }

    /**
     * 回退上组操作 并从历史记录中删除它
     *
     * @return
     */
    public boolean back(@Nullable Viewer viewer) {
        if (transformHistoryDeque.size() > 0) {
            Set<TransformHistory> oneSet = transformHistoryDeque.removeLast();
            forEachTransform(viewer, oneSet);
            return true;
        } else {
            return false;
        }
    }

    private void forEachTransform(Viewer viewer, Set<TransformHistory> oneSet) {
        //遍历
        int total = oneSet.size();
        if (total > 0) {
            int i = 1;
            for (TransformHistory transformHistory :
                    oneSet) {
                if (viewer != null) {
                    viewer.oneStep(i++, transformHistory.x, transformHistory.y, transformHistory.oldElem, transformHistory.newElem, total);
                }
            }
        }
    }

    /**
     * 回放上组操作 此操作不会被删除
     *
     * @return
     */
    public boolean review(@Nullable Viewer viewer) {
        if (transformHistoryDeque.size() > 0) {
            Set<TransformHistory> oneSet = transformHistoryDeque.peekLast();
            //遍历
            forEachTransform(viewer, oneSet);
            return true;
        } else {
            return false;
        }
    }

    /**
     * 重置矩阵
     */
    public void reset() {
        while (true) {
            if (!back(new Viewer() {
                @Override
                public void oneStep(int step,int x,int y,int oldElem,int newElem,int total) {
                    set(x, y, oldElem);
                }
            })) {
                break;
            }
        }
    }

    /**
     * 二维数组展开到一维
     */
    public int find(int x, int y) {
        return (x * xLen()) + y;
    }

    public int size() {
        return xLen() * yLen();
    }

    public int yLen() {
        return matrix[0].length;
    }

    public int xLen() {
        return matrix.length;
    }

    /**
     * 重现器， 重现矩阵的整个变换操作组的历史。
     */
    class Reproducer {
        Deque<Set<TransformHistory>> transformHistoryCopy;

        public Reproducer(Deque<Set<TransformHistory>> transformHistoryDeque) {
            this.transformHistoryCopy = new LinkedList<>(transformHistoryDeque);
            reset();
        }

        public int historySize() {
            return transformHistoryCopy.size();
        }

        public boolean reproduce(@Nullable Viewer viewer) {
            if (transformHistoryCopy.size() > 0) {
                Set<TransformHistory> oneSet = transformHistoryCopy.removeFirst();
                forEachTransform(viewer, oneSet);
                return true;
            } else {
                return false;
            }
        }
    }

    public Reproducer reproducer() {
        return new Reproducer(transformHistoryDeque);
    }
}
