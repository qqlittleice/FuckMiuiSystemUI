package com.yuk.fuckMiuiSystemUI

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
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
                val classIfExists = XposedHelpers.findClassIfExists("com.android.systemui.statusbar.views.MiuiClock", lpparam.classLoader)
                XposedHelpers.findAndHookConstructor(classIfExists, Context::class.java, AttributeSet::class.java, Integer.TYPE,
                    object : XC_MethodHook() {
                        override fun afterHookedMethod(param: MethodHookParam) {
                            val textV = param.thisObject as TextView
                            val h = Handler(textV.context.mainLooper)
                            class T : TimerTask() {
                                override fun run() {
                                    val r = Runnable { val declaredMethod: Method = textV.javaClass.getDeclaredMethod("updateTime", *arrayOfNulls<Class<*>>(0)); declaredMethod.isAccessible = true; textV.text = ""; declaredMethod.invoke(textV, *arrayOfNulls<Class<*>>(0)) }; h.post(r)
                                }
                            }
                            if (textV.resources.getResourceEntryName(textV.id) == "clock") {
                                var t: Timer? = null; if (t == null) t = Timer(); t.scheduleAtFixedRate(T(), 1050 - System.currentTimeMillis() % 1000, 1000)
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
                                TimeZone.setDefault(TimeZone.getDefault()); val c = Calendar.getInstance(); val f: DateFormat = SimpleDateFormat("hh:mm:ss"); val d = c.time; val t : String = f.format(d); textV.text = t
                            }
                        }
                    })
            }
            else -> return
        }
    }
}