package vcmsa.projects.budgetingbestie

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class AddCategory : AppCompatActivity() {

    private lateinit var edtName: EditText
    private lateinit var edtDescription: EditText
    private lateinit var edtNotes: EditText
    private lateinit var btnAddCategory: Button
    private val categoryService = CategoryRepository()
    private var currentUserId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_category)

        edtName = findViewById(R.id.edtName)
        edtDescription = findViewById(R.id.edtDescription)
        edtNotes = findViewById(R.id.edtNotes)
        btnAddCategory = findViewById(R.id.btnAddCategory)

        currentUserId = getCurrentUserId()
        if (currentUserId.isEmpty()) {
            Toast.makeText(this, "User not signed in", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        btnAddCategory.setOnClickListener {
            val name = edtName.text.toString().trim()
            val description = edtDescription.text.toString().trim()
            val notes = edtNotes.text.toString().trim()

            if (name.isEmpty()) {
                Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                try {
                    val existing = categoryService.getCategoryByName(currentUserId, name)
                    if (existing != null) {
                        Toast.makeText(this@AddCategory, "Category already exists with this name", Toast.LENGTH_SHORT).show()
                    } else {
                        val newCategory = Category(
                            name = name,
                            description = description,
                            notes = notes,
                            userId = currentUserId
                        )
                        categoryService.addCategory(newCategory)
                        Toast.makeText(this@AddCategory, "Category saved successfully: $name", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this@AddCategory, "Error saving category: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun getCurrentUserId(): String {
        val user = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        return user?.uid ?: ""
    }

}
