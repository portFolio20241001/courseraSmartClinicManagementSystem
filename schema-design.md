## MySQL Database Design
# スマートクリニック管理システム - データベース設計書（NOT NULL 指定付き）

本設計書では、MySQL データベースの各テーブルに NOT NULL 制約を明示しています。  
NOT NULL によって「必須入力項目」を定義し、データの整合性を強化します。

---

## 💾 MySQL テーブル定義（NOT NULL 明示）

### 🧑 Table: patients（患者情報）

| カラム名       | データ型                            | 制約                   | 説明                       |
|----------------|-----------------------------------|------------------------|----------------------------|
| id             | INT, PRIMARY KEY, AUTO_INCREMENT  | NOT NULL               | 主キー：患者ID             |
| name           | VARCHAR(100)                      | NOT NULL               | 氏名                       |
| email          | VARCHAR(100), UNIQUE              | NOT NULL, UNIQUE       | メールアドレス（重複不可） |
| phone          | VARCHAR(20)                      | NOT NULL               | 電話番号                   |
| date_of_birth  | DATE                             | NOT NULL               | 生年月日                   |
| gender         | ENUM('male', 'female', 'other')  | NOT NULL               | 性別                       |
| created_at     | DATETIME DEFAULT CURRENT_TIMESTAMP | NOT NULL             | 登録日時                   |

---

### 🩺 Table: doctors（医師情報）

| カラム名             | データ型                             | 制約                          | 説明                                           |
|----------------------|--------------------------------------|-------------------------------|------------------------------------------------|
| id                   | BIGINT, PRIMARY KEY, AUTO_INCREMENT | NOT NULL                      | 主キー：医師ID（自動採番）                    |
| name                 | VARCHAR(100)                         | NOT NULL                      | 医師の氏名（3〜100文字）                      |
| specialty            | VARCHAR(50)                          | NOT NULL                      | 専門分野（3〜50文字）                         |
| email                | VARCHAR(100), UNIQUE                 | NOT NULL, UNIQUE              | メールアドレス（有効形式、重複不可）         |
| password             | VARCHAR(255)                         | NOT NULL                      | パスワード（6文字以上、JSON出力時は非表示）   |
| phone                | VARCHAR(10)                          | NOT NULL                      | 電話番号（数字10桁）                          |
| available_times      | TEXT (List 形式として保存)           | NULL許容                      | 診療可能時間帯（例："09:00-10:00"の文字列配列）|
| created_at           | DATETIME                             | NOT NULL, DEFAULT CURRENT_TIMESTAMP | 登録日時（自動設定、更新不可）         |

---

### 🏥 Table: clinic_locations（クリニック所在地）

| カラム名    | データ型                            | 制約                   | 説明                       |
|-------------|-----------------------------------|------------------------|----------------------------|
| id          | INT, PRIMARY KEY, AUTO_INCREMENT  | NOT NULL               | 主キー：所在地ID           |
| name        | VARCHAR(100)                      | NOT NULL               | クリニック名               |
| address     | VARCHAR(255)                     | NOT NULL               | 所在地住所                 |
| phone       | VARCHAR(20)                      | NOT NULL               | 電話番号                   |
| created_at  | DATETIME DEFAULT CURRENT_TIMESTAMP | NOT NULL             | 登録日時                   |

---

### 📅 Table: appointments（予約情報）

| カラム名            | データ型                            | 制約                                                   | 説明                       |
|---------------------|-----------------------------------|--------------------------------------------------------|----------------------------|
| id                  | BIGINT, PRIMARY KEY, AUTO_INCREMENT | NOT NULL                                               | 主キー：予約ID             |
| patient_id          | BIGINT                            | NOT NULL, FOREIGN KEY → patients(id)                   | 外部キー：患者ID           |
| doctor_id           | BIGINT                            | NOT NULL, FOREIGN KEY → doctors(id)                    | 外部キー：医師ID           |
| appointment_time    | DATETIME                         | NOT NULL, 未来日時制約（アプリケーション側でバリデーション） | 予約日時                   |
| status              | INT                              | NOT NULL, 0=Scheduled, 1=Completed, 2=Cancelled（整数で管理） | 予約ステータス             |
| notes               | TEXT                             | NULL（任意）                                           | 備考                       |

---

### 💳 Table: payments（支払い情報）

