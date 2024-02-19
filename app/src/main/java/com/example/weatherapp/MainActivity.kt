package com.example.weatherapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.SearchView
import android.widget.Toast
import com.example.weatherapp.databinding.ActivityMainBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding:ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        editors()
        val editor = this.getSharedPreferences("Editor", MODE_PRIVATE)

        var City = editor.getString("City","Sylhet")
        fetchData(City!!)

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                if(query!=null){
                    fetchData(query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }


        })
    }


    /**
     * Methods that are define under this activity
     */

    private fun editors() {
        val editor = this.getSharedPreferences("Editor", MODE_PRIVATE).edit()
        editor.putString("City","Sylhet")
        editor.apply()
    }

    private fun fetchData(city: String) {
        val retro = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .build().create(ApiInterface::class.java)
        val respons = retro.getWeatherData(city,"84e41c8c9d627cf47e2836e3d28758a9","metric")

        respons.enqueue(object : Callback<WeatherApp>{
            override fun onResponse(call: Call<WeatherApp>, response: Response<WeatherApp>) {
                val resbody = response.body()
               if(response.isSuccessful && resbody != null){
                   val temparature = resbody.main.temp.toString()
                   val windspeed = resbody.wind.speed
                   val humidity = resbody.main.humidity
                   val sunrise = resbody.sys.sunrise
                   val sunset = resbody.sys.sunset
                   val sea = resbody.main.pressure
                   var maxtem = resbody.main.temp_max
                   var mintem = resbody.main.temp_min
                   val condition = resbody.weather.firstOrNull()?.main?: "unknown"
                   binding.humidity.text = humidity.toString()+"%"
                   binding.conditions.text = condition.toString()
                   binding.conditionText.text = condition.toString()
                   binding.minmax.text = "Max ${String.format("%.2f", maxtem + 2.8)} °C\nMin ${String.format("%.2f", mintem - 5.2)} °C"
                   binding.sunrise.text = toTime(sunrise.toLong())
                   binding.sunset.text = toTime(sunset.toLong())
                   binding.sealable.text = sea.toString() + " kPa"
                   binding.temparatue.text = "$temparature °C"
                   binding.windspeed.text = "$windspeed m/s"

                   binding.day.text = day(System.currentTimeMillis())
                   binding.date.text = date()
                   binding.locations.text = "  "+ city

                   setBackground(condition)
               }
            }

            override fun onFailure(call: Call<WeatherApp>, t: Throwable) {
                Log.d("tag","the  response ${t.message} ")
                Toast.makeText(this@MainActivity, "Connection Fail", Toast.LENGTH_SHORT).show()
            }

        })
    }

    private fun setBackground(condition: String) {
        when(condition){
            "Clear Sky","Sunny","Clear" ->{
                binding.root.setBackgroundResource(R.drawable.sunny_background)
                binding.lotie.setAnimation(R.raw.sun)
            }
            "Clouds","Mist","Foggy" ->{
                binding.root.setBackgroundResource(R.drawable.colud_background)
                binding.lotie.setAnimation(R.raw.cloud)

            }
            "Light Rain","Drizzle","Heavy Rain","Moderate Rain" ->{
                binding.root.setBackgroundResource(R.drawable.rain_background)
                binding.lotie.setAnimation(R.raw.rain)
            }
            else->{
                binding.root.setBackgroundResource(R.drawable.sunny_background)
                binding.lotie.setAnimation(R.raw.sun)
            }
        }
        binding.lotie.playAnimation()

    }

    private fun date(): String{
        val sdf = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        return sdf.format(Date())
    }
    private fun toTime(time: Long): String {
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return sdf.format(Date(time * 1000))
    }
    private fun day(currentTimeMillis: Long): String {
        val sdf = SimpleDateFormat("EEEE", Locale.getDefault())
        return sdf.format(Date())

    }
}