package com.example.core.utils

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager

object GhostModeManager {
    fun isGhostModeEnabled(context: Context): Boolean {
        val pm = context.packageManager
        val ghostAlias = ComponentName(context, "com.example.GhostActivityAlias")
        val state = pm.getComponentEnabledSetting(ghostAlias)
        return state == PackageManager.COMPONENT_ENABLED_STATE_ENABLED
    }

    fun setGhostMode(context: Context, enable: Boolean) {
        val pm = context.packageManager
        val mainAlias = ComponentName(context, "com.example.MainActivityAlias")
        val ghostAlias = ComponentName(context, "com.example.GhostActivityAlias")

        if (enable) {
            pm.setComponentEnabledSetting(
                ghostAlias,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
            )
            pm.setComponentEnabledSetting(
                mainAlias,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP
            )
        } else {
            pm.setComponentEnabledSetting(
                mainAlias,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
            )
            pm.setComponentEnabledSetting(
                ghostAlias,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP
            )
        }
    }
}
