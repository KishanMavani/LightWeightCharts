package com.tradingview.lightweightcharts.example.app.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tradingview.lightweightcharts.api.series.common.SeriesData
import com.tradingview.lightweightcharts.api.series.enums.SeriesType
import com.tradingview.lightweightcharts.example.app.model.Data
import com.tradingview.lightweightcharts.example.app.model.DefaultChartsDataAPIResponseItem
import com.tradingview.lightweightcharts.example.app.network.ApiService
import com.tradingview.lightweightcharts.example.app.network.WebserviceCaller
import com.tradingview.lightweightcharts.example.app.repository.DynamicRepository
import com.tradingview.lightweightcharts.example.app.repository.StaticRepository
import com.tradingview.lightweightcharts.example.app.view.charts.RealTimeEmulationFragment.Companion.cointListt
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class RealTimeEmulationViewModel : BaseViewModel() {

    private lateinit var subscription: Disposable
    private var apiService: ApiService = WebserviceCaller.getClient()

    private val staticRepository = StaticRepository()
    private val dynamicRepository = DynamicRepository()

    val seriesFlow: Flow<SeriesData>
        get() = dynamicRepository.getListSeriesData(data.value!!) {
            loadData()
        }

    val seriesData: LiveData<Data>
        get() = data

    private val data: MutableLiveData<Data> by lazy {
        MutableLiveData<Data>().also {
            loadData()
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            val barData  = staticRepository.getRealTimeEmulationSeriesData()
            data.postValue(Data(barData, SeriesType.CANDLESTICK))
        }
    }

    var mutExchangePairListResponse: MutableLiveData<ArrayList<DefaultChartsDataAPIResponseItem>> =
        MutableLiveData()
    private var interval: String? = "5m"
    var timeEnd: Number = 0
    private var limit: Number = 1000
    private var pair: String? = "btc_usdt"

    /**         Fetching Exchange Pair List API Call      **/
    fun onChartsDefaultAPICalling() {
        subscription = apiService.onGetChartsPairData(
            pair.toString(),
            interval.toString(),
            timeEnd,
            limit
        )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .unsubscribeOn(Schedulers.io())
            .subscribe(
                { result -> onExchangePairListSuccess(result) },
                this::onApiError
            )
    }

    private fun onExchangePairListSuccess(result: ArrayList<DefaultChartsDataAPIResponseItem>) {
        mutExchangePairListResponse.value = result
    }
}