package com.devanshu.thehappyplaces.activities

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import com.devanshu.thehappyplaces.adapters.HappyPlacesAdapter
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
            //startActivityForResult(intent, ADD_PLACE_ACTIVITY_REQUEST_CODE)
            getResult.launch(intent)
        }
        getHappyPlacesListFromLocalDB()
    }

    private fun setupHappyPlacesRecyclerView(happyPlaceList: ArrayList<HapplyPlaceModel>){
        binding?.rvHappyPlacesList?.layoutManager = LinearLayoutManager(this)

        binding?.rvHappyPlacesList?.setHasFixedSize(true)

        val placesAdapter = HappyPlacesAdapter(this, happyPlaceList)
        binding?.rvHappyPlacesList?.adapter = placesAdapter

    }

    private fun getHappyPlacesListFromLocalDB(){
        val dbHandler = DatabaseHandler(this)
        val getHappyPlaceList : ArrayList<HapplyPlaceModel> = dbHandler.getHappyPlacesList()

        if (getHappyPlaceList.size > 0){
            binding?.rvHappyPlacesList?.visibility = View.VISIBLE
            binding?.tvNoRecordsAvailable?.visibility = View.GONE
            setupHappyPlacesRecyclerView(getHappyPlaceList)
        }else{
            binding?.rvHappyPlacesList?.visibility = View.GONE
            binding?.tvNoRecordsAvailable?.visibility = View.VISIBLE
        }
    }

    /*
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
            if (resultCode == ADD_PLACE_ACTIVITY_REQUEST_CODE){
            getHappyPlacesListFromLocalDB()
            }
            else {
            Log.e("Activity","Cancelled or back pressed")
            }
    }
     */

    private val getResult = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) {
            if(it.resultCode == Activity.RESULT_OK){
                getHappyPlacesListFromLocalDB()
            } else {
                Log.e("Activity","Cancelled or back pressed")
            }
    }



    companion object{
        var ADD_PLACE_ACTIVITY_REQUEST_CODE =1
    }



}