package vfv9w6.headsetcall

import android.app.Application
import com.orm.SugarContext

class HeadsetCallApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        SugarContext.init(this)
    }

    override fun onTerminate() {
        SugarContext.terminate()
        super.onTerminate()
    }
}