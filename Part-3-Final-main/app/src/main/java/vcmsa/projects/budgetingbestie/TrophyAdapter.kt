package vcmsa.projects.budgetingbestie

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TrophyAdapter : RecyclerView.Adapter<TrophyAdapter.TrophyViewHolder>() {

    private var trophyList = listOf<Trophy>()

    fun submitList(list: List<Trophy>) {
        trophyList = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrophyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_trophy, parent, false)
        return TrophyViewHolder(view)
    }

    override fun onBindViewHolder(holder: TrophyViewHolder, position: Int) {
        holder.bind(trophyList[position])
    }

    override fun getItemCount(): Int = trophyList.size

    inner class TrophyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(R.id.trophyTitle)
        private val date: TextView = itemView.findViewById(R.id.trophyDate)
        private val description: TextView = itemView.findViewById(R.id.trophyDescription)
        private val image: ImageView = itemView.findViewById(R.id.trophyImage)

        fun bind(trophy: Trophy) {
            title.text = trophy.title
            date.text = trophy.dateAchieved
            description.text = trophy.description
            image.setImageResource(trophy.imageRes) // Replace with your icon
        }
    }
}
