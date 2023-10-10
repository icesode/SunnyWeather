package com.sunnyweather.android.ui.weather

import android.content.Context
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.sunnyweather.android.R
import com.sunnyweather.android.databinding.ActivityWeatherBinding
import com.sunnyweather.android.databinding.ForecastBinding
import com.sunnyweather.android.databinding.LifeIndexBinding
import com.sunnyweather.android.databinding.NowBinding
import com.sunnyweather.android.logic.model.Weather
import com.sunnyweather.android.logic.model.getSky
import java.text.SimpleDateFormat
import java.util.*
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.sunnyweather.android.SunnyWeatherApplication.Companion.context

class WeatherActivity : AppCompatActivity() {
    public lateinit var binding: ActivityWeatherBinding
//    private lateinit var nowbinding: NowBinding
//    private lateinit var forcecastbinging:ForecastBinding
//    private lateinit var lifeIndexBinding: LifeIndexBinding

    private lateinit var nowLayout: View
    private lateinit var placeName: TextView
    private lateinit var currentTemp: TextView
    private lateinit var currentSky: TextView
    private lateinit var currentAQI: TextView
    private lateinit var forecastLayout: ViewGroup
    private lateinit var coldRiskText: TextView
    private lateinit var dressingText: TextView
    private lateinit var ultravioletText: TextView
    private lateinit var carWashingText: TextView
    private lateinit var nabBtn:Button

    val viewModel by lazy { ViewModelProvider(this).get(WeatherViewModel::class.java) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityWeatherBinding.inflate(layoutInflater)
//        nowbinding= NowBinding.inflate(layoutInflater)
//        forcecastbinging= ForecastBinding.inflate(layoutInflater)
//        lifeIndexBinding= LifeIndexBinding.inflate(layoutInflater)
        setContentView(binding.root)

        nowLayout = findViewById(R.id.nowLayout)
        placeName = findViewById(R.id.placeName)
        currentTemp = findViewById(R.id.currentTemp)
        currentSky = findViewById(R.id.currentSky)
        currentAQI = findViewById(R.id.currentAQI)
        forecastLayout = findViewById(R.id.forecastLayout)
        coldRiskText = findViewById(R.id.coldRiskText)
        dressingText = findViewById(R.id.dressingText)
        ultravioletText = findViewById(R.id.ultravioletText)
        carWashingText = findViewById(R.id.carWashingText)
        nabBtn=findViewById(R.id.navBtn)
//        val nowlayout=binding.now.root

        if (viewModel.locationLng.isEmpty()){
            viewModel.locationLng=intent.getStringExtra("location_lng") ?:""
//            Log.e("lng", viewModel.locationLng)
        }
        if (viewModel.locationLat.isEmpty()) {
            viewModel.locationLat = intent.getStringExtra("location_lat") ?: ""
//            Log.e("lat", viewModel.locationLat)
        }
        if (viewModel.placeName.isEmpty()) {
            viewModel.placeName = intent.getStringExtra("place_name") ?: ""
//            Log.e("placename", viewModel.placeName)
        }
        viewModel.weatherLiveData.observe(this, Observer { result->
//            Log.e("result", result.toString())
            val weather=result.getOrNull()
//            Log.e("result", weather.toString())
            if (weather!=null){
                showWeatherInfo(weather)
            }else{
                Toast.makeText(this,"无法成功获取天气信息",Toast.LENGTH_SHORT).show()
                result.exceptionOrNull()?.printStackTrace()
            }
            binding.swipeRefresh.isRefreshing=false
        })
//        val color = ContextCompat.getColor(context, R.color.colorPrimary)
        binding.swipeRefresh.setColorSchemeColors(ContextCompat.getColor(context, R.color.colorPrimary))
        refreshWeather()
        binding.swipeRefresh.setOnRefreshListener {
            refreshWeather()
        }
        viewModel.refreshWeather(viewModel.locationLng,viewModel.locationLat)

        nabBtn.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }
        binding.drawerLayout.addDrawerListener(object:DrawerLayout.DrawerListener{
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
            }

            override fun onDrawerOpened(drawerView: View) {
            }

            override fun onDrawerClosed(drawerView: View) {
                val manager=getSystemService(Context.INPUT_METHOD_SERVICE)
                as InputMethodManager
                manager.hideSoftInputFromWindow(drawerView.windowToken,
                InputMethodManager.HIDE_NOT_ALWAYS)
            }

            override fun onDrawerStateChanged(newState: Int) {
            }
        })
    }

    fun refreshWeather(){
        viewModel.refreshWeather(viewModel.locationLng,viewModel.locationLat)
        binding.swipeRefresh.isRefreshing=true
    }

    private fun showWeatherInfo(weather: Weather) {
        placeName.text=viewModel.placeName
        val realtime=weather.realtime
        val daily=weather.daily
        val currentTempText="${realtime.temperature.toInt()} ℃"
        currentTemp.text=currentTempText
        currentSky.text= getSky(realtime.skycon).info
        val currentPM25Text="空气指数${realtime.airQuality.aqi.chn.toInt()}"
        currentAQI.text=currentPM25Text
        nowLayout.setBackgroundResource(getSky(realtime.skycon).bg)

        var forcecastLayout = forecastLayout
        forcecastLayout.removeAllViews()
        val days=daily.skycon.size
        for(i in 0 until days){
            val skycon=daily.skycon[i]
            val temperature=daily.temperature[i]
            val view=LayoutInflater.from(this).inflate(R.layout.forecast_item,forcecastLayout,false)
            val dateInfo=view.findViewById(R.id.dateInfo) as TextView
            val skyIcon=view.findViewById(R.id.skyIcon) as ImageView
            val skyInfo=view.findViewById(R.id.skyInfo) as TextView
            val temperatureInfo=view.findViewById(R.id.temperatureInfo) as TextView
            val simpleDateFormat=SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            dateInfo.text=simpleDateFormat.format(skycon.date)
            val sky= getSky(skycon.value)
            skyIcon.setImageResource(sky.icon)
            skyInfo.text=sky.info
            val tempText="${temperature.min.toInt()}~${temperature.max.toInt()} ℃"
            temperatureInfo.text=tempText
            forcecastLayout.addView(view)
        }
        val lifeIndex=daily.lifeIndex
        coldRiskText.text = lifeIndex.coldRisk[0].desc
//        Log.e("coldRisk", lifeIndex.coldRisk[0].desc)
        dressingText.text = lifeIndex.dressing[0].desc
        ultravioletText.text = lifeIndex.ultraviolet[0].desc
        carWashingText.text = lifeIndex.carWashing[0].desc
        binding.weatherLayout.visibility = View.VISIBLE
    }


}