package vfv9w6.headsetcall.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kotlinx.android.synthetic.main.contact_row.view.*
import vfv9w6.headsetcall.R
import vfv9w6.headsetcall.data.Contact
import kotlin.concurrent.thread

class ContactRecyclerViewAdapter(private val contactList: ArrayList<Contact>) : RecyclerView.Adapter<ContactRecyclerViewAdapter.ViewHolder>() {

    val availablePresses: ArrayList<Int> = ArrayList(listOf(1, 2, 3, 4, 5, 6, 7, 8, 9))
    var itemClickListener: ContactItemClickListener? = null

    init {
        contactList.forEach {
            availablePresses.remove(it.pressCount) }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.contact_row, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val contact = contactList[position]

        holder.contact = contact

        holder.tvName.text = contact.name
        holder.tvPhoneNumber.text = contact.phoneNumber
        holder.tvPressCount.text = contact.pressCount.toString()
    }

    private fun addItem(contact: Contact) {
        val size = contactList.size
        contactList.add(contact)
        availablePresses.remove(contact.pressCount)
        thread { contact.save() }
        notifyItemInserted(size)
    }

    fun addOrModifyItem(contact: Contact, previousPressCount: Int) {
        if(!contactList.contains(contact))
            addItem(contact)
        else
        {
            availablePresses.remove(contact.pressCount)
            availablePresses.add(previousPressCount)
            availablePresses.sort()
            thread { contact.save() }
            notifyItemChanged(contactList.indexOf(contact))
        }
    }

    private fun deleteRow(position: Int) {
        val contact = contactList[position]
        thread { contact.delete() }
        availablePresses.add(contactList[position].pressCount)
        availablePresses.sort()
        contactList.removeAt(position)
        notifyItemRemoved(position)
    }

    fun deleteContact(contact: Contact) {
        deleteRow(contactList.indexOf(contact))
    }

    override fun getItemCount() = contactList.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.tv_name
        val tvPhoneNumber: TextView = view.tv_phone_number
        val tvPressCount: TextView = view.tv_press_count

        var contact: Contact? = null

        init {
            itemView.setOnLongClickListener { clickedView ->
                contact?.let { itemClickListener?.onItemLongClick(clickedView, it) }
                true
            }
        }
    }

    interface ContactItemClickListener {
        fun onItemLongClick(view: View, contact: Contact): Boolean
    }

}