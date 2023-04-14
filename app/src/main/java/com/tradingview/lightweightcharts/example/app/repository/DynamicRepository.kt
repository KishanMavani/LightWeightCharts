package com.tradingview.lightweightcharts.example.app.repository

import android.util.Log
import com.tradingview.lightweightcharts.api.series.common.SeriesData
import com.tradingview.lightweightcharts.api.series.models.CandleChartData
import com.tradingview.lightweightcharts.api.series.models.OhlcData
import com.tradingview.lightweightcharts.api.series.models.OhlcDataDynamic
import com.tradingview.lightweightcharts.api.series.models.Time
import com.tradingview.lightweightcharts.example.app.model.Data
import com.tradingview.lightweightcharts.example.app.view.charts.RealTimeEmulationFragment.Companion.tempCoinList
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.*
import kotlin.math.roundToInt

class DynamicRepository {

    fun getListSeriesData(data: Data, onEmulationComplete: () -> Unit): Flow<SeriesData> {
        val lastData = data.list.last() as OhlcData
        var lastClose = lastData.close
        var lastHigh = lastData.high
        var lastLow = lastData.low
        var lastOpen = lastData.open
        var lastIndex = data.list.size - 2

        var targetIndex = lastIndex + 105 + (Math.random() + 30).roundToInt()
        var targetPrice = getRandomPrice()

        var currentIndex = lastIndex + 1
        var ticksInCurrentBar = 0;

        return flow {
            val date = Date()
//            (contextttt as? LifecycleOwner)?.let { it1 ->
//                CoinListObject.coinList.observe(it1) { value ->
//                    if (value != null) {
//                        lastOpen = value.open.toFloat()
//                        lastHigh = value.high.toFloat()
//                        lastLow = value.low.toFloat()
//                        lastClose = value.close.toFloat()
//                        Log.e("vsbhi8bsdfibs", "--=--$lastOpen")
//                        Log.e("vsbhi8bsdfibs", "--=--$lastHigh")
//                        Log.e("vsbhi8bsdfibs", "--=--$lastLow")
//                        Log.e("vsbhi8bsdfibs", "--=--$lastClose")
//
//                    }
//                }
//            }

            /*for (i in 0 until cointListt.size){
                 var currentCandlestickData: OhlcDataDynamic

                 delay(1500)
                 val deltaY = targetPrice - lastClose
                 val deltaX = targetIndex - lastIndex
                 val angle = deltaY / deltaX
                 val basePrice = lastClose + (currentIndex - lastIndex) * angle
                 val noise = (0.1f - Math.random().toFloat() * 0.2f) + 1.0f
                 val noisedPrice: Float = basePrice * noise
             }*/

            while (true) {
                var currentCandlestickData: OhlcDataDynamic

                delay(1500)

                if (!tempCoinList.isEmpty()) {
                    Log.e("Tewstttt", " = " + tempCoinList.size)
                    val deltaY = targetPrice - lastClose
                    val deltaX = targetIndex - lastIndex
                    val angle = deltaY / deltaX
                    val basePrice = lastClose + (currentIndex - lastIndex) * angle
                    val noise = (0.1f - Math.random().toFloat() * 0.2f) + 1.0f
                    val noisedPrice: Float = basePrice * noise


                    if (ticksInCurrentBar == 0) {
                        currentCandlestickData = CandleChartData(
                            time = Time.Utc(tempCoinList.get(tempCoinList.size - 1).time),
                            open = tempCoinList.get(tempCoinList.size - 1).open.toFloat(),
                            high = tempCoinList.get(tempCoinList.size - 1).high.toFloat(),
                            low = tempCoinList.get(tempCoinList.size - 1).low.toFloat(),
                            close = tempCoinList.get(tempCoinList.size - 1).close.toFloat(),
                        )
                    } else {
                        currentCandlestickData = CandleChartData(
//                            time = Time.Utc(tempCoinList.get(tempCoinList.size - 1).time),
//                            open = lastOpen,
//                            high = lastHigh.coerceAtLeast(noisedPrice),
//                            low = lastLow.coerceAtMost(noisedPrice),
//                            close = noisedPrice
                            time = Time.Utc(tempCoinList.get(tempCoinList.size - 1).time),
                            open = tempCoinList.get(tempCoinList.size - 1).open.toFloat(),
                            high = tempCoinList.get(tempCoinList.size - 1).high.toFloat(),
                            low = tempCoinList.get(tempCoinList.size - 1).low.toFloat(),
                            close = tempCoinList.get(tempCoinList.size - 1).close.toFloat(),
                        )
                    }

                    emit(currentCandlestickData)

//                    lastOpen = currentCandlestickData.open
//                    lastHigh = currentCandlestickData.high
//                    lastLow = currentCandlestickData.low
//                    lastClose = currentCandlestickData.close

//                    if (++ticksInCurrentBar == 5) {


                        date.time = date.time + 86000L * 1000L
                        currentIndex++;
                        ticksInCurrentBar = 0
                        if (currentIndex == 5000) {
                            onEmulationComplete.invoke()
                            return@flow
                        }

                        if (currentIndex == targetIndex) {
                            // change trend
                            lastClose = noisedPrice
                            lastIndex = currentIndex
                            targetIndex = (lastIndex + 5 + (Math.random() + 30).roundToInt())
                            targetPrice = getRandomPrice();
                        }
//                    }
                }
            }
        }
    }

    private fun getRandomPrice(): Int {
        return 10 + (Math.random() * 1000).roundToInt() / 100
    }
}