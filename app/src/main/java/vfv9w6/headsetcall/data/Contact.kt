package vfv9w6.headsetcall.data

import com.orm.SugarRecord

class Contact(var name: String = "",
              var phoneNumber: String = "",
              var pressCount: Int = 0): SugarRecord()

