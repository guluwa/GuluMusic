package cn.guluwa.gulumusic.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import cn.guluwa.gulumusic.R
import cn.guluwa.gulumusic.adapter.SongMoreOperationAdapter
import cn.guluwa.gulumusic.listener.OnSelectListener
import kotlinx.android.synthetic.main.song_more_operation_dialog.*

/**
 * Created by guluwa on 2018/3/7.
 */
class SongMoreOperationDialog(context: Context?, themeResId: Int,
                              private var listener: OnSelectListener,
                              private var list: List<String>) :
        Dialog(context, themeResId) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.song_more_operation_dialog)
        initView()
    }

    private fun initView() {
        setCancelable(true)
        window.setWindowAnimations(R.style.bottom_menu_animation)
        mRecyclerView.adapter = SongMoreOperationAdapter(list, listener)
        mRecyclerView.layoutManager = LinearLayoutManager(context)
    }
}