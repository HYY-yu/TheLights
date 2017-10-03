package com.example.yufeng.thelights

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.example.matrixlayout.BaseMatrixView
import com.example.matrixlayout.Matrix
import com.yvelabs.chronometer2.utils.FontUtils
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.ctx
import org.jetbrains.anko.sp
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {

    val handler = Handler()
    val viewList = ArrayList<View>()
    var step = 0
    lateinit var fontLobster: Typeface

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        fontLobster = FontUtils.getTypeface(
                ctx,"fonts/segoeprb.ttf")

        val lightOn = getDrawable(R.drawable.bg_light_on)
        val lightOff = getDrawable(R.drawable.bg_light_off)

        val matrixData = Array(8) { IntArray(8) { 0 } }

        tvStepCount.typeface = fontLobster
        tvStepCount.text = step.toString()
        tvStepCount.setTextColor(Color.WHITE)
        tvStepCount.textSize = sp(12).toFloat()

        matrixLayout.setMatrix(matrixData)
                .setMatrixView(BaseMatrixView(matrixLayout.context))
                .setMatrixOnClickListener { view, matrix ->
                    run {

                        val x = view.matrixX
                        val y = view.matrixY

                        //动画
                        viewList.clear()
                        viewList.add(view)

                        matrix.startTransform()

                        matrix.set(x, y, reverseElem(matrix, x, y))

                        //上下左右
                        if (0 <= x - 1 && x - 1 < matrix.xLen()) {
                            matrix.set(x - 1, y, reverseElem(matrix, x - 1, y))
                            viewList.add(matrixLayout.getChildAt(matrix.find(x - 1, y)))
                        }
                        if (0 <= x + 1 && x + 1 < matrix.xLen()) {
                            matrix.set(x + 1, y, reverseElem(matrix, x + 1, y))
                            viewList.add(matrixLayout.getChildAt(matrix.find(x + 1, y)))
                        }
                        if (0 <= y - 1 && y - 1 < matrix.yLen()) {
                            matrix.set(x, y - 1, reverseElem(matrix, x, y - 1))
                            viewList.add(matrixLayout.getChildAt(matrix.find(x, y - 1)))
                        }
                        if (0 <= y + 1 && y + 1 < matrix.yLen()) {
                            matrix.set(x, y + 1, reverseElem(matrix, x, y + 1))
                            viewList.add(matrixLayout.getChildAt(matrix.find(x, y + 1)))
                        }
                        matrix.endTransform()

                        for (v in viewList) {
                            playViewAnim(v)
                        }

                        matrixLayout.refreshTransform()
                        step++
                        tvStepCount.text = step.toString()
                    }
                }
                .addStatus(0, lightOff)
                .addStatus(1, lightOn)
                .refresh()

        btnStartAuto.setOnClickListener { v ->
            //随机选择一个方块，点击
            //执行n次
            handler.postDelayed(MyRunnable(), 2000)
        }

        btnBack.setOnClickListener {
            //回退
            matrixLayout.matrix().back { step, x, y, oldElem, newElem, total ->
                val matrix = matrixLayout.matrix()
                matrix.set(x, y, oldElem)
            }
            matrixLayout.refresh()
            step--
            tvStepCount.text = step.toString()
        }



        btnReset.setOnClickListener {
            matrixLayout.matrix().reset()
            matrixLayout.refresh()
            step = 0
            tvStepCount.text = step.toString()
            chronometer.reset()
        }

        chronometer.setTypeFace(fontLobster)
        chronometer.setTextSize(sp(14).toFloat())
        chronometer.setTextColor(Color.WHITE)
        chronometer.setTextBold(false)

        btnStartGame.setOnClickListener {
            chronometer.start()
        }
    }

    private fun playViewAnim(v: View) {
        YoYo.with(Techniques.BounceIn)
                .duration(300)
                .playOn(v)
        YoYo.with(Techniques.FadeIn)
                .duration(300)
                .playOn(v)
    }

    inner class MyRunnable : Runnable {
        override fun run() {
            randomSelectMatrixElem(matrixLayout.matrix())
            handler.postDelayed(this, 2000)
        }
    }

    private fun randomSelectMatrixElem(matrix: Matrix) {
        val random = Random(System.currentTimeMillis())
        val randomX = random.nextInt(matrix.xLen())
        val randomY = random.nextInt(matrix.yLen())

        matrixLayout.getChildAt(matrix.find(randomX, randomY)).performClick()
    }

    private fun reverseElem(matrix: Matrix, x: Int, y: Int) = if (matrix.get(x, y) == 0) 1 else 0
}
