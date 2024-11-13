package pos.background.downloadworker;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;

import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

/**
 * BootReceiver: ตัวรับการ Broadcast เพื่อเรียกใช้งานเมื่ออุปกรณ์บูตสำเร็จ
 * ใช้เพื่อเริ่มต้นงาน Periodic Work ด้วย WorkManager
 */
public class BootReceiver extends BroadcastReceiver {

    @Override
    @SuppressLint("BatteryLife")// ข้ามการแจ้งเตือน Battery Optimization
    public void onReceive(Context context, Intent intent) {
        Log.i("BootReceiver", "Received broadcast in BootReceiver"); // เพิ่ม log เพื่อช่วยในการ debug

        // ตรวจสอบว่า Intent คือการบูตเครื่องเสร็จสมบูรณ์หรือไม่
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {

            // สร้างงาน Periodic Work สำหรับดาวน์โหลดไฟล์ทุกๆ 1 ชั่วโมง
            PeriodicWorkRequest downloadWorkRequest = new PeriodicWorkRequest.Builder(
                    DownloadWorker.class,
                    1, TimeUnit.HOURS // กำหนดให้ทำงานทุกๆ 1 ชั่วโมง
            ).setConstraints(Constraints.NONE).build();

            // เริ่มต้นการทำงานของ WorkManager โดยตั้งค่าให้รีเซ็ตการทำงานหากมีงานอยู่แล้ว
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                    "DownloadWorker",
                    ExistingPeriodicWorkPolicy.REPLACE,// แทนที่งานเก่าด้วยงานใหม่
                    downloadWorkRequest
            );
        }
    }
}