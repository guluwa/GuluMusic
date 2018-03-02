package cn.guluwa.gulumusic.utils

import java.util.ArrayList
import java.util.Random

/**
 * Created by 俊康 on 2018/2/1.
 */

class RandomPicker {

    private val mOriginWeightList = ArrayList<Int>()
    private val mCurrentWeightList = ArrayList<Int>()
    /*获取历史条目的位置列表*/
    private val historyList = ArrayList<Int>()

    private var mMultiplyNumber = 1
    private var mAddNumber = 1
    private var mPickedPosition: Int = 0
    private var isRepeatable: Boolean = false
    private var mNextPickPosition: Int? = null
    internal var mRandom = Random()

    /*获得当前条目数*/
    val size: Int
        get() = mOriginWeightList.size

    constructor() {
        //默认一个，避免报错。
        RandomPicker(1)
    }

    constructor(size: Int) {
        initSize(size)
    }

    /*设置累乘积数*/
    fun setMultiplyNumber(multiplyNumber: Int) {
        mMultiplyNumber = multiplyNumber
    }

    /*设置累加积数*/
    fun setAddNumber(addNumber: Int) {
        mAddNumber = addNumber
    }

    /*指定下一次选中的位置*/
    fun setNextPick(pickedPosition: Int) {
        mNextPickPosition = pickedPosition
    }

    /*是否允许连续两次出现同一个位置*/
    fun setRepeatable(repeatable: Boolean) {
        isRepeatable = repeatable
    }

    /*初始化列表长度*/
    private fun initSize(size: Int) {
        mOriginWeightList.clear()
        mCurrentWeightList.clear()
        historyList.clear()
        for (i in 0 until size) {
            add()
        }
    }


    /*上为配置参数*/
    /*下为逻辑实现*/


    /*获得下一个随机条目的位置*/
    operator fun next(): Int {
        random()
        historyList.add(mPickedPosition)
        return mPickedPosition
    }

    /*添加一个条目*/
    @JvmOverloads
    fun add(index: Int = size, weight: Int = 1) {
        mOriginWeightList.add(index, weight)
        mCurrentWeightList.add(index, calculateWeight(0, weight))
    }

    /*修改一个条目的比重*/
    fun changeOriginWeight(index: Int, weight: Int) {
        mOriginWeightList[index] = weight
        val currentWeight = mCurrentWeightList[index]
        mCurrentWeightList[index] = currentWeight / mOriginWeightList[index] * weight
    }

    /*移除一个条目*/
    fun remove(index: Int) {
        mOriginWeightList.removeAt(index)
        mCurrentWeightList.removeAt(index)
    }

    /*执行随机算法*/
    private fun random() {
        // 算出下一次选中的位置
        if (mNextPickPosition != null) {
            mPickedPosition = mNextPickPosition!!
            mNextPickPosition = null
        } else {
            var allCount: Long = 0
            for (i in mCurrentWeightList.indices) {
                allCount += mCurrentWeightList[i].toLong()
            }

            val randomLong = (mRandom.nextDouble() * allCount).toLong()
            var currentLong: Long = 0
            for (i in mCurrentWeightList.indices) {
                currentLong += mCurrentWeightList[i].toLong()
                if (currentLong > randomLong) {
                    mPickedPosition = i
                    break
                }
            }
        }

        // 若列表长度小于2，则下一次位置必为0.
        if (mCurrentWeightList.size < 2) {
            mPickedPosition = 0
            return
        }

        // 预先算好下一次的比重
        for (i in mCurrentWeightList.indices) {
            val weight = calculateWeight(mCurrentWeightList[i], mOriginWeightList[i])
            mCurrentWeightList[i] = weight
        }
        if (isRepeatable) {
            mCurrentWeightList[mPickedPosition] = calculateWeight(0, mOriginWeightList[mPickedPosition])
        } else {
            mCurrentWeightList[mPickedPosition] = 0
        }
    }

    /*计算下一次的比重*/
    private fun calculateWeight(currentWeight: Int, originWeight: Int): Int {
        return (currentWeight + mAddNumber) * mMultiplyNumber * originWeight
    }

}// 默认每个条目的比重为1.
