package com.tradingview.lightweightcharts.example.app.network

import com.tradingview.lightweightcharts.example.app.model.DefaultChartsDataAPIResponseItem
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query


interface ApiService {

    companion object {
        private const val GET_CHARTS_PAIR = "api/chart/{pair}"
    }

    /**  Get Withdraw Details  **/
    @GET(GET_CHARTS_PAIR)
    fun onGetChartsPairData(
        @Path("pair") pair: String,
        @Query("interval") interval: String,
        @Query("timeEnd") timeEnd: Number,
        @Query("limit") limit: Number
    ): Observable<ArrayList<DefaultChartsDataAPIResponseItem>>

}