package com.nitanmarcel.isodriveui

import android.app.Application
import com.topjohnwu.superuser.Shell

class IsoDriveApp : Application() {

    override fun onCreate() {
        super.onCreate()
        Shell.setDefaultBuilder(
            Shell.Builder.create()
                .setFlags(Shell.FLAG_REDIRECT_STDERR)
                .setTimeout(15)
        )
    }
}
