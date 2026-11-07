package com.lockguard.security

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import com.lockguard.security.receiver.IntruderAdminReceiver

class MainActivity : ComponentActivity() {

    private lateinit var devicePolicyManager: DevicePolicyManager
    private lateinit var adminComponent: ComponentName

    private val deviceAdminLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (devicePolicyManager.isAdminActive(adminComponent)) {
            Toast.makeText(this, "تم تفعيل حارس أندرويد بنجاح!", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        devicePolicyManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        adminComponent = ComponentName(this, IntruderAdminReceiver::class.java)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 80, 50, 50)
            setBackgroundColor(0xFF0F172A.toInt())
        }

        val title = TextView(this).apply {
            text = "🛡️ حارس قفل الشاشة (Lock Guard)"
            textSize = 20f
            setTextColor(0xFFFFFFFF.toInt())
            setPadding(0, 0, 0, 30)
        }
        layout.addView(title)

        val desc = TextView(this).apply {
            text = "التطبيق جاهز لحماية هاتفك. عند إدخال رمز خاطئ، تلتقط الكاميرا الأمامية صورة المتطفل فوراً."
            textSize = 14f
            setTextColor(0xFF94A3B8.toInt())
            setPadding(0, 0, 0, 40)
        }
        layout.addView(desc)

        val adminBtn = Button(this).apply {
            text = if (devicePolicyManager.isAdminActive(adminComponent)) "مسؤول الجهاز مفعّل ✅" else "تفعيل مسؤول الجهاز (مطلوب)"
            setBackgroundColor(0xFF14B8A6.toInt())
            setTextColor(0xFF020617.toInt())
            setOnClickListener { requestDeviceAdmin() }
        }
        layout.addView(adminBtn)

        val batteryBtn = Button(this).apply {
            text = "إلغاء قيود البطارية (لتشغيل دائم)"
            setBackgroundColor(0xFF1E293B.toInt())
            setTextColor(0xFFF1F5F9.toInt())
            setOnClickListener { requestIgnoreBatteryOptimization() }
        }
        layout.addView(batteryBtn)

        setContentView(layout)
    }

    private fun requestDeviceAdmin() {
        if (!devicePolicyManager.isAdminActive(adminComponent)) {
            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent)
                putExtra(
                    DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                    "يتطلب تطبيق حارس قفل الشاشة هذه الصلاحية لمعرفة متى يتم إدخال رمز خاطئ والتقاط صورة للمتطفل فوراً."
                )
            }
            deviceAdminLauncher.launch(intent)
        } else {
            Toast.makeText(this, "الصلاحية مفعلة بالفعل!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun requestIgnoreBatteryOptimization() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = Uri.parse("package:$packageName")
                }
                startActivity(intent)
            } catch (e: Exception) {
                startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
            }
        }
    }
}