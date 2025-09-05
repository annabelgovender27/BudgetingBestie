package vcmsa.projects.budgetingbestie

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class GoalsAdapter(private var goalsList: List<Goals>) :
    RecyclerView.Adapter<GoalsAdapter.GoalsViewHolder>() {

    class GoalsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvMin: TextView = view.findViewById(R.id.tvMin)
        val tvMax: TextView = view.findViewById(R.id.tvMax)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GoalsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.goal_layout, parent, false)
        return GoalsViewHolder(view)
    }

    override fun onBindViewHolder(holder: GoalsViewHolder, position: Int) {

        val goal = goalsList.lastOrNull()
        goal?.let {
            holder.tvMin.text = "Minimum Spending Goal: R${it.min}"
            holder.tvMax.text = "Maximum Spending Goal: R${it.max}"
        }
    }

    override fun getItemCount(): Int {
        // Show only one item (the latest goal), or 0 if the list is empty
        return if (goalsList.isNotEmpty()) 1 else 0
    }

    fun updateGoalsList(newList: List<Goals>) {
        goalsList = newList
        notifyDataSetChanged()
    }
}