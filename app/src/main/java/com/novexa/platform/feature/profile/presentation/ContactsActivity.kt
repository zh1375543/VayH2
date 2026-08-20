package com.novexa.platform.feature.profile.presentation

import androidx.activity.viewModels
import com.novexa.platform.core.ui.base.BaseActivity
import com.novexa.platform.databinding.ActivityContactsBinding
import com.novexa.platform.feature.dashboard.model.CustomerContactConfig
import com.novexa.platform.feature.profile.presentation.adapter.ContactWayAdapter
import com.novexa.platform.feature.dashboard.presentation.VisitorPortalViewModel
import com.novexa.platform.core.common.util.viewBinding

class ContactsActivity : BaseActivity<ActivityContactsBinding>() {

    override val binding by viewBinding(ActivityContactsBinding::inflate)
    private val vm by viewModels<VisitorPortalViewModel>()

    private val contactAdapter by lazy { ContactWayAdapter() }

    override fun initView() = with(binding) {
        rvCustomer.adapter = contactAdapter
        vm.getUnAuthData()
    }

    override fun initObserve() = with(vm) {
        super.initObserve()
        result.observe(this@ContactsActivity) {
            val list = mutableListOf<CustomerContactConfig>()
            it?.customerPhone?.let { phone ->
                list.add(
                    CustomerContactConfig(
                        enTitle = "Phone Number",
                        vernacularTitle = "Số điện thoại",
                        content = phone,
                        buttonType = 2
                    )
                )
            }
            it?.customerEmail?.let { email ->
                list.add(
                    CustomerContactConfig(
                        enTitle = "Email",
                        vernacularTitle = "Email",
                        content = email,
                        buttonType = 1
                    )
                )
            }
            it?.customerConfigs?.let { configs ->
                list.addAll(configs)
            }
            contactAdapter.submitItems(list)
        }
    }
}
