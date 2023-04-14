package com.tradingview.lightweightcharts.example.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tradingview.lightweightcharts.example.app.model.ErrorHandle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

open class BaseViewModel : ViewModel() {

    private val _errorMessageChannel: Channel<String> = Channel(Channel.BUFFERED)
    val errorMessageFlow: Flow<String> get() = _errorMessageChannel.receiveAsFlow()

    private fun sendErrorMessage(message: String) {
        viewModelScope.launch(Dispatchers.Main) {
            _errorMessageChannel.send(message)
        }
    }

    fun onApiError(error: Throwable) {
        viewModelScope.launch(Dispatchers.Default) {
            var errorMessage = FALLBACK_ERROR_MESSAGE

            if (error is HttpException) {
                try {
                    val response = error.response()?.errorBody()?.string()
                    val craterErrorResponse =
                        CraterParsers.gson.fromJson(response, ErrorHandle::class.java)

                    if (craterErrorResponse.status == 422) {
                        if (craterErrorResponse.message?.email != null) {
                            errorMessage = craterErrorResponse.message?.email.toString()
                        }
                        if (craterErrorResponse.message?.dateOfBirth != null) {
                            errorMessage = craterErrorResponse.message?.dateOfBirth.toString()
                        }
                        if (craterErrorResponse.message?.phoneNumber != null) {
                            errorMessage = craterErrorResponse.message?.phoneNumber.toString()
                        }
                    } else {
                        craterErrorResponse.message?.email ?: FALLBACK_ERROR_MESSAGE
                    }

                } catch (e: Exception) {
                    FALLBACK_ERROR_MESSAGE
                }
            } else {
                errorMessage = error.localizedMessage!!
            }

            sendErrorMessage(errorMessage)
        }
    }

    companion object {
        const val FALLBACK_ERROR_MESSAGE = "Something went wrong, try later!"
    }
}