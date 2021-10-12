package com.jongmyeong.odga

import android.content.res.AssetManager
import android.os.AsyncTask
import kotlinx.coroutines.CoroutineScope
import org.tensorflow.contrib.android.TensorFlowInferenceInterface
import kotlin.coroutines.CoroutineContext

//import android.util.Log;
//import java.util.Arrays;
open class TensorFlowTask(
    assetManager: AssetManager?,  // Assets에 접근하기 위함
    numberOfLabel: Int,  // label의 갯수
    input: ShortArray?
) : AsyncTask<Void, Void, Int>() {
    private var mInputs: ShortArray? = null
    private var mNumberOfLabel = 0
    private var mAssetManager: AssetManager? = null

    /*
        new TensorFlowTask(.....) 하면 위의 위 생성자가 호출됨

        new TensorFlowTask(.....).execute() 하면 위 생성자를 호출한뒤 아래 doInBackground를 호출함
     */

    override fun doInBackground(vararg voids: Void?): Int? {

        /*
            AudioRecord.read를 통해 모았던 로우 데이터를 0.0 ~ 1.0 사이의 값으로 변환시킨다.
         */
        val floatBuffer = FloatArray(mInputs?.size!!)
        for (index in mInputs?.indices!!) {
            floatBuffer[index] = mInputs?.get(index)!!/ Short.MAX_VALUE.toFloat()
        }

        /*
            텐서플로우 관련 작업
         */
        val tensorFlowInferenceInterface =
            TensorFlowInferenceInterface(mAssetManager, App.MODEL_FILENAME)
        tensorFlowInferenceInterface.feed(App.INPUT_SAMPLE_RATE_NAME.toString(), intArrayOf(App.SAMPLE_RATE))
        tensorFlowInferenceInterface.feed(App.INPUT_DATA_NAME.toString(), floatBuffer, App.RECORDING_LENGTH.toLong(), 1)
        tensorFlowInferenceInterface.run(arrayOf(App.OUTPUT_NODE_NAME))
        val outputScores = FloatArray(mNumberOfLabel)
        tensorFlowInferenceInterface.fetch(App.OUTPUT_NODE_NAME, outputScores)
        var targetIndex = 0
        var max = Float.MIN_VALUE
        for (index in outputScores.indices) {
            if (outputScores[index] > max) {
                max = outputScores[index]
                targetIndex = index
            } else {
                continue
            }
        }
        tensorFlowInferenceInterface.close()
        return targetIndex
    }



    /*
        생성자

        작업에 필요한 정보들을 이 생성자를 통해 받음
     */
    init {    // AudioRecord.read를 통해 모았던 로우 데이터
        mInputs = input
        mAssetManager = assetManager
        mNumberOfLabel = numberOfLabel
    }

}
