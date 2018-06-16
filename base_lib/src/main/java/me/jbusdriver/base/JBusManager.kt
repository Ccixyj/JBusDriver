package me.jbusdriver.base

import android.app.Activity
import android.app.Application
import android.os.Bundle
import java.lang.ref.WeakReference

object JBusManager : Application.ActivityLifecycleCallbacks {

    val manager = mutableListOf<WeakReference<Activity>>()

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        manager.add(WeakReference(activity))
    }

    override fun onActivityStarted(activity: Activity?) {
    }

    override fun onActivityPaused(activity: Activity?) {
    }

    override fun onActivityResumed(activity: Activity?) {
    }

    override fun onActivityStopped(activity: Activity?) {
    }

    override fun onActivityDestroyed(activity: Activity?) {
        manager.removeAll { it.get() == activity }
    }

    override fun onActivitySaveInstanceState(activity: Activity?, outState: Bundle?) {
    }
}