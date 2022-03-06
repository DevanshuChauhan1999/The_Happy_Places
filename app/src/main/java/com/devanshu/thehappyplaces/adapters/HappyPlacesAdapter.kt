package com.devanshu.thehappyplaces.adapters

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.devanshu.thehappyplaces.R
import com.devanshu.thehappyplaces.models.HapplyPlaceModel
import de.hdodenhof.circleimageview.CircleImageView



class HappyPlacesAdapter(
    private val context: Context,
    private var list: ArrayList<HapplyPlaceModel>)
    : RecyclerView.Adapter<HappyPlacesAdapter.MyViewHolder>() {

    private var onClickListener: OnClickListener? = null


    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }
    interface OnClickListener {
        fun onClick(position: Int, model: HapplyPlaceModel)
    }




    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.item_happy_place,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = list[position]
        holder.ivPlaceImage.setImageURI(Uri.parse(currentItem.image))
        holder.tvTitle.text = currentItem.title
        holder.tvDescription.text = currentItem.description

        holder.itemView.setOnClickListener {

            if (onClickListener != null) {
                onClickListener!!.onClick(position, currentItem)
            }
        }

    }

    override fun getItemCount(): Int {
        return list.size
    }


    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val ivPlaceImage : CircleImageView = itemView.findViewById(R.id.iv_place_image)
        val tvTitle : TextView = itemView.findViewById(R.id.tv_title)
        val tvDescription : TextView = itemView.findViewById(R.id.tv_description)
    }

}