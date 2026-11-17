package com.tvpower

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.io.DataOutputStream

class MainActivity : AppCompatActivity() {

    private lateinit var btnReboot: Button
    private lateinit var btnShutdown: Button
    private lateinit var btnSleep: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnReboot = findViewById(R.id.btnReboot)
        btnShutdown = findViewById(R.id.btnShutdown)
        btnSleep = findViewById(R.id.btnSleep)

        // 重启：二次确认后执行 reboot
        btnReboot.setOnClickListener {
            confirmAction("重启盒子", "确定要现在重启吗？") {
                execRootCmd("reboot")
            }
        }

        // 关机：二次确认后执行 reboot -p（部分海思固件可改用 poweroff）
        btnShutdown.setOnClickListener {
            confirmAction("关机", "确定要现在关机吗？") {
                execRootCmd("reboot -p")
            }
        }

        // 睡眠：模拟电源键息屏，可逆，不弹确认
        btnSleep.setOnClickListener {
            val ok = execRootCmd("input keyevent KEYCODE_POWER")
            if (!ok) toast("执行失败：未获取 ROOT 权限")
        }

        // 兜底：进入界面把焦点给到“重启盒子”，保证遥控器首键即可操作
        btnReboot.requestFocus()
    }

    /** 二次确认弹窗，确定/取消按钮同样支持遥控器左右键切换 + 确认键触发 */
    private fun confirmAction(title: String, message: String, action: () -> Unit) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("确定") { _, _ -> action() }
            .setNegativeButton("取消", null)
            .show()
    }

    /**
     * 通过 su 执行 root shell 命令。
     * @return true 表示 su 进程已拉起；false 表示没有 ROOT 或执行异常。
     */
    private fun execRootCmd(cmd: String): Boolean {
        return try {
            val su = Runtime.getRuntime().exec("su")
            DataOutputStream(su.outputStream).use { os ->
                os.writeBytes("$cmd\n")
                os.flush()
            }
            su.waitFor()
            true
        } catch (e: Exception) {
            toast("执行失败：未获取 ROOT 权限")
            false
        }
    }

    private fun toast(text: String) {
        Toast.makeText(this, text, Toast.LENGTH_LONG).show()
    }
}
