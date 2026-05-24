package com.vaastuverse.app.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vaastuverse.app.data.CommunicationPreferences
import com.vaastuverse.app.data.dto.AccountMeResponse
import com.vaastuverse.app.data.dto.ApplicationResponse
import com.vaastuverse.app.data.dto.CustomerProfileResponse
import com.vaastuverse.app.data.repository.CommunicationPreferencesRepository
import com.vaastuverse.app.data.dto.DiscoverablePartnerResponse
import com.vaastuverse.app.data.dto.PartnerProfileResponse
import com.vaastuverse.app.data.repository.VaastuRepository
import com.vaastuverse.app.ui.partner.PartnerPersona
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AppUiState(
  val phase: AppPhase = AppPhase.Loading,
  val isLoading: Boolean = false,
  val error: String? = null,
  val info: String? = null,
  val session: StoredSession? = null,
  val customerProfile: CustomerProfileResponse? = null,
  val account: AccountMeResponse? = null,
  val communicationPreferences: CommunicationPreferences = CommunicationPreferences(),
  val partnerProfiles: List<PartnerProfileResponse> = emptyList(),
  val applications: List<ApplicationResponse> = emptyList(),
  val discoverablePartners: List<DiscoverablePartnerResponse> = emptyList(),
  val partnerStats: PartnerDashboardStats = PartnerDashboardStats(),
  val experienceMode: ExperienceMode = ExperienceMode.Customer,
  val partnerPersona: PartnerPersona = PartnerPersona.Guruji,
  val otpPhone: String = "",
  val otpSent: Boolean = false,
  val profileExists: Boolean = false,
)

class AppCoordinatorViewModel(application: Application) : AndroidViewModel(application) {
  private val tokenStore = TokenStore(application)
  private val repository = VaastuRepository(tokenStore)
  private val commPrefsRepository = CommunicationPreferencesRepository(application)

  private val _state = MutableStateFlow(AppUiState())
  val state: StateFlow<AppUiState> = _state.asStateFlow()

  init {
    bootstrap()
  }

  fun clearMessage() {
    _state.update { it.copy(error = null, info = null) }
  }

  fun goToWelcome() {
    _state.update { it.copy(phase = AppPhase.Welcome, error = null) }
  }

  fun goToOtp() {
    _state.update { it.copy(phase = AppPhase.OtpLogin, error = null, otpSent = false, otpPhone = "") }
  }

  fun resetOtpFlow() {
    _state.update { it.copy(otpSent = false, otpPhone = "", error = null, info = null) }
  }

  fun goToRegister() {
    _state.update { it.copy(phase = AppPhase.Register, error = null) }
  }

  fun goToEmailLogin() {
    _state.update { it.copy(phase = AppPhase.EmailLogin, error = null) }
  }

  fun goToProfileChoice() = runTask(showLoading = false) {
    _state.update { it.copy(phase = AppPhase.ProfileChoice, error = null) }
  }

  fun openPartnerOnboardingGate() = runTask(showLoading = false) {
    refreshApplicationsInternal()
    _state.update { it.copy(phase = AppPhase.PartnerOnboardingGate) }
  }

  fun chooseCustomer() = runTask {
    val session = refreshSessionInternal()
    _state.update {
      it.copy(
        session = session,
        phase = AppPhase.Experience,
        experienceMode = ExperienceMode.Customer,
      )
    }
  }

  fun choosePartner() = runTask {
    val session = refreshSessionInternal()
    val apps = repository.myApplications(session)
    when {
      PartnerAccess.isOnboarded(session) -> enterPartnerExperienceInternal(session, apps)
      PartnerAccess.hasOpenApplication(apps) -> {
        _state.update {
          it.copy(
            session = session,
            applications = apps,
            phase = AppPhase.PartnerOnboardingGate,
          )
        }
      }
      else -> {
        _state.update {
          it.copy(
            session = session,
            applications = apps,
            phase = AppPhase.PartnerApply,
          )
        }
      }
    }
  }

  fun loadDiscoverPartners(type: String) = runTask(showLoading = false) {
    val session = _state.value.session ?: return@runTask
    val partners = repository.discoverPartners(session, type)
    _state.update { it.copy(discoverablePartners = partners) }
  }

  fun sendOtp(phone: String) = runTask {
    val normalized = PhoneNormalizer.normalizeIndianMobile(phone)
    repository.sendOtp(normalized)
    _state.update {
      it.copy(
        otpPhone = normalized,
        otpSent = true,
        info = "OTP sent to $normalized. Check auth logs: http://localhost:8080/dev/logs/auth",
      )
    }
  }

  fun verifyOtp(phone: String, code: String) = runTask {
    val normalized = PhoneNormalizer.normalizeIndianMobile(phone)
    val session = repository.verifyOtp(normalized, code.trim())
    afterAuth(session)
  }

  fun register(phone: String, email: String?, password: String?, displayName: String?) = runTask {
    val normalized = PhoneNormalizer.normalizeIndianMobile(phone)
    val session = repository.register(
      normalized,
      email?.trim()?.ifBlank { null },
      password?.ifBlank { null },
      displayName?.trim()?.ifBlank { null },
    )
    afterAuth(session)
  }

  fun loginWithEmail(email: String, password: String) = runTask {
    val session = repository.login(email.trim(), password)
    afterAuth(session)
  }

