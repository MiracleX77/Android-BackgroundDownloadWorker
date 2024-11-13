package pos.background.downloadworker;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.ForegroundInfo;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * DownloadWorker: Worker สำหรับดาวน์โหลดไฟล์ใน background และเก็บใน MediaStore
 * ใช้ร่วมกับ Foreground Service เพื่อป้องกันการหยุดทำงานเมื่ออยู่ใน background
 */
public class DownloadWorker extends Worker {

    private static final String TAG = "DownloadWorker";
    private static final String CHANNEL_ID = "DownloadWorkerChannel";
    private static final int NOTIFICATION_ID = 1;

    private NotificationManager notificationManager;
    public DownloadWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);


    }

    @NonNull
    @Override
    public Result doWork() {
        Log.i(TAG, "doWork started in DownloadWorker");

        // URL ของไฟล์ที่ต้องการดาวน์โหลด :: Response Json
        String urlString = "http://10.0.2.2:8081/data";
        ContentResolver resolver = getApplicationContext().getContentResolver();

        try {
            // เริ่มเชื่อมต่อ URL
            URL url = new URL(urlString);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                Log.e(TAG, "Server returned HTTP response code: " + responseCode);
                return Result.retry();
            }

            // เริ่ม Foreground Service สำหรับดาวน์โหลด
            setForegroundAsync(createForegroundInfo("Starting Download"));

            InputStream inputStream = connection.getInputStream();

            // ตั้งค่า ContentValues สำหรับไฟล์ใน MediaStore
            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DISPLAY_NAME, "large.json");
            values.put(MediaStore.MediaColumns.MIME_TYPE, "application/json");
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, "Documents/PosData");

            // ตรวจสอบไฟล์เดิมใน MediaStore และลบหากพบไฟล์เดิม
            Cursor cursor = resolver.query(
                    MediaStore.Files.getContentUri("external"),
                    new String[]{MediaStore.MediaColumns._ID},
                    MediaStore.MediaColumns.DISPLAY_NAME + "=?",
                    new String[]{"large.json"},
                    null
            );

            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    long id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID));
                    Uri fileUriToDelete = Uri.withAppendedPath(MediaStore.Files.getContentUri("external"), String.valueOf(id));
                    resolver.delete(fileUriToDelete, null, null);
                    Log.i(TAG, "Deleted existing file successfully.");
                }
                cursor.close();
            }

            // เพิ่มไฟล์ลง MediaStore
            Uri fileUri = resolver.insert(MediaStore.Files.getContentUri("external"), values);

            // ดาวน์โหลดและบันทึกไฟล์
            OutputStream outputStream = null;
            try {
                assert fileUri != null;
                outputStream = resolver.openOutputStream(fileUri);
                if (outputStream == null) {
                    Log.e(TAG, "Failed to open output stream.");
                    return Result.failure();
                }

                BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    bufferedOutputStream.write(buffer, 0, bytesRead);
                }
                bufferedOutputStream.flush();
                Log.i(TAG, "File written successfully.");

            } catch (IOException e) {
                Log.e(TAG, "Error writing file", e);
                return Result.failure();
            } finally {
                if (inputStream != null) inputStream.close();
            }

            return Result.success();


        } catch (Exception e){
            e.fillInStackTrace();
            Log.e(TAG, "Error downloading file", e);
            return Result.retry();
        }
    }
    /**
     * สร้าง ForegroundInfo สำหรับแจ้งเตือนการดาวน์โหลด
     * @param progress ข้อความแสดงความคืบหน้า
     */
    private ForegroundInfo createForegroundInfo(@NonNull String progress) {
        Context context = getApplicationContext();
        String id = context.getString(R.string.notification_channel_id);
        String title = context.getString(R.string.notification_title);

        createChannel();

        Notification notification = new NotificationCompat.Builder(context, id)
                .setContentTitle(progress)
                .setTicker(title)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setOngoing(true)
                .build();

        return new ForegroundInfo(NOTIFICATION_ID, notification);
    }
    /**
     * สร้าง Notification Channel สำหรับการแจ้งเตือนการดาวน์โหลด
     */
    private void createChannel() {
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Download Channel",
                NotificationManager.IMPORTANCE_LOW
        );
        channel.setDescription("Channel for file download notifications");

        NotificationManager manager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.createNotificationChannel(channel);
        }
    }

}