| カラム名         | データ型                            | 制約                                                   | 説明                       |
|------------------|-----------------------------------|--------------------------------------------------------|----------------------------|
| id               | INT, PRIMARY KEY, AUTO_INCREMENT  | NOT NULL                                               | 主キー：支払いID           |
| patient_id       | INT                               | NOT NULL, FOREIGN KEY → patients(id)                   | 外部キー：患者ID           |
| appointment_id   | INT                               | NOT NULL, FOREIGN KEY → appointments(id)               | 外部キー：予約ID           |
| amount           | DECIMAL(10,2)                     | NOT NULL                                               | 支払い金額                 |
| payment_method   | ENUM('cash', 'credit', 'insurance') | NOT NULL                                             | 支払い方法                 |
| status           | ENUM('Paid', 'Pending', 'Failed') | NOT NULL                                               | 支払いステータス           |
| paid_at          | DATETIME                         | NULL（未払いの場合NULL）                               | 支払日時（支払済のみ）     |
| created_at       | DATETIME DEFAULT CURRENT_TIMESTAMP | NOT NULL                                             | レコード作成日時           |

---

### 🛠️ Table: admins（管理者情報）

| カラム名         | データ型                            | 制約                   | 説明                       |
|------------------|-----------------------------------|------------------------|----------------------------|
| id               | INT, PRIMARY KEY, AUTO_INCREMENT  | NOT NULL               | 主キー：管理者ID           |
| username         | VARCHAR(50), UNIQUE               | NOT NULL, UNIQUE       | ユーザー名（ログイン用）   |
| password_hash    | VARCHAR(255)                      | NOT NULL               | ハッシュ化パスワード（bcrypt）       |
| role             | ENUM('admin', 'superadmin')       | NOT NULL               | 権限                       |
| created_at       | DATETIME DEFAULT CURRENT_TIMESTAMP | NOT NULL             | 登録日時                   |

---

## ✅ 補足事項

- **必須入力項目**：NOT NULL を設定することで、アプリケーションやユーザーの入力ミスを防止。  
- **外部キー制約**：appointments.patient_id や doctor_id は参照整合性を担保。  
- **ユニーク制約**：メールアドレスやユーザー名は一意に保つ。  
- **notes や paid_at のような任意項目は NULL を許容**。  

---


## 📦 MongoDB 利用概要

MongoDB は、柔軟なスキーマ設計が求められるデータを格納するために使用します。  
特に以下のようなデータを想定しています。

- **prescriptions（処方箋）**  
- **feedbacks（患者・医師のフィードバック）**  
- **logs（システムログや操作履歴）**

---

### 💊 Collection: prescriptions（処方箋）

```json
{
  "_id": "ObjectId('64abc123456')",            // MongoDB固有のドキュメントID
  "patientId": 101,                            // MySQLのpatients.idと紐づく数値ID
  "appointmentId": 51,                         // MySQLのappointments.idと紐づく数値ID
  "medication": "パラセタモール",
  "dosage": "500mg",
  "doctorNotes": "6時間おきに1錠服用してください。",
  "refillCount": 2,
  "pharmacy": {
    "name": "マツモト薬局 新宿店",
    "location": "東京都新宿区"
  }
}

```

### 💊 Collection: feedbacks（フィードバック）

```json
{
  "_id": "ObjectId('64def456789')",
  "patientId": 101,                           // MySQLのpatients.id
  "doctorId": 201,                            // MySQLのdoctors.id
  "appointmentId": 51,                        // MySQLのappointments.id
  "rating": 4,
  "comments": "医師の対応が丁寧でした。",
  "createdAt": "2025-06-01T10:00:00Z"
}

```

### 💊 Collection: logs（システムログ）

## 患者が自ら予約操作した場合のログ例
ログ内の `performedBy.userId` と `patientId` が同一で `role: "patient"` の場合、その予約操作は「患者本人によるもの」であると判断します。
```json
{
  "_id": "ObjectId('64fed321654')",
  "patientId": 101,                   // 対象：患者本人
  "performedBy": {
    "userId": 101,                    // 操作したユーザー（患者自身）
    "role": "patient"
  },
  "action": "予約を作成しました",
  "timestamp": "2025-06-01T09:30:00Z",
  "details": {
    "appointmentId": 51,
    "status": "Scheduled"
  }
}

```

## 管理者が患者の予約を代理作成した場合のログ例
```json
{
  "_id": "ObjectId('64fed321655')",
  "patientId": 101,                   // 対象：患者
  "performedBy": {
    "userId": 301,                    // 操作：管理者ID
    "role": "admin"
  },
  "action": "予約を代理作成しました",
  "timestamp": "2025-06-01T10:00:00Z",
  "details": {
    "appointmentId": 52,
    "status": "Scheduled"            //　※ logs.details.status は `appointments.status` と同じ ENUM('Scheduled', 'Completed', 'Cancelled') を使用。
  }
}
