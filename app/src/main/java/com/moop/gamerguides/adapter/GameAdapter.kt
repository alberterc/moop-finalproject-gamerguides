package com.moop.gamerguides.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.android.material.imageview.ShapeableImageView
import com.moop.gamerguides.R
import com.moop.gamerguides.adapter.model.Games
import com.squareup.picasso.Picasso


class GameAdapter(options: FirebaseRecyclerOptions<Games>):
    FirebaseRecyclerAdapter<Games, GameAdapter.GameViewHolder>(options) {

    class GameViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val gameTitle: TextView = itemView.findViewById(R.id.game_title)
        val gameImage: ShapeableImageView = itemView.findViewById(R.id.game_image)
        val gameLayout: RelativeLayout = itemView.findViewById(R.id.game_layout)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.item_game, parent, false)
        return GameViewHolder(view)
    }

    override fun onBindViewHolder(holder: GameViewHolder, position: Int, model: Games) {
        // get game title from firebase database
        holder.gameTitle.text = model.title
        Picasso.get()
            .load(model.image)
            .into(holder.gameImage)

        // game onclick function
        holder.gameLayout.setOnClickListener {
            Toast.makeText(holder.gameTitle.context, holder.gameTitle.text.toString(), Toast.LENGTH_SHORT)
                .show()
        }
    }
}