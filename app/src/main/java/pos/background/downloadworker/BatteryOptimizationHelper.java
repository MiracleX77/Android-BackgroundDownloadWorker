package pos.background.downloadworker;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;

public class BatteryOptimizationHelper {

    /**
     * Method สำหรับขอสิทธิ์ข้ามการ Optimize Battery เพื่อให้แอปฯ ทำงานใน background โดยไม่ถูกระบบปิดหรือจำกัด
     * ใช้ได้ตั้งแต่ Android M (API 23) ขึ้นไป
     *
     * @param context Context ของแอปฯ ที่เรียกใช้ Method นี้
     */
    public static void requestBatteryOptimizationPermission(Context context) {
        // ตรวจสอบว่าเป็น Android เวอร์ชัน M (API 23) หรือสูงกว่า
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);

            // ตรวจสอบว่าแอปฯ ได้รับการข้ามการ Optimize Battery แล้วหรือยัง
            if (pm != null && !pm.isIgnoringBatteryOptimizations(context.getPackageName())) {
                // หากยังไม่ได้รับสิทธิ์ ให้เรียก Intent เพื่อขอสิทธิ์ข้ามการ Optimize Battery
                Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + context.getPackageName()));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);

                // บันทึก log เพื่อระบุว่ากำลังขอสิทธิ์ข้ามการ Optimize Battery
                Log.i("BatteryOptimization", "Requesting to ignore battery optimizations.");
            } else {
                // ถ้าได้รับสิทธิ์ข้ามการ Optimize Battery แล้ว บันทึก log ไว้
                Log.i("BatteryOptimization", "Already ignoring battery optimizations.");
            }
        }
    }
}