package com.narxoz.social.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.*
import com.narxoz.social.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AuthRepository(application)

    // Шаблон: ^[SF]\d{8}$
    private val loginRegex = Regex("^[sSfF]\\d{8}$")

    // LiveData для хранения текста ошибки
    private val _loginError = MutableLiveData<String?>()
    val loginError: LiveData<String?> = _loginError

    private val _email = MutableStateFlow("")
    val email = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password = _password.asStateFlow()

    private val _staySignedIn = MutableStateFlow(false)
    val staySignedIn = _staySignedIn.asStateFlow()

    private val _loginResult = MutableLiveData<String?>()
    val loginResult: LiveData<String?> = _loginResult

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _policyAccepted = MutableStateFlow(AuthRepository.isPolicyAccepted())
    val policyAccepted = _policyAccepted.asStateFlow()

    init {
        loadStoredCredentials(application)
    }

    private fun loadStoredCredentials(context: Context) {
        val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        val storedEmail = prefs.getString("email", "") ?: ""
        val storedPassword = prefs.getString("password", "") ?: ""
        val storedStaySignedIn = prefs.getBoolean("stay_signed_in", false)
        _policyAccepted.value = AuthRepository.isPolicyAccepted()

        if (storedStaySignedIn && storedEmail.isNotBlank() && storedPassword.isNotBlank()) {
            _email.value = storedEmail
            _password.value = storedPassword
            _staySignedIn.value = storedStaySignedIn
            login(storedEmail, storedPassword, storedStaySignedIn)  // Передаём staySignedIn
        }
    }

    fun updateEmail(value: String) {
        _email.value = value
    }

    fun updatePassword(value: String) {
        _password.value = value
    }

    fun updateStaySignedIn(value: Boolean) {
        _staySignedIn.value = value
    }

    fun login(email: String, password: String, staySignedIn: Boolean = _staySignedIn.value) {
        viewModelScope.launch {
            // Сначала проверяем формат логина
            if (!loginRegex.matches(email)) {
                // Устанавливаем сообщение об ошибке
                _loginError.value = "Неверный формат логина: требуется S######## или F########"
                // Сбрасываем результат входа (чтобы UI понял, что вход неудачен)
                _loginResult.value = null
                return@launch
            } else {
                // Если формат корректный, то убираем возможное предыдущее сообщение об ошибке
                _loginError.value = null
            }

            _isLoading.value = true
            try {
                val role = repository.login(email, password, staySignedIn)
                _isLoading.value = false
                if (role != null) {
                    _loginResult.value = role
                    saveCredentials(email, password, staySignedIn)
                    _policyAccepted.value = AuthRepository.isPolicyAccepted()
                } else {
                    // Если сервер ответил неудачей / нет токена или роли
                    _loginResult.value = null
                }
            } catch (e: Exception) {
                _isLoading.value = false
            }
        }
    }

    fun logout() {
        repository.logout()
        // Дополнительно можно сбросить локальные поля, если надо
        _loginResult.value = null
        _email.value = ""
        _password.value = ""
        _staySignedIn.value = false
    }

    fun acceptPolicy() {
        AuthRepository.setPolicyAccepted(true)
        _policyAccepted.value = true
    }

    private fun saveCredentials(email: String, password: String, staySignedIn: Boolean) {
        val prefs = getApplication<Application>().getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        with(prefs.edit()) {
            putString("email", if (staySignedIn) email else "")
            putString("password", if (staySignedIn) password else "")
            putBoolean("stay_signed_in", staySignedIn)
            apply()
        }
    }
}