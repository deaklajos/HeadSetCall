package vfv9w6.headsetcall.data

import com.orm.SugarRecord

class Contact(val name: String = "",
              val phoneNumber: String = "",
              val pressCount: Int = 0): SugarRecord()

