# Kế Hoạch Phát Triển TodoApp (Giai Đoạn 2 & Giai Đoạn 3)

Dựa trên nền tảng dự án hiện tại (Android, Kotlin, Jetpack Compose, Room, Firebase, WorkManager), tài liệu này phác thảo kế hoạch phát triển chi tiết cho Giai đoạn 2 và 3. Kế hoạch được thiết kế theo tư duy của một Project Manager, bám sát các tiêu chuẩn thiết kế ứng dụng quốc tế (như Habitica, TickTick, Apple Fitness) và nghiên cứu hành vi người dùng (Nudge theory, Gamification).

---

## Tầm Nhìn & Mục Tiêu Trải Nghiệm Người Dùng (UX)
- **Giảm ma sát (Frictionless):** Tối ưu hóa tối đa việc nhập liệu. Người dùng càng lười nhập, ứng dụng càng phải thông minh trong việc gợi ý.
- **Tạo thói quen (Habit-forming):** Sử dụng vòng lặp: **Gợi ý (Trigger) -> Hành động (Action) -> Phần thưởng (Reward)** thông qua Notification, Điểm số, Streak và Quote động lực.
- **Trực quan (Visual-first):** Sử dụng biểu đồ để người dùng "nhìn thấy" sự tiến bộ của bản thân thay vì chỉ đọc chữ.

---

## 🚀 Giai Đoạn 2: Cá Nhân Hóa & Gamification
*Tập trung vào việc định hình thói quen, đa dạng hóa loại công việc và tạo động lực cho người dùng.*

### 1. Tái cấu trúc Hệ thống Task (Task Categorization)
Tách biệt triệt để về mặt logic dữ liệu và UI/UX giữa Task Ngắn hạn và Task Dài hạn, đồng thời tuân thủ nguyên tắc thiết kế mở (Open-Closed Principle) để dễ dàng thêm các loại task mới (ví dụ: Project, Note) sau này.

*   **Task Thường xuyên / Dài hạn (Habits & Routines):**
    *   **Logic:** Hỗ trợ cấu hình lịch phức tạp. 
        *   *Lịch cố định:* Chọn các thứ trong tuần (VD: T3, T5, T7).
        *   *Lịch linh hoạt:* Số buổi / tuần, hoặc số buổi / tháng (VD: Tập Gym 3 buổi/tuần, ngày nào cũng được).
    *   **Giao diện (UI):** Thay vì check-list thông thường, sử dụng dạng lưới (Grid Tracker) hoặc thanh tiến trình vòng tròn để biểu thị tần suất.
*   **Task Ngắn hạn / Trong ngày (To-Dos):**
    *   **Logic:** Task có hạn chót cụ thể hoặc cần làm ngay trong ngày.
    *   **Giao diện (UI):** Check-list đơn giản, tập trung. Cung cấp chức năng "Snooze/Dời lịch" nhanh.
    *   **Daily Review (Đầu ngày):** Một màn hình Pop-up hoặc Banner đầu ngày tổng hợp: "Hôm nay bạn có 3 task cần làm và 2 task tồn đọng từ hôm qua".

### 2. Dashboard Thống Kê (Data Visualization)
Xây dựng một màn hình "Thống Kê" (Insights/Analytics) để người dùng theo dõi tiến độ.
*   **Nội dung:** 
    *   Tổng số task hoàn thành / chưa hoàn thành (Theo ngày, tuần, tháng).
    *   Tỷ lệ hoàn thành (Completion rate) dưới dạng phần trăm (%).
*   **Giao diện:** Sử dụng các thư viện vẽ biểu đồ Jetpack Compose (như Vico hoặc YCharts) để tạo Biểu đồ Tròn (Pie chart cho tỷ lệ) và Biểu đồ Cột (Bar chart cho số lượng theo ngày). UI cần mang hơi hướng của các app sức khỏe (Apple Health, Google Fit).

### 3. Gamification: Điểm số & Streak
Chuyển hóa quá trình hoàn thành mục tiêu thành một trò chơi để giữ chân người dùng (Retention).
*   **Hệ thống Điểm (Health Score):** 
    *   Khởi đầu và tối đa là 100 điểm. 
    *   **Phạt:** Bỏ lỡ một task trong ngày bị trừ điểm (-2 đ).
    *   **Thưởng:** Hoàn thành task khó hoặc đạt chuỗi liên tiếp được cộng điểm (+1, +3 đ).
    *   Mục tiêu tâm lý: Tâm lý con người ghét sự mất mát (Loss Aversion), việc cố gắng "Bảo vệ 100 điểm" sẽ hiệu quả hơn là việc cày từ 0 điểm lên.
*   **Streak (Chuỗi ngày liên tiếp):** Áp dụng đặc biệt cho các Task Thường xuyên. Hiển thị biểu tượng "Ngọn lửa" 🔥 đang cháy để kích thích người dùng không phá vỡ chuỗi.

### 4. Tối ưu Giao diện Gợi ý & Nhập liệu thông minh (Smart Inputs)
*   **Gợi ý Task (Suggestions):** Cung cấp các con chip/nút bấm gợi ý các task phổ biến (Đọc sách, Uống nước, Tập thể dục) bên dưới ô nhập text. App sẽ tự học và đẩy các task người dùng hay nhập lên đầu.
*   **Gợi ý Thời gian:** Khi người dùng tạo task, thay vì phải lướt vòng xoay giờ/phút, cung cấp sẵn các nút chọn nhanh thời lượng (15p, 30p, 1h) và thời hạn (Cuối ngày hôm nay, Sáng mai, Cuối tuần).

