package com.tradingview.lightweightcharts.example.app.view.charts

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.tradingview.lightweightcharts.api.options.models.CandlestickSeriesOptions
import com.tradingview.lightweightcharts.api.options.models.crosshairOptions
import com.tradingview.lightweightcharts.api.series.enums.CrosshairMode
import com.tradingview.lightweightcharts.example.app.R
import com.tradingview.lightweightcharts.example.app.model.DefaultChartsDataAPIResponseItem
import com.tradingview.lightweightcharts.example.app.network.CoinListObject
import com.tradingview.lightweightcharts.example.app.viewmodel.RealTimeEmulationViewModel
import com.tradingview.lightweightcharts.view.ChartsView
import io.socket.client.IO
import io.socket.client.Manager
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import okhttp3.OkHttpClient
import org.json.JSONObject
import java.net.URISyntaxException
import java.security.KeyManagementException
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.*

class RealTimeEmulationFragment : Fragment() {
    private val chartsView get() = requireView().findViewById<ChartsView>(R.id.charts_view)
    private val chartApi get() = chartsView.api

    private var realtimeDataJob: Job? = null
    private var mSocket: Socket? = null
    private val roomJson = JSONObject("{\n \"room\": \"kgraph:btc_usdt:1m\"\n}")

    companion object {
        val cointListt = ArrayList<DefaultChartsDataAPIResponseItem>()
        val tempCoinList = ArrayList<DefaultChartsDataAPIResponseItem>()

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.layout_chart_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val viewModelProvider = ViewModelProvider(this)
        val viewModel = viewModelProvider[RealTimeEmulationViewModel::class.java]

        cointListt.clear()
        tempCoinList.clear()

        val time = System.currentTimeMillis() / 1000
        viewModel.timeEnd = time

        connectWebSocket()
        Handler(Looper.getMainLooper()).postDelayed({
            emitEvent("subscribe", roomJson)
        }, 1000)

        viewModel.onChartsDefaultAPICalling()


        viewModel.mutExchangePairListResponse.observe(viewLifecycleOwner) {
            if (it != null) {
                for (i in 0 until it.size) {
                    cointListt.add(it[i])
                }

//                Thread {
//                    activity?.runOnUiThread {
//                        for (data in arrayList) {
//                            CoinListObject.coinList.value = DefaultChartsDataAPIResponseItem(
//                                data.close,
//                                data.high,
//                                data.low,
//                                data.open,
//                                time,
//                                data.volume
//                            )
//                        }
//                    }
//                }.run()
//                arrayList.addAll(it)


             /*   viewModel.seriesData.observe(viewLifecycleOwner) { data ->
                    chartApi.addCandlestickSeries(
                        options = CandlestickSeriesOptions(),
                        onSeriesCreated = { series ->
                            // By Default API Data Set //
                            series.setData(data.list)

                            // Live Value Change
                            realtimeDataJob = lifecycleScope.launchWhenResumed {
                                viewModel.seriesFlow.collect()
                            }
                        }
                    )
                }*/

                viewModel.seriesData.observe(viewLifecycleOwner) { data ->
                    chartApi.addCandlestickSeries(
                        options = CandlestickSeriesOptions(),
                        onSeriesCreated = { series ->
                            /**
                             * Get data from StaticRepository for start point chart.
                             */
                            series.setData(data.list)
                            realtimeDataJob = lifecycleScope.launchWhenResumed {
                                viewModel.seriesFlow.collect(series::update)
                            }
                        }
                    )
                }

                chartApi.applyOptions {
                    crosshair = crosshairOptions {
                        mode = CrosshairMode.NORMAL
                    }
                }
            }
        }


    }


