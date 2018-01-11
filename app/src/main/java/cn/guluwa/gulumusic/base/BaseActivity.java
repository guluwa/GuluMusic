package cn.guluwa.gulumusic.base;

import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

/**
 * Created by guluwa on 2018/1/11.
 */

public abstract class BaseActivity extends AppCompatActivity{

    public abstract int getViewLayoutId();

    protected abstract void initViews();

    protected abstract void  initDagger();

    public ViewDataBinding mViewDataBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewDataBinding= DataBindingUtil.setContentView(this,getViewLayoutId());
        initViews();
        initDagger();
    }

    public void showToast(String msg){
        Toast.makeText(this,msg,Toast.LENGTH_SHORT).show();
    }
}
