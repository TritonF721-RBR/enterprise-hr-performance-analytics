# 🏥 Sistem Pendukung Keputusan (SPK) Penilaian Kinerja HRD
Aplikasi berbasis *Web Fullstack* untuk membantu HRD Rumah Sakit/Perusahaan dalam menyeleksi dan mengevaluasi kinerja karyawan terbaik secara objektif dan otomatis.

## 🧠 Algoritma yang Digunakan
Sistem ini menggabungkan dua algoritma cerdas:
1. **AHP (Analytical Hierarchy Process) & SAW:** Digunakan untuk pembobotan multikriteria (Kualitas Kerja, Kedisiplinan, Tanggung Jawab, dll) dan pemeringkatan kandidat awal secara matematis.
2. **C4.5 (Decision Tree / Data Mining):** Bertindak sebagai *Machine Learning* dasar untuk mengekstrak pola dari data hasil AHP, lalu secara otomatis membuat aturan klasifikasi akhir (TERBAIK, TERPILIH, atau GAGAL) beserta alasan spesifiknya.

## 🚀 Fitur Utama
* **Secure Authentication:** Implementasi login aman menggunakan JWT (JSON Web Token) & Spring Security.
* **Bulk Upload Excel:** Fitur *import* data karyawan dan nilai dari file `.xlsx` secara massal.
* **Dynamic Evaluation:** Sistem memvalidasi dan mengonversi input nilai (misal: jumlah hari absen) ke dalam skala pembobotan secara *real-time*.
* **Automated Reporting:** *Generate* laporan keputusan akhir dan *Decision Tree* (Pola) yang siap dicetak ke format PDF.

## 🛠️ Tech Stack
* **Backend:** Java, Spring Boot, Spring Security, JWT, Spring Data JPA.
* **Database:** PostgreSQL.
* **Frontend:** HTML5, CSS3, JavaScript, Bootstrap 5, SheetJS.

## 📸 Dokumentasi / Screenshot
*(Catatan: Tarik dan lepas gambar screenshot dashboard, hasil ranking, dan hasil cetak PDF kamu ke sini nanti di GitHub)*

---
*Developed as a showcase of Fullstack Development & Algorithm Implementation.*