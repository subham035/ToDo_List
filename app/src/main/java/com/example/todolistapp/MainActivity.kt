package com.example.todolistapp

import ToDoModel
import android.os.Bundle
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.core.view.View

class MainActivity : AppCompatActivity() ,UpdateAndDelete{
    private lateinit var database: DatabaseReference
    var toDoList: MutableList<ToDoModel>? =null
    lateinit var adapter:ToDoAdapter
    private var listViewItem: ListView? =null

    private fun addItemToList(snapshot: DataSnapshot) {
        val items=snapshot.children.iterator()

        if (items.hasNext()){

            val toDoIndexedValue=items.next()
            val itemsIterator =toDoIndexedValue.children.iterator()

            while (itemsIterator.hasNext()) {
                val currentItem =itemsIterator.next()
                val toDoItemData= ToDoModel.createList()
                val map= currentItem.getValue() as HashMap<String,Any>

                toDoItemData.UID =currentItem.key
                toDoItemData.done=map.get("done") as Boolean
                toDoItemData.itemDataText=map.get("itemDataText") as String
                toDoList!!.add(toDoItemData)
            }

        }

        adapter.notifyDataSetChanged()

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        val fab=findViewById<android.view.View>(R.id.fab) as FloatingActionButton

        listViewItem= findViewById<ListView>(R.id.item_listview)!!

        database= FirebaseDatabase.getInstance().reference
        fab.setOnClickListener {
            val alertDialog = AlertDialog.Builder(this)
            val textEditText = EditText(this)
            alertDialog.setMessage("Add TODO item")
            alertDialog.setTitle("Enter TODO item")
            alertDialog.setPositiveButton("Add"){ dialog, i ->
                val todoItemData =ToDoModel.createList()
                todoItemData.itemDataText=textEditText.text.toString()
                todoItemData.done= false

                val newItemData =database.child("todo").push()
                todoItemData.UID = newItemData.key

                newItemData.setValue(todoItemData)

                dialog.dismiss()
                Toast.makeText(this,"item saved",Toast.LENGTH_LONG).show()

            }
            alertDialog.setView(textEditText)
            alertDialog.show()
        }






        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        toDoList= mutableListOf<ToDoModel>()
        adapter= ToDoAdapter(this,toDoList!!)
        listViewItem!!.adapter=adapter
        database.addValueEventListener(object : ValueEventListener{
         override   fun onCancelled(error:DatabaseError){
                Toast.makeText(applicationContext, "No item Added", Toast.LENGTH_LONG).show()
            }

         override   fun onDataChange(snapshot: DataSnapshot){
                toDoList!!.clear()
                addItemToList(snapshot)
            }
        })



    }

    override fun modifyItem(itemUID: String, isDone: Boolean) {
        val itemReference=database.child("todo").child(itemUID)
        itemReference.child("done").setValue(isDone)
    }

    override fun onItemDelete(itemUID: String) {
        val itemReference= database.child("todo").child(itemUID)
        itemReference.removeValue()
        adapter.notifyDataSetChanged()




    }
}