package com.yuk.fuckMiuiSystemUI

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.os.Handler
import android.provider.Settings
import android.util.AttributeSet
import android.widget.TextView
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import java.lang.reflect.Method
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class XposedInit : IXposedHookLoadPackage {

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        when (lpparam.packageName) {
            "com.android.systemui" -> {
                var c :Context? = null
                val classIfExists = XposedHelpers.findClassIfExists("com.android.systemui.statusbar.views.MiuiClock", lpparam.classLoader)
                XposedHelpers.findAndHookConstructor(classIfExists, Context::class.java, AttributeSet::class.java, Integer.TYPE,
                    object : XC_MethodHook() {
                        override fun afterHookedMethod(param: MethodHookParam) {
                            c = param.args[0] as Context
                            val textV = param.thisObject as TextView
                            val h = Handler(textV.context.mainLooper)
                            class T : TimerTask() {
                                override fun run() {
                                    val r = Runnable {
                                        val d: Method = textV.javaClass.getDeclaredMethod("updateTime", *arrayOfNulls<Class<*>>(0))
                                        d.isAccessible = true; textV.text = ""
                                        d.invoke(textV, *arrayOfNulls<Class<*>>(0))
                                    }
                                    h.post(r)
                                }
                            }
                            if (textV.resources.getResourceEntryName(textV.id) == "clock") {
                                var t: Timer? = null
                                if (t == null) t = Timer()
                                t.scheduleAtFixedRate(T(), 1050 - System.currentTimeMillis() % 1000, 1000)
                            }
                        }
                    }
                )
                XposedHelpers.findAndHookMethod(classIfExists, "updateTime",
                    object : XC_MethodHook() {
                        @SuppressLint("SetTextI18n", "SimpleDateFormat")
                        override fun afterHookedMethod(param: MethodHookParam) {
                            val textV = param.thisObject as TextView
                            if (textV.resources.getResourceEntryName(textV.id) == "clock") {
                                val cv: ContentResolver = c!!.contentResolver
                                val strTimeFormat = Settings.System.getString(cv, Settings.System.TIME_12_24)
                                TimeZone.setDefault(TimeZone.getDefault())
                                val f : DateFormat = if (strTimeFormat == "24") SimpleDateFormat("HH:mm:ss") else SimpleDateFormat("ah:mm:ss")
                                val ca = Calendar.getInstance()
                                val d = ca.time
                                val t : String = f.format(d)
                                textV.text = t
                            }
                        }
                    })
            }
            else -> return
        }
    }
}