  fun saveCustomerProfile(displayName: String, city: String?) = runTask {
    val session = _state.value.session ?: throw IllegalStateException("Not logged in")
    val profile = repository.saveCustomerProfile(
      session,
      displayName.trim(),
      city?.trim()?.ifBlank { null },
      _state.value.profileExists,
    )
    _state.update { it.copy(customerProfile = profile, profileExists = true, phase = AppPhase.ProfileChoice) }
    refreshApplicationsInternal()
  }

  fun updateCommunicationPreferences(preferences: CommunicationPreferences) = viewModelScope.launch {
    val userId = _state.value.session?.userId ?: return@launch
    commPrefsRepository.save(userId, preferences)
    _state.update { it.copy(communicationPreferences = preferences) }
  }

  fun refreshAccount() = runTask(showLoading = false) {
    val session = _state.value.session ?: return@runTask
    val account = repository.getAccountMe(session)
    _state.update { it.copy(account = account) }
  }

  fun saveCustomerProfileInExperience(displayName: String, city: String?) = runTask {
    val session = _state.value.session ?: throw IllegalStateException("Not logged in")
    val profile = repository.saveCustomerProfile(
      session,
      displayName.trim(),
      city?.trim()?.ifBlank { null },
      _state.value.profileExists,
    )
    _state.update {
      it.copy(
        customerProfile = profile,
        profileExists = true,
        info = "Profile updated",
      )
    }
  }

  fun applyForPartner(role: String) = runTask {
    val session = _state.value.session ?: throw IllegalStateException("Not logged in")
    repository.applyPartner(session, role)
    refreshApplicationsInternal()
    _state.update {
      it.copy(
        phase = AppPhase.PartnerOnboardingGate,
        info = "Application submitted. A Guruji will review it.",
      )
    }
  }

  fun refreshApplications() = runTask(showLoading = false) {
    refreshApplicationsInternal()
    val session = _state.value.session
    if (session != null && PartnerAccess.isOnboarded(session) && _state.value.phase == AppPhase.PartnerOnboardingGate) {
      choosePartner()
    }
  }

  fun logout() = viewModelScope.launch {
    repository.logout()
    _state.value = AppUiState(phase = AppPhase.Welcome)
  }

  private fun bootstrap() = viewModelScope.launch {
    _state.update { it.copy(isLoading = true) }
    try {
      val session = tokenStore.currentSession()
      if (session == null) {
        _state.update { it.copy(phase = AppPhase.Welcome, isLoading = false) }
        return@launch
      }
      afterAuth(session, skipLoadingBanner = true)
    } catch (e: Exception) {
      tokenStore.clear()
      _state.update { it.copy(phase = AppPhase.Welcome, isLoading = false, error = e.message) }
    }
  }

  private suspend fun afterAuth(session: StoredSession, skipLoadingBanner: Boolean = false) {
    if (!skipLoadingBanner) _state.update { it.copy(isLoading = true) }
    try {
      val activeSession = try {
        repository.refreshSession()
      } catch (_: Exception) {
        session
      }
      val profile = repository.getCustomerProfile(activeSession)
      val account = try {
        repository.getAccountMe(activeSession)
      } catch (_: Exception) {
        null
      }
      val commPrefs = commPrefsRepository.preferencesFlow(activeSession.userId).first()
      val partners = if (PartnerAccess.isOnboarded(activeSession)) {
        repository.listPartnerProfiles(activeSession)
      } else {
        emptyList()
      }
      val apps = repository.myApplications(activeSession)
      val profileExists = profile != null
      _state.update {
        it.copy(
          session = activeSession,
          customerProfile = profile,
          account = account,
          communicationPreferences = commPrefs,
          partnerProfiles = partners,
          applications = apps,
          profileExists = profileExists,
          isLoading = false,
          phase = if (profileExists) AppPhase.ProfileChoice else AppPhase.CustomerProfile,
        )
      }
    } catch (e: Exception) {
      _state.update { it.copy(isLoading = false, error = e.message, phase = AppPhase.Welcome) }
    }
  }

  private suspend fun refreshSessionInternal(): StoredSession {
    val session = repository.refreshSession()
    _state.update { it.copy(session = session) }
    return session
  }

  private suspend fun refreshApplicationsInternal() {
    val session = _state.value.session ?: return
    val apps = repository.myApplications(session)
    _state.update { it.copy(applications = apps) }
  }

  private suspend fun enterPartnerExperienceInternal(
    session: StoredSession,
    apps: List<ApplicationResponse>,
  ) {
    val role = PartnerAccess.primaryPartnerRole(session)
      ?: throw IllegalStateException("No partner role found. Refresh session or sign in again.")
    val partnerProfiles = repository.listPartnerProfiles(session)
    val stats = repository.loadPartnerDashboardStats(session, role)
    val persona = PartnerAccess.partnerPersona(session)
    _state.update {
      it.copy(
        session = session,
        applications = apps,
        partnerProfiles = partnerProfiles,
        partnerStats = stats,
        partnerPersona = persona,
        phase = AppPhase.Experience,
        experienceMode = ExperienceMode.Partner,
      )
    }
  }

  private fun runTask(showLoading: Boolean = true, block: suspend () -> Unit) {
    viewModelScope.launch {
      if (showLoading) _state.update { it.copy(isLoading = true, error = null) }
      try {
        block()
      } catch (e: Exception) {
        _state.update { it.copy(error = e.message ?: e.toString()) }
      } finally {
        if (showLoading) _state.update { it.copy(isLoading = false) }
      }
    }
  }
}
