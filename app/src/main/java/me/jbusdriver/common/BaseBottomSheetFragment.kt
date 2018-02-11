package me.jbusdriver.common

import android.app.Dialog
import android.os.Bundle
import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.BottomSheetDialogFragment
import android.view.View
import jbusdriver.me.jbusdriver.R


/**
 * Created by Administrator on 2017/6/2 0002.
 */
abstract class BaseBottomSheetFragment : BottomSheetDialogFragment() {

    protected val mBehavior by lazy { BottomSheetBehavior.from(dialog.findViewById(R.id.design_bottom_sheet) as View) }
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {

        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

    }

}