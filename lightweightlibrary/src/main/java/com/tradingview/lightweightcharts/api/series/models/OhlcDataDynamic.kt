package com.tradingview.lightweightcharts.api.series.models

import com.tradingview.lightweightcharts.api.series.common.SeriesData

interface OhlcDataDynamic : SeriesData {
    /**
     * The bar time.
     */
    override val time: Time

    /**
     * The open price.
     */
    val open: Float

    /**
     * The high price.
     */
    val high: Float

    /**
     * The low price.
     */
    val low: Float

    /**
     * The close price.
     */
    val close: Float
}