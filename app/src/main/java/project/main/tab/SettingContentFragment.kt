package project.main.tab

import android.os.Bundle
import android.view.View
import com.buddha.qrcodeweb.databinding.FragmentSettingContentBinding
import project.main.base.BaseFragment
import project.main.model.SettingDataItem

class SettingContentFragment(val settingData: SettingDataItem, val position: Int) : BaseFragment<FragmentSettingContentBinding>(FragmentSettingContentBinding::inflate) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initData()

        initView()

        initValue()

        initEvent()

    }

    private fun initData() {

    }

    private fun initView() {
        mBinding.tvSettingName.text = settingData.name + position

    }

    private fun initValue() {

    }

    private fun initEvent() {

    }

//    companion object {
//        /**
//         * Use this factory method to create a new instance of
//         * this fragment using the provided parameters.
//         *
//         * @param param1 Parameter 1.
//         * @param param2 Parameter 2.
//         * @return A new instance of fragment SettingContentFragment.
//         */
//        // TODO: Rename and change types and number of parameters
//        @JvmStatic
//        fun newInstance(param1: String, param2: String) =
//            SettingContentFragment().apply {
//                arguments = Bundle().apply {
//                    putString(ARG_PARAM1, param1)
//                    putString(ARG_PARAM2, param2)
//                }
//            }
//    }
}