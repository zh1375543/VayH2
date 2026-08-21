package com.velora.portal.feature.onboarding.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.velora.portal.core.ui.base.BaseViewModel
import com.velora.portal.core.session.SessionStore
import com.velora.portal.feature.onboarding.data.AuthStatusRepository
import com.velora.portal.feature.onboarding.model.VerificationProgressResponse
import com.velora.portal.core.common.util.PageLoadState

/** Provides authentication progress and the server-configured authentication steps. */
class AuthStatusViewModel(
    private val authStatusRepository: AuthStatusRepository = AuthStatusRepository(),
) : BaseViewModel() {

    val userAuthStatusResult = MutableLiveData<VerificationProgressResponse?>()
    val loadFailedResult = MutableLiveData<Unit>()
    private val _userAuthStatusState = MutableLiveData<PageLoadState<VerificationProgressResponse>>(
        PageLoadState.Loading,
    )
    val userAuthStatusState: LiveData<PageLoadState<VerificationProgressResponse>> = _userAuthStatusState

    fun getUserAuthStatus() {
        _userAuthStatusState.value = PageLoadState.Loading
        createNetworkRequest {
            authStatusRepository.loadUserAuthStatus()
        }.onSuccess { status ->
            userAuthStatusResult.value = status
            _userAuthStatusState.value = status?.let { PageLoadState.Content(it) }
                ?: PageLoadState.Empty
        }.onFailed {
            _userAuthStatusState.value = PageLoadState.Error
            loadFailedResult.value = Unit
            false
        }
    }

    fun fetchAuthConfigList(action: (List<String>) -> Unit) {
        if (!SessionStore.isLoggedIn) return
        createNetworkRequest {
            authStatusRepository.loadAuthConfigList()
        }.onSuccess { list ->
            action(list.orEmpty())
        }.execute()
    }
}
