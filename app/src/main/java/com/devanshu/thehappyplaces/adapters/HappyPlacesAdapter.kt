package com.devanshu.thehappyplaces.adapters

import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.devanshu.thehappyplaces.R
import com.devanshu.thehappyplaces.models.HapplyPlaceModel
import kotlinx.android.synthetic.main.item_happy_place.view.*


class HappyPlacesAdapter(private val dataList: ArrayList<HapplyPlaceModel>) : RecyclerView.Adapter<HappyPlacesAdapter.MyViewHolder>() {

    private lateinit var mListener: onItemClickListener
    interface onItemClickListener{
        fun onItemClick(position: Int)
    }
    fun setOnItemClickListener(listener: onItemClickListener){
        mListener = listener
    }




    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_happy_place, parent,false)
        return MyViewHolder(itemView,mListener)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = dataList[position]
        holder.itemView.iv_place_image.setImageURI(Uri.parse(currentItem.image))
        holder.itemView.tv_title.text = currentItem.title
        holder.itemView.tv_description.text = currentItem.description


    }

    override fun getItemCount(): Int {
        return dataList.size
    }


    class MyViewHolder(itemView: View, listener: onItemClickListener) : RecyclerView.ViewHolder(itemView){

        init {
            itemView.setOnClickListener {
                listener.onItemClick(absoluteAdapterPosition)
            }
        }
    }

}