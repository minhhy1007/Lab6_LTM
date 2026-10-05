# 🏆 PHASESUM: TỔNG BẰNG GIAO DIỆN VÀ CHỨC NĂNG ỨNG DỤNG NET SEND JAVA

---

## 📌 1. TỔNG QUAN GIAO DIỆN (GUI OVERVIEW)

Ứng dụng **Net Send Java Multicast & UDP** được thiết kế bằng thư viện **Java Swing** với mô hình Phân tầng (Layered Architecture). Giao diện được chia thành **4 Khu Vực Chức Năng Chính**, thể hiện đầy đủ các thành phần tĩnh và dữ liệu động phản hồi theo thời gian thực.

```text
┌─────────────────────────────────────────────────────────────────────────────┐
│ 🟢 1. BẢNG TRẠNG THÁI MẠNG (System Info Bar)                                │
│ Trạng thái: Đang Lắng Nghe | IP Máy này: 192.168.1.3 | Port: 9876 | Group: 225.1.2.3 │
├─────────────────────────────────────────────────────────────────────────────┤
│ 🔵 2. NHẬT KÝ TIN NHẮN & LOG HỆ THỐNG (Center Panel - 2 Ô ScrollPane)       │
│ ┌──────────────────────────────────────┐ ┌────────────────────────────────┐ │
│ │ NHẬT KÝ TIN NHẮN NHẬN ĐƯỢC (History) │ │ LỊCH SỬ HOẠT ĐỘNG (System Log) │ │
│ │ [21:06:20] [MULTICAST] Từ 192.168.1.5│ │ [21:05:38] Đã bật Receiver    │ │
│ │            ➔ Hello Multicast GUI!    │ │ [21:06:20] Gửi thành công ...  │ │
│ └──────────────────────────────────────┘ └────────────────────────────────┘ │
├─────────────────────────────────────────────────────────────────────────────┤
│ 🟡 3. BỘ GỬI TIN NHẮN (Send Controller Panel)                               │
│ Chế độ: (•) Unicast (1-1)   ( ) Multicast (Nhóm)   ( ) Broadcast (*)       │
│ Địa chỉ Đích (Target): [ 127.0.0.1             ]                            │
│ Nội dung (Message):   [ Nhập tin nhắn ở đây... ] [ 🚀 GỬI ] [ 🧹 Xóa Log ]  │
│ Lệnh thô (Raw):       [ net send 225.1.2.3 Hello ] [ Thực thi Lệnh ]        │
├─────────────────────────────────────────────────────────────────────────────┤
│ 🔴 4. QUẢN LÝ NHÓM MULTICAST & BẬT/TẮT THREAD                              │
│ IP Nhóm Multicast: [ 225.1.2.3 ]  [ Join Group ]  [ Leave Group ]  [ Tắt Nghe ]│
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 🔍 2. CHI TIẾT NỘI DUNG VÀ THUỘC TÍNH ĐỘNG / CỐ ĐỊNH TRÊN GIAO DIỆN

### 🟢 KHU VỰC 1: BẢNG TRẠNG THÁI MẠNG (System Info Bar - Top Panel)

| Thành phần GUI | Loại nội dung | Mô tả chức năng & Khả năng thay đổi |
| :--- | :--- | :--- |
| **Nhãn "Trạng thái"** | **Động (Dynamic)** | - Hiển thị `Trạng thái: Đang Lắng Nghe` (chữ xanh) khi tiến trình mạng chạy.<br>- Tự động đổi thành `Trạng thái: Đã Tắt` (chữ đỏ) khi người dùng bấm nút *Tắt Lắng Nghe*. |
| **Nhãn "IP Máy này"** | **Động (Dynamic)** | - Tự động phát hiện và hiển thị địa chỉ IP thực tế của card mạng LAN (`InetAddress.getLocalHost()`). Tự thay đổi tùy theo máy tính đang chạy phần mềm. |
| **Nhãn "Port"** | **Cố định (Static)** | - Hiển thị cổng UDP mặc định của hệ thống (`Port: 9876`). |
| **Nhãn "Nhóm Multicast"**| **Động (Dynamic)** | - Hiển thị địa chỉ Group IP hiện tại đang Join (mặc định: `225.1.2.3`). Tự cập nhật khi người dùng Join sang nhóm mới hoặc Leave nhóm. |

---

### 🔵 KHU VỰC 2: NHẬT KÝ TIN NHẮN & LOG HOẠT ĐỘNG (Center Panel)

| Ô Văn Bản (JTextArea) | Loại nội dung | Nội dung hiển thị & Cơ chế hoạt động |
| :--- | :--- | :--- |
| **Nhật ký tin nhắn nhận được (`txtHistory`)** | **Động (Dynamic)** | - Tự động append dòng mới khi có gói tin UDP gửi đến:<br>`[HH:mm:ss] [Loại_Gói] Từ [IP_Người_Gửi] ➔ [Nội_dung]`<br>- Cập nhật bất đồng bộ an toàn qua `SwingUtilities.invokeLater()`. |
| **Lịch sử hoạt động Log (`txtLog`)** | **Động (Dynamic)** | - Ghi vết thời gian thực mọi tác vụ: Bật/Tắt Thread, Gửi tin nhắn thành công, Chuyển đổi nhóm Multicast, và thông báo lỗi. |

---

### 🟡 KHU VỰC 3: BỘ GỬI TIN NHẮN (Send Controller Panel)

| Thành phần GUI | Loại nội dung | Chức năng tương tác & Khả năng thay đổi |
| :--- | :--- | :--- |
| **Bộ chọn Radio Button** | **Tương tác (Event)**| - Khi bấm chọn **Unicast (1-1)** ➔ Ô Target tự điền `127.0.0.1`.<br>- Khi bấm chọn **Multicast (Nhóm)** ➔ Ô Target tự điền IP Nhóm (`225.1.2.3`).<br>- Khi bấm chọn **Broadcast (*)** ➔ Ô Target tự điền `*`. |
| **Ô "Địa chỉ Đích"** | **Động (Editable)** | - Cho phép người dùng nhập trực tiếp IP máy, IP Group hoặc `*`. |
| **Ô "Nội dung Message"** | **Động (Editable)** | - Nhập chuỗi văn bản bản rõ (hỗ trợ nhiều từ và Tiếng Việt UTF-8). |
| **Nút "🚀 GỬI (NET SEND)"**| **Tương tác (Event)**| - Lấy dữ liệu từ ô Target & Message ➔ Đóng gói `ChatMessage` ➔ Gọi `UdpSender.send()` phát ra mạng. |
| **Nút "🧹 Xóa Nhật Ký"** | **Tương tác (Event)**| - Xóa sạch toàn bộ chữ đang hiển thị trong 2 ô JTextArea History & Log. |
| **Form "Nhập Lệnh Thô"** | **Tương tác (Event)**| - Cho phép gõ chuỗi lệnh chuẩn `net send 225.1.2.3 Hello` ➔ Bấm *Thực thi Lệnh* để đẩy qua lớp `CommandParser.parse()`. |

---

### 🔴 KHU VỰC 4: QUẢN LÝ NHÓM MULTICAST & THREAD (Bottom Panel)

| Thành phần GUI | Loại nội dung | Chức năng mạng tương ứng phía sau |
| :--- | :--- | :--- |
| **Ô "IP Nhóm Multicast"**| **Động (Editable)** | - Ô nhập địa chỉ IP Group Class D mới (ví dụ: `230.0.0.1`). |
| **Nút "Join Group"** | **Tương tác (Event)**| - Hủy nhóm cũ, khởi tạo tiến trình `MulticastReceiver` mới và gọi `socket.joinGroup(newIP)`. |
| **Nút "Leave Group"** | **Tương tác (Event)**| - Gọi `socket.leaveGroup()` và ngắt kết nối khỏi nhóm Multicast hiện tại. |
| **Nút "Tắt / Bật Lắng Nghe"**| **Tương tác (Event)**| - Đóng/Mở các Socket và dừng/chạy lại các Thread lắng nghe mạng. |

---

## 🔗 3. BẢNG ÁNH XẠ GIỮA THÀNH PHẦN GIAO DIỆN VÀ MÃ NGUỒN (GUI ↔ CLASS MAPPING)

```text
┌─────────────────────────┐          ┌─────────────────────────┐
│     GUI COMPONENT       │          │   CLASS XỬ LÝ PHÍA SAU   │
├─────────────────────────┼──────────┼─────────────────────────┤
│ Nút "GỬI (NET SEND)"    │ ───────> │ network.UdpSender       │
│ Nút "Thực thi Lệnh"     │ ───────> │ parser.CommandParser    │
│ Hiển thị Nhật ký        │ <─────── │ network.MessageListener │
│ Lắng nghe Unicast 1-1   │ <─────── │ network.UdpReceiver     │
│ Lắng nghe Multicast     │ <─────── │ network.MulticastReceiver│
│ Mô hình Dữ liệu Tin nhắn│ <─────── │ model.ChatMessage       │
└─────────────────────────┘          └─────────────────────────┘
```

---

## 🔄 4. QUY TRÌNH DỮ LIỆU ĐỘNG TRÊN GIAO DIỆN (DYNAMIC DATA FLOW)

### 📤 4.1. Luồng Gửi Tin Nhắn (GUI ➔ Network):
1. Người dùng nhập thông tin trên GUI và nhấn **🚀 GỬI (NET SEND)**.
2. Phương thức `handleSendAction()` trong [`MainFrame.java`](file:///d:/Lap_Trinh_Mang/Lab6_Multicast/src/gui/MainFrame.java#L209-L235) được kích hoạt.
3. Chuỗi lệnh được truyền qua [`CommandParser.parse()`](file:///d:/Lap_Trinh_Mang/Lab6_Multicast/src/parser/CommandParser.java#L16-L75) để tạo đối tượng [`ChatMessage`](file:///d:/Lap_Trinh_Mang/Lab6_Multicast/src/model/ChatMessage.java).
4. [`UdpSender.send()`](file:///d:/Lap_Trinh_Mang/Lab6_Multicast/src/network/UdpSender.java#L19-L44) kiểm tra `TargetType` và phát gói UDP Datagram qua card mạng.
5. Giao diện tự động thêm dòng Log màu xanh báo `GỬI THÀNH CÔNG` và xóa trắng ô nhập tin nhắn.

### 📥 4.2. Luồng Nhận Tin Nhắn (Network ➔ GUI):
1. Gói UDP đi tới cổng 9876 của máy.
2. Tiến trình ngầm [`UdpReceiver`](file:///d:/Lap_Trinh_Mang/Lab6_Multicast/src/network/UdpReceiver.java) hoặc [`MulticastReceiver`](file:///d:/Lap_Trinh_Mang/Lab6_Multicast/src/network/MulticastReceiver.java) giật được gói tin từ `socket.receive()`.
3. Receiver giải mã gói tin và gọi Callback [`MessageListener.onMessageReceived()`](file:///d:/Lap_Trinh_Mang/Lab6_Multicast/src/network/MessageListener.java).
4. Khối lệnh `SwingUtilities.invokeLater()` đẩy dữ liệu về luồng **Swing Event Dispatch Thread (EDT)**.
5. Ô `txtHistory` tự động chèn dòng tin nhắn mới kèm thời gian thực, IP người gửi và cuộn xuống dòng cuối cùng.
