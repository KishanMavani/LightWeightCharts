package com.tradingview.lightweightcharts.example.app.network

import androidx.lifecycle.MutableLiveData
import com.tradingview.lightweightcharts.example.app.model.DefaultChartsDataAPIResponseItem

object CoinListObject {
    var coinList: MutableLiveData<DefaultChartsDataAPIResponseItem> = MutableLiveData()
}