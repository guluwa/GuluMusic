package cn.guluwa.gulumusic.utils

import android.os.Environment
import android.util.LongSparseArray

import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.util.ArrayList
import java.util.HashMap
import java.util.regex.Matcher
import java.util.regex.Pattern

import cn.guluwa.gulumusic.data.bean.LrcBean

/**
 * Created by guluwa on 2018/1/25.
 */

object LrcParser {

    private var currentTime: Long = 0//存放临时时间
    private var currentContent: String? = null//存放临时歌词
    private var lrcBeans: MutableList<LrcBean>? = null//用户保存所有的歌词和时间点信息间的映射关系的List

    @Throws(Exception::class)
    fun parserLocal(name: String): List<LrcBean> {
        val `in` = readLrcFile(Environment.getExternalStorageDirectory().absolutePath + "/gulu_music/word/" + name)
        return parser(`in`)
    }

    @Throws(IOException::class)
    fun parserRemote(str: String): List<LrcBean> {
        val `in` = ByteArrayInputStream(str.toByteArray())
        return parser(`in`)
    }

    /**
     * 根据文件路径，读取文件，返回一个输入流
     *
     * @param path 路径
     * @return 输入流
     * @throws FileNotFoundException
     */
    @Throws(FileNotFoundException::class)
    private fun readLrcFile(path: String): InputStream {
        val f = File(path)
        return FileInputStream(f)
    }

    /**
     * 将输入流中的信息解析，返回一个LrcInfo对象
     *
     * @param inputStream 输入流
     * @return 解析好的LrcInfo对象
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun parser(inputStream: InputStream): List<LrcBean> {
        // 三层包装
        val inr = InputStreamReader(inputStream)
        val reader = BufferedReader(inr)
        // 一行一行的读，每读一行，解析一行
        var line: String?
        lrcBeans = ArrayList()
        while (true) {
            line = reader.readLine()
            if (line != null)
                parserLine(line)
            else
                break
        }
        reader.close()
        inr.close()
        inputStream.close()
        // 全部解析完后，设置info
        return lrcBeans as ArrayList<LrcBean>
    }

    /**
     * 利用正则表达式解析每行具体语句
     * 并在解析完该语句后，将解析出来的信息设置在LrcInfo对象中
     *
     * @param str
     */
    private fun parserLine(str: String) {
        // 设置正则规则
        val reg1 = "\\[(\\d{2}:\\d{2}\\.\\d{2})\\]"
        val reg2 = "\\[(\\d{2}:\\d{2}\\.\\d{3})\\]"
        // 编译
        val pattern1 = Pattern.compile(reg1)
        val pattern2 = Pattern.compile(reg2)
        val matcher1 = pattern1.matcher(str)
        val matcher2 = pattern2.matcher(str)

        // 如果存在匹配项，则执行以下操作
        while (matcher1.find()) {

            // 得到这个匹配项中的组数
            val groupCount = matcher1.groupCount()
            // 得到每个组中内容
            for (i in 0..groupCount) {
                val timeStr = matcher1.group(i)
                if (i == 1) {
                    // 将第二组中的内容设置为当前的一个时间点
                    currentTime = strToLong2(timeStr)
                }
            }

            // 得到时间点后的内容
            val content = pattern1.split(str)
            // 输出数组内容
            for (i in content.indices) {
                if (i == content.size - 1) {
                    // 将内容设置为当前内容
                    currentContent = content[i]
                }
            }
            // 设置时间点和内容的映射
            lrcBeans!!.add(LrcBean(currentTime, currentContent))
        }
        // 如果存在匹配项，则执行以下操作
        while (matcher2.find()) {

            // 得到这个匹配项中的组数
            val groupCount = matcher2.groupCount()
            // 得到每个组中内容
            for (i in 0..groupCount) {
                val timeStr = matcher2.group(i)
                if (i == 1) {
                    // 将第二组中的内容设置为当前的一个时间点
                    currentTime = strToLong3(timeStr)
                }
            }

            // 得到时间点后的内容
            val content = pattern2.split(str)
            // 输出数组内容
            for (i in content.indices) {
                if (i == content.size - 1) {
                    // 将内容设置为当前内容
                    currentContent = content[i]
                }
            }
            // 设置时间点和内容的映射
            lrcBeans!!.add(LrcBean(currentTime, currentContent))
        }
    }

    /**
     * 将解析得到的表示时间的字符转化为Long型
     *
     * @param timeStr 字符形式的时间点
     * @return Long形式的时间
     */
    private fun strToLong2(timeStr: String): Long {
        // 因为给如的字符串的时间格式为XX:XX.XX,返回的long要求是以毫秒为单位
        // 1:使用：分割 2：使用.分割
        val s = timeStr.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val min = Integer.parseInt(s[0])
        val ss = s[1].split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val sec = Integer.parseInt(ss[0])
        val mill = Integer.parseInt(ss[1])
        return (min * 60 * 1000 + sec * 1000 + mill * 10).toLong()
    }

    private fun strToLong3(timeStr: String): Long {
        // 因为给如的字符串的时间格式为XX:XX.XX,返回的long要求是以毫秒为单位
        // 1:使用：分割 2：使用.分割
        val s = timeStr.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val min = Integer.parseInt(s[0])
        val ss = s[1].split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val sec = Integer.parseInt(ss[0])
        val mill = Integer.parseInt(ss[1])
        return (min * 60 * 1000 + sec * 1000 + mill).toLong()
    }

}
