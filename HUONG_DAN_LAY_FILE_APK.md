# Hướng dẫn lấy file cài đặt `.apk` (CoC Builder Alert + Widget màn hình chính)

Bộ mã nguồn này đã tích hợp sẵn:
1. Toàn bộ giao diện và tính năng trong `coc_builder_notifier.html` (`app/src/main/assets/index.html`).
2. **Báo thức ngầm chính xác (`AlarmManager`)** hoạt động kể cả khi vuốt tắt ứng dụng hoặc khoá màn hình.
3. **Widget Android gốc (`CoCWidgetProvider`)** để kéo thả trực tiếp ra ngoài màn hình chính điện thoại Oppo / Android.

---

### Cách 1: Đóng gói `.apk` siêu tốc trong 30 giây bằng WebIntoApp (Không cần cài phần mềm)
1. Truy cập trang miễn phí **https://www.webintoapp.com/html-to-app**
2. Tải duy nhất file `app/src/main/assets/index.html` (chính là file `coc_builder_notifier.html`) lên.
3. Đặt tên App là **CoC Builder Alert** -> Bấm **Make App** để tải ngay file `.apk` về điện thoại cài đặt.

### Cách 2: Build bản Full Native (Có cả Widget màn hình chính Android) trên GitHub Actions (Miễn phí, không nặng máy)
1. Tạo 1 repository mới trên GitHub và kéo toàn bộ thư mục giải nén này lên.
2. Vào tab **Actions** trên GitHub -> bấm **Build CoC Builder Alert APK** -> tải file `CoC-Builder-Alert-APK.zip` chứa sẵn `app-debug.apk`.

### Cách 3: Mở bằng Android Studio trên máy tính (Ổ D)
1. Giải nén file `.zip` này, mở thư mục bằng **Android Studio**.
2. Chọn **Build -> Build Bundle(s) / APK(s) -> Build APK(s)** để xuất file `app-debug.apk`.
