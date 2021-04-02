package com.utopia.recovery.view;

import android.content.Context;
import android.widget.Button;
import android.widget.TextView;

import com.utopia.mvp.view.BaseView;
import com.utopia.recovery.R;

public class RecoveryView extends BaseView {
    private TextView tvRecovery;
    private Button btnRescue;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_recovery;
    }

    @Override
    protected void init(Context context) {
        this.tvRecovery = findViewById(R.id.tv_recovery);
        this.btnRescue = findViewById(R.id.btn_rescue);
    }

    public TextView getTvRecovery() {
        return tvRecovery;
    }

    public Button getBtnRescue() {
        return btnRescue;
    }
}
