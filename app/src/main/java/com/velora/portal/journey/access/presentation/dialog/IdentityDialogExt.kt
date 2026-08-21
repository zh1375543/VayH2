package com.velora.portal.journey.access.presentation.dialog

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.velora.portal.platform.design.base.BaseDialog
import com.velora.portal.platform.design.base.BaseSheetDialog
import com.velora.portal.platform.common.data.bean.SelectionOption
import com.velora.portal.databinding.AddressDialogBinding
import com.velora.portal.databinding.DialogIdCardExampleBinding
import com.velora.portal.databinding.DialogSelfieExampleBinding
import com.velora.portal.platform.design.extension.singleClick
import com.velora.portal.journey.access.presentation.profile.ApplicantDetailsViewModel

fun Context.showKycCardExampleDialog(
) {
    object : BaseDialog<DialogIdCardExampleBinding>(
        this,
        DialogIdCardExampleBinding::inflate,
    ) {
        override fun initView() = with(binding) {
            super.initView()
            tvOk.singleClick { dismiss() }
        }
    }.show()
}

fun Context.showKycSelfieExampleDialog(
) {
    object : BaseDialog<DialogSelfieExampleBinding>(
        this,
        DialogSelfieExampleBinding::inflate,
    ) {
        override fun initView() = with(binding) {
            super.initView()
            tvOk.singleClick { dismiss() }
        }
    }.show()
}

fun Context.showAddressPickerDialog(
    vm: ApplicantDetailsViewModel,
    action: (address: String, provinceId: Long?, cityId: Long?, areaId: Long?) -> Unit,
) {
    AddressPickerDialog(this, vm, action).show()
}

private class AddressPickerDialog(
    context: Context,
    private val viewModel: ApplicantDetailsViewModel,
    private val onAddressSelected: (
        address: String,
        provinceId: Long?,
        cityId: Long?,
        areaId: Long?,
    ) -> Unit,
) : BaseSheetDialog<AddressDialogBinding>(context, AddressDialogBinding::inflate) {

    private enum class AddressLevel {
        PROVINCE,
        CITY,
        AREA,
    }

    private var provinceId: Long? = null
    private var cityId: Long? = null
    private var areaId: Long? = null
    private var activeTab = AddressLevel.PROVINCE
    private var pendingSelection: SelectionOption? = null
    private val addressAdapter = AddressOptionAdapter()
    private var letterPositions: Map<Char, Int> = emptyMap()

    override fun initView() {
        super.initView()
        setupAddressList()
        bindLevelSelection()
        bindConfirmAction()
        setOnShowListener { loadAddressLevel(AddressLevel.PROVINCE) }
    }

    private fun setupAddressList() = with(binding) {
        rvAddress.adapter = addressAdapter
        alphabetIndex.setOnLetterSelectedListener { letter ->
            val position = letterPositions[letter] ?: return@setOnLetterSelectedListener
            (rvAddress.layoutManager as? LinearLayoutManager)
                ?.scrollToPositionWithOffset(position, 0)
        }
        rvAddress.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val layoutManager = recyclerView.layoutManager as? LinearLayoutManager ?: return
                val position = layoutManager.findFirstVisibleItemPosition()
                val letter = addressAdapter.getItem(position)?.let { addressInitial(it.info) }
                if (letter != '#') alphabetIndex.setSelectedLetter(letter)
            }
        })
    }

    private fun loadAddressLevel(level: AddressLevel) = with(binding) {
        val parentId = when (level) {
            AddressLevel.PROVINCE -> null
            AddressLevel.CITY -> provinceId?.toString() ?: return@with
            AddressLevel.AREA -> cityId?.toString() ?: return@with
        }
        activeTab = level
        pendingSelection = null
        viewModel.getAddressList(parentId) { addressList ->
            if (activeTab != level) return@getAddressList

            val sortedItems = sortAddressOptions(addressList)
            letterPositions = buildMap {
                sortedItems.forEachIndexed { index, item ->
                    val letter = addressInitial(item.info)
                    if (letter != '#' && letter !in this) put(letter, index)
                }
            }
            addressAdapter.resetSelection()
            addressAdapter.submitItems(sortedItems)
            alphabetIndex.setAvailableLetters(letterPositions.keys)
            addressAdapter.setOnItemClickListener { item, position ->
                addressAdapter.select(position)
                pendingSelection = item
                updateLevelHeader(level, item)
            }
            sortedItems.firstOrNull()?.let { defaultItem ->
                addressAdapter.select(0)
                pendingSelection = defaultItem
                updateLevelHeader(level, defaultItem)
                rvAddress.scrollToPosition(0)
                alphabetIndex.setSelectedLetter(addressInitial(defaultItem.info))
            }
        }
    }

    private fun updateLevelHeader(level: AddressLevel, item: SelectionOption) = with(binding) {
        when (level) {
            AddressLevel.PROVINCE -> tvProvince.text = item.info
            AddressLevel.CITY -> tvCity.text = item.info
            AddressLevel.AREA -> tvArea.text = item.info
        }
    }

    private fun confirmAddressSelection(item: SelectionOption) = with(binding) {
        when (activeTab) {
            AddressLevel.PROVINCE -> {
                tvProvince.text = item.info
                provinceId = item.id.toLong()
                loadAddressLevel(AddressLevel.CITY)
            }

            AddressLevel.CITY -> {
                tvCity.text = item.info
                cityId = item.id.toLong()
                loadAddressLevel(AddressLevel.AREA)
            }

            AddressLevel.AREA -> {
                tvArea.text = item.info
                areaId = item.id.toLong()
                dismiss()
                onAddressSelected(
                    "${tvProvince.text}/${tvCity.text}/${tvArea.text}",
                    provinceId,
                    cityId,
                    areaId,
                )
            }
        }
    }

    private fun bindLevelSelection() = with(binding) {
        tvProvince.singleClick {
            tvProvince.text = ""
            provinceId = null
            tvCity.text = ""
            cityId = null
            tvArea.text = ""
            areaId = null
            loadAddressLevel(AddressLevel.PROVINCE)
        }

        tvCity.singleClick {
            if (provinceId == null) return@singleClick
            tvCity.text = ""
            cityId = null
            tvArea.text = ""
            areaId = null
            loadAddressLevel(AddressLevel.CITY)
        }

        tvArea.singleClick {
            if (cityId == null) return@singleClick
            tvArea.text = ""
            areaId = null
            loadAddressLevel(AddressLevel.AREA)
        }
    }

    private fun bindConfirmAction() = with(binding) {
        tvOk.singleClick {
            pendingSelection?.let(::confirmAddressSelection)
        }
    }
}
