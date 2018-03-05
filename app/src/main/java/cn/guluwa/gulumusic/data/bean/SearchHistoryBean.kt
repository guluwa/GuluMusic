package cn.guluwa.gulumusic.data.bean

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

/**
 * Created by guluwa on 2018/3/3.
 */

@Entity(tableName = "search_history")
data class SearchHistoryBean(val date: Long, @PrimaryKey var text: String)