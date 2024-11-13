package pos.background.downloadworker;

import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("StartApp","start");

        // เริ่มต้นแอปฯ และบันทึก log เพื่อระบุว่าแอปฯ ถูกเปิดใช้งานแล้ว
        // ตรวจสอบและขอสิทธิ์ให้ข้าม Battery Optimization เพื่อให้แอปฯ ทำงานใน background โดยไม่ถูกจำกัดจากระบบ
        BatteryOptimizationHelper.requestBatteryOptimizationPermission(this);

        // ปิดการใช้งาน MainActivity เพื่อป้องกันไม่ให้ผู้ใช้สามารถเข้าถึงหน้าจอหลักได้โดยตรง
        PackageManager packageManager = getPackageManager();
        ComponentName componentName = new ComponentName(this, MainActivity.class);
        packageManager.setComponentEnabledSetting(
                componentName,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED, // ปิดการใช้งาน Activity นี้
                PackageManager.DONT_KILL_APP // ไม่ฆ่าแอปฯ หลังปิด Activity นี้
        );

        // ปิด MainActivity ทันทีหลังจากเรียกใช้ เพื่อลดการใช้งานทรัพยากร
        finish();
    }
}