    private fun connectWebSocket() {
        try {
            val myHostnameVerifier =
                HostnameVerifier { _, _ -> true }
            val mySSLContext = SSLContext.getInstance("TLS")
            val trustAllCerts = arrayOf<TrustManager>(@SuppressLint("CustomX509TrustManager")
            object : X509TrustManager {
                override fun getAcceptedIssuers(): Array<X509Certificate> {
                    return arrayOf()
                }

                @SuppressLint("TrustAllX509TrustManager")
                @Throws(CertificateException::class)
                override fun checkClientTrusted(
                    chain: Array<X509Certificate>,
                    authType: String,
                ) {
                }

                @SuppressLint("TrustAllX509TrustManager")
                @Throws(CertificateException::class)
                override fun checkServerTrusted(
                    chain: Array<X509Certificate>,
                    authType: String,
                ) {
                }
            })

            mySSLContext.init(null, trustAllCerts, SecureRandom())
            val okHttpClient = OkHttpClient.Builder()
                .hostnameVerifier(myHostnameVerifier)
                .sslSocketFactory(mySSLContext.socketFactory, object : X509TrustManager {
                    @SuppressLint("TrustAllX509TrustManager")
                    @Throws(CertificateException::class)
                    override fun checkClientTrusted(
                        chain: Array<X509Certificate>,
                        authType: String,
                    ) {
                    }

                    @SuppressLint("TrustAllX509TrustManager")
                    @Throws(CertificateException::class)
                    override fun checkServerTrusted(
                        chain: Array<X509Certificate>,
                        authType: String,
                    ) {
                    }

                    override fun getAcceptedIssuers(): Array<X509Certificate?> {
                        return arrayOfNulls(0)
                    }
                })
                .build()


            HttpsURLConnection.setDefaultHostnameVerifier(myHostnameVerifier);
            val options = IO.Options()

            options.callFactory = okHttpClient
            options.webSocketFactory = okHttpClient

            mSocket =
                IO.socket("wss://view-socket.lynxbeta.com/?EIO=4&transport=websocket", options)

            mSocket!!.on(Socket.EVENT_CONNECT, onConnect)
            mSocket!!.on(Manager.EVENT_TRANSPORT, onTransportError)
            mSocket!!.on(Socket.EVENT_CONNECT_ERROR, onConnectError)
            mSocket!!.connect()

            Log.e("khvgjucvhcvyg", "----${mSocket!!.connected()}")

        } catch (e: URISyntaxException) {
            throw RuntimeException(e)
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        } catch (e: KeyManagementException) {
            e.printStackTrace()
        }
    }

    private val onConnect = Emitter.Listener {
        activity?.runOnUiThread {
            if (mSocket!!.connected()) {
                mSocket!!.on("message", onMessageEvent)
                mSocket!!.on("subscribe") { }
                mSocket!!.on("kgraph:btc_usdt:1m", onReceviedMessage)
            }
        }
    }

    private val onReceviedMessage =
        Emitter.Listener { args ->
            Log.e("uybyua8guyese", "----${args.contentToString()}")
            activity?.runOnUiThread {
                try {
                    val data = args.contentToString()
                    val newData =
                        data.replace("[", "").replace("]", "")
                    val allData: JsonObject? = JsonParser().parse(newData).asJsonObject
                    val value = allData?.getAsJsonObject("tempKindle")

                    val close = value?.getAsJsonPrimitive("close")?.asString
                    val high = value?.getAsJsonPrimitive("high")?.asString
                    val low = value?.getAsJsonPrimitive("low")?.asString
                    val open = value?.getAsJsonPrimitive("open")?.asString
                    val time = value?.getAsJsonPrimitive("time")?.asString
                    val volume = value?.getAsJsonPrimitive("volume")?.asString

                    Thread {
                         activity?.runOnUiThread {
                             tempCoinList.add(DefaultChartsDataAPIResponseItem(
                                 close!!.toDouble(),
                                 high!!.toDouble(),
                                 low!!.toDouble(),
                                 open!!.toDouble(),
                                 time!!.toLong(),
                                 volume!!.toDouble()
                             ))
                             CoinListObject.coinList.value =
                                 DefaultChartsDataAPIResponseItem(
                                     close!!.toDouble(),
                                     high!!.toDouble(),
                                     low!!.toDouble(),
                                     open!!.toDouble(),
                                     time!!.toLong(),
                                     volume!!.toDouble()
                                 )
                         }
                     }.run()
                } catch (e: Exception) {
                    Log.e("VISHAL---", "-------${e}")
                }
            }
        }

    private fun emitEvent(eventKey: String, json: JSONObject) {
        try {
            if (mSocket != null && mSocket!!.connected()) {
                mSocket!!.emit(eventKey, json)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private val onMessageEvent =
        Emitter.Listener { args ->
            activity?.runOnUiThread {
                try {
                    Log.e("VISHAL-->onMessageEvent", "-------${args.size}")
                } catch (e: Exception) {
                    Log.e("VISHAL---> Error ", "-------${e}")
                }
            }
        }

    private val onDisconnect = Emitter.Listener {
        activity?.runOnUiThread {
            Log.e("JAY---> onDisconnect", "onDisconnect:--> ")
        }
    }

    private val onConnectError = Emitter.Listener {
        activity?.runOnUiThread {
            try {
                Log.e("TAG", "onConnectError:--> ${it.size}   ${it[0]}")
                Thread {
                    activity?.runOnUiThread {

                    }
                }.run()
            } catch (e: URISyntaxException) {
                Log.e("JAY---> onConnectError", "Error connecting$e")
            }
        }
    }

    private val onTransportError = Emitter.Listener {
        activity?.runOnUiThread {
            try {
                Thread {
                    activity?.runOnUiThread {

                    }
                }.run()
            } catch (e: URISyntaxException) {
                Log.e("JAY---> onTransport", "Error connecting$e")
            }
        }
    }

    override fun onDestroy() {
        realtimeDataJob?.cancel()
        super.onDestroy()
    }
}

