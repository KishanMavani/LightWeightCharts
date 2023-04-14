package com.tradingview.lightweightcharts.api.series.models

class CandleChartData(
    override val time: Time,
    override val open: Float,
    override val high: Float,
    override val low: Float,
    override val close: Float,
) : OhlcDataDynamic
