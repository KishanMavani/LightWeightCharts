package com.tradingview.lightweightcharts.example.app.model

data class DefaultChartsDataAPIResponseItem(
    val close: Double,
    val high: Double,
    val low: Double,
    val open: Double,
    val time: Long,
    val volume: Double
)