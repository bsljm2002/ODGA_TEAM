package com.jongmyeong.odga

import android.app.Service
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.IBinder
import java.io.*
import java.lang.Exception
import java.net.URL

class MusicService : Service() {
    lateinit var mp: MediaPlayer
    val fourth: String = "7"

    override fun onBind(intent: Intent): IBinder {
        // Service 객체와 통신할 때 사용
        TODO("Return the communication channel to the service.")
    }

    /////////////////////////////////////////////////////////////////////////
    // 서비스가 호출되었을 때 한번만 호출
    override fun onCreate() {
        super.onCreate()
        Thread(Runnable {
            musicUrls()
//            println(list_)
//            return list_
        }).start()
        println("555555555555555555555555555555555555555555555555555555555555555")

        val file = File("/data/data/com.jongmyeong.odga/now.txt")
        while(true) {
            try {
                val fileReader = FileReader(file)
                println("555555555555555555555555555555555555555555555555555555555555555")

                val bufferedReader = BufferedReader(fileReader)
                //        val str = bufferedReader.readLine()
                //        println(str)
                println("555555555555555555555555555555555555555555555555555555555555555")

                val String = bufferedReader.use { it.readText() }
                val list_ = String.split("\n")
                println("555555555555555555555555555555555555555555555555555555555555555")

                val lineNumberReader = LineNumberReader(fileReader)
                //        val lineSize = bufferedReader.use{ it.readText() }
                //        println(lineSize)
                val ranNum = (1..list_.size - 1).random()
                lineNumberReader.setLineNumber(ranNum)
                val lineNum = lineNumberReader.getLineNumber()
                //        var str = lineNumberReader.readLine()
                //        println(str)
                println("555555555555555555555555555555555555555555555555555555555555555")

                println(ranNum)
                println(lineNum)
                println(list_[ranNum])

                //        val scanner = Scanner(File("/data/data/org.techtown.servicetest/now.txt"))
                //        println(scanner.nextLine())

                mp = MediaPlayer()
                mp.setAudioStreamType(AudioManager.STREAM_MUSIC)
        //        mp.setDataSource("http://localhost:8000/play/")
                println("555555555555555555555555555555555555555555555555555555555555555")

                mp.setDataSource(list_[ranNum])
        //        mp.setDataSource(applicationContext, "/data/media/0/Android/media/mp3/Tobu - Lost [NCS Release].mp3")
                mp.prepare()
                println("555555555555555555555555555555555555555555555555555555555555555")

                mp.isLooping = false // 반복재생
                mp.start()
                fileReader.close()
                lineNumberReader.close()
                break
            } catch (e: Exception)  {
                println(e)
                continue
            }
        }
    }
    /////////////////////////////////////////////////////////////////////////

    // 서비스가 호출될때마다 호출 (음악재생)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        mp.start()
        return super.onStartCommand(intent, flags, startId)
    }

    // 서비스가 종료될 때 음악 종료
    override fun onDestroy() {
        super.onDestroy()
        mp.stop()
//        mp.reset()
    }


    fun musicUrls() {
        var pathh = "http://192.168.0.$fourth:8000/media/mp3/__now__/now.txt"
        val urll = URL(pathh)
        val urlss = urll.openStream().bufferedReader().use { it.readText() }

        var path = "http://192.168.0.$fourth:8000/media/mp3/__now__/" + urlss + ".txt"
        val url = URL(path)
        val urls = url.openStream().bufferedReader().use { it.readText() }
//        println(urls)

//        val bufwr = BufferedWriter(FileWriter(File("/assets/now.txt")))
//        bufwr.write(urls)
        println("8888888888888888888888888888888888888888888888888888888888888")

        val list = urls.split("\n")
//        println(list)
//        println(list[(1..list.size-1).random()])
//        println(list.shuffled())

        println("8888888888888888888888888888888888888888888888888888888888888")

        val file = File("/data/data/com.jongmyeong.odga/now.txt")
        val fileWriter = FileWriter(file, false)
        val bufferedWriter = BufferedWriter(fileWriter)
        println("8888888888888888888888888888888888888888888888888888888888888")

        bufferedWriter.append(urls)
        bufferedWriter.close()
//        return list.shuffled()
//        return urls
        println("8888888888888888888888888888888888888888888888888888888888888")

    }
}