### 5. Morning Routine & Động lực hàng ngày
*   **Daily Quotes:** Tích hợp bộ sưu tập các câu châm ngôn, quote tạo động lực. Thay đổi ngẫu nhiên mỗi ngày và hiển thị trang trọng ở phần đầu của màn hình Home.
*   **Smart Notifications:** Sử dụng `AlarmManager` hoặc `WorkManager` gửi thông báo Local vào một giờ cố định (VD: 7:00 AM - có thể cấu hình).
    *   *Nội dung:* "Chào buổi sáng! Hãy thiết lập mục tiêu cho hôm nay nhé." kèm theo một câu quote nhỏ. Khi ấn vào sẽ mở ra màn hình Daily Review.

### 6. Cải tiến Widget (Glance)
Nâng cấp Widget hiện tại (vốn chỉ xem task cơ bản):
*   **Daily Widget:** Hiển thị task ngắn hạn trong ngày, cho phép check (Done) ngay trên màn hình chính mà không cần mở app.
*   **Habit/Status Widget:** Một widget nhỏ gọn chỉ hiển thị số Điểm hiện tại, số ngày Streak, và biểu đồ mini tiến độ trong tuần.

---

## 🚀 Giai Đoạn 3: Trải Nghiệm Chuyên Sâu & Tính Năng Xã Hội
*Tập trung vào quá trình thực thi công việc và mở rộng vòng kết nối người dùng.*

### 1. Đồng Hồ Đếm Ngược Thời Gian Thực (Focus/Pomodoro Mode)
Khi bắt đầu một task đòi hỏi thời gian (VD: Học tiếng anh 2 tiếng):
*   **Giao diện:** Mở ra màn hình "Focus" toàn màn hình. Hiển thị đồng hồ đếm ngược kiểu "Đồng hồ lật" (Flip clock) sắc nét, có thể kèm theo nhạc nền lo-fi/trắng (white noise) giúp tập trung.
*   **Chạy ngầm (Background):** Sử dụng `Foreground Service` của Android để đồng hồ vẫn đếm và hiển thị tiến trình (Progress) trên thanh Notification khi người dùng thoát ra Home.
*   **Chống xao nhãng:** Cấu hình để màn hình luôn sáng (Keep screen on) nếu người dùng đang ở màn hình Focus.

### 2. Tính Năng Chia Sẻ & Cộng Đồng (Social Connection)
Chuyển đổi TodoApp từ "Single-player" sang "Multiplayer", tận dụng Firebase Firestore đã có sẵn.
*   **Kết bạn:** Tìm và kết bạn qua mã định danh (ID) hoặc Email.
*   **Chia sẻ & Theo dõi:** 
    *   Người dùng có thể set một task là `Public` hoặc `Shared with Friends`.
    *   Bạn bè có thể xem tiến độ các task chung (VD: 2 người cùng cam kết chạy bộ). Có tính năng thả tim (React) hoặc nhắc nhở nhau (Poke).
*   **Leaderboard (Bảng xếp hạng):** 
    *   Tính tổng điểm hoặc tổng số ngày Streak trong tuần.
    *   Xếp hạng với bạn bè và Xếp hạng Toàn cầu.
    *   Phân hạng rank (Đồng, Bạc, Vàng) tạo tính ganh đua tích cực.

---

## 🛠 Lộ Trình Kỹ Thuật (Technical Roadmap cho Dev)

Để thực hiện các thay đổi trên source code hiện tại, dưới đây là định hướng kỹ thuật:
1.  **Database (Room & Firestore):** 
    *   Thực hiện Room Database Migration. Tạo thêm các bảng: `Habits` (Lưu lịch phức tạp), `TaskLogs` (Lưu lịch sử hoàn thành để vẽ biểu đồ), `UserProfile` (Lưu điểm số, Streak).
2.  **Background Processing:** 
    *   Viết một `DailyResetWorker` (WorkManager) chạy vào lúc 00:00 mỗi đêm để kiểm tra: Nếu task hàng ngày chưa làm -> trừ điểm; reset trạng thái ngày mới.
3.  **UI/UX (Jetpack Compose):** 
    *   Thiết kế lại hệ thống màu sắc (Color Tokens) cho Gamification.
    *   Áp dụng các Animation (Mavels/Lottie) cho các tương tác thưởng điểm, streak.
4.  **Kiến trúc:** Đảm bảo duy trì Offline-first. Các tính năng Social ở Giai đoạn 3 cần thiết kế hệ thống Sync (Conflict resolution) kỹ lưỡng vì dữ liệu lúc này có tính tương tác nhiều người.

> [!NOTE]
> Bản kế hoạch này được thiết kế đảm bảo sự cân bằng giữa giá trị thực tiễn (giúp người dùng quản lý công việc) và tâm lý học hành vi (giữ chân người dùng). Bạn có thể xem xét và phản hồi để điều chỉnh các tính năng cho phù hợp với tầm nhìn của bạn!
