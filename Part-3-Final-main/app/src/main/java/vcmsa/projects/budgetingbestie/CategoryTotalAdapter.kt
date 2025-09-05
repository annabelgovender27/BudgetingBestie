package vcmsa.projects.budgetingbestie

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CategoryTotalAdapter(private var data: List<CategoryTotal>) :
    RecyclerView.Adapter<CategoryTotalAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvCategoryName: TextView = view.findViewById(R.id.tvCategoryName)
        val tvCategoryTotal: TextView = view.findViewById(R.id.tvCategoryTotal)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category_total, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]
        holder.tvCategoryName.text = item.category
        holder.tvCategoryTotal.text = "R%.2f".format(item.total)
    }

    fun updateData(newList: List<CategoryTotal>) {
        data = newList
        notifyDataSetChanged()
    }
}