package com.jongmyeong.odga

import android.app.Application

object App:Application() {
    const val SAMPLE_RATE = 16000
    const val RECORDING_LENGTH = SAMPLE_RATE
    val MODEL_FILENAME: String? = "file:///android_asset/hyunho_frozen_graph.pb"
    val INPUT_DATA_NAME: String? = "decoded_sample_data:0"
    val INPUT_SAMPLE_RATE_NAME: String? = "decoded_sample_data:1"
    val OUTPUT_NODE_NAME: String? = "labels_softmax"
    val NOTIFICATON_CHANNEL_ID: String? = "HH_NOTI_ID"
    val NOTIFICATON_CHANNEL_NAME: String? = "HH_NOTI_NAME"
    val NOTIFICATON_CHANNEL_DESCRIPTION: String? = "HH_NOTI_DESC"
}