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

class ContactRecyclerViewAdapter(context: Context) : RecyclerView.Adapter<ContactRecyclerViewAdapter.ViewHolder>() {

    init {
        //TODO should call somewhere else
        SugarContext.init(context)
    }
    private val contactList = SugarRecord.listAll(Contact::class.java)

    var itemClickListener: ContactItemClickListener? = null

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

    fun addItem(contact: Contact) {
        val size = contactList.size
        contactList.add(contact)
        contact.save()
        notifyItemInserted(size)
    }

//    fun addAll(contacts: List<Contact>) {
//        val size = contactList.size
//        contactList += contacts
//        notifyItemRangeInserted(size, contacts.size)
//    }

    fun deleteRow(position: Int) {
        contactList[position].delete()
        contactList.removeAt(position)
        notifyItemRemoved(position)
    }

    override fun getItemCount() = contactList.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.tv_name
        val tvPhoneNumber: TextView = view.tv_phone_number
        val tvPressCount: TextView = view.tv_press_count

        var contact: Contact? = null

        init {
            itemView.setOnClickListener {
                contact?.let { itemClickListener?.onItemClick(it) }
            }

            itemView.setOnLongClickListener { clickedView ->
                itemClickListener?.onItemLongClick(adapterPosition, clickedView)
                true
            }
        }
    }

    interface ContactItemClickListener {
        fun onItemClick(contact: Contact)
        fun onItemLongClick(position: Int, view: View): Boolean
    }

}