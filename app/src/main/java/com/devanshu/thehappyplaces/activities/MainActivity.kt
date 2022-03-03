package com.devanshu.thehappyplaces.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.devanshu.thehappyplaces.database.DatabaseHandler
import com.devanshu.thehappyplaces.databinding.ActivityMainBinding
import com.devanshu.thehappyplaces.models.HapplyPlaceModel

class MainActivity : AppCompatActivity() {

    private var binding:ActivityMainBinding?= null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        binding?.fabAddHappyPlaces?.setOnClickListener {
            val intent = Intent( this@MainActivity, AddHappyPlacesActivity::class.java)
            startActivity(intent)
        }
        getHappyPlacesListFromLocalDB()
    }

    private fun getHappyPlacesListFromLocalDB(){
        val dbHandler = DatabaseHandler(this)
        val getHappyPlaceList : ArrayList<HapplyPlaceModel> = dbHandler.getHappyPlacesList()

        if (getHappyPlaceList.size > 0){
            for (i in getHappyPlaceList){
                Log.e("Title", i.title)
                Log.e("desc",i.description)
            }
        }
    }
}