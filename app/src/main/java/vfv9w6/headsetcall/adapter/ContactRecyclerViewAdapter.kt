package vfv9w6.headsetcall.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.orm.SugarContext
import com.orm.SugarRecord
import kotlinx.android.synthetic.main.contact_row.view.*
import vfv9w6.headsetcall.R
import vfv9w6.headsetcall.data.Contact
import java.util.*

class ContactRecyclerViewAdapter(context: Context) : RecyclerView.Adapter<ContactRecyclerViewAdapter.ViewHolder>() {
    private val contactList: ArrayList<Contact>
    val availablePresses: ArrayList<Int>
    var itemClickListener: ContactItemClickListener? = null

    init {
        //TODO should call somewhere else maybe
        SugarContext.init(context) //TODO call terminate too
        contactList = ArrayList(SugarRecord.listAll(Contact::class.java))
        availablePresses = ArrayList()
        availablePresses.addAll(listOf(1, 2, 3, 4, 5, 6, 7, 8, 9))
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
        contact.save()
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
            contact.save()
            notifyItemChanged(contactList.indexOf(contact))
        }
    }

//    fun addAll(contacts: List<Contact>) {
//        val size = contactList.size
//        contactList += contacts
//        notifyItemRangeInserted(size, contacts.size)
//    }

    fun deleteRow(position: Int) {
        contactList[position].delete()
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