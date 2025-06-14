// ==============================
// 各種関数のインポート
// ==============================

// 予約オーバーレイ（患者ログイン時）の表示用関数をインポート
import { showBookingOverlay } from "../loggedIn/loggedPatient.js";

// 医師を削除するAPI関数（管理者用）をインポート
import { deleteDoctor } from "../services/doctorServices.js";

// 予約時に必要な患者情報を取得する関数をインポート
import { fetchPatientDetails } from "../services/patientServices.js";

// ==============================
// 医師カードを生成する関数
// ==============================

/**
 * 医師の情報をもとに1つのカードUIを生成する関数
 *
 * @function createDoctorCard
 * @param {Object} doctor - 医師データ（name, specialization, email, availability, _idなどを含む）
 * @returns {HTMLElement} - 生成された医師カードDOM要素
 *
 * @description
 * ロールごとに以下の処理を実行：
 *
 * ## 🔐 ロールごとの処理概要
 * ---
 * ### 1. Admin
 * - 医師情報を表示
 * - [削除] ボタンを表示し、クリックで確認ダイアログ → 削除API呼び出し → カードを削除
 *
 * ### 2. Patient（未ログイン）
 * - 医師情報を表示
 * - [予約する] ボタンを表示し、クリックで「ログインしてください」とアラート表示
 *
 * ### 3. Logged-in Patient（ログイン済み）
 * - 医師情報を表示
 * - [予約する] ボタンを表示し、クリックでトークン検証 → 患者情報取得 → 予約オーバーレイを表示
 * 
 *  * この関数は、ユーザーのロールに応じて以下のようなHTML構造の要素を返す：
 *
 * 🚀[管理者（admin）の場合]
 * <div class="doctor-card">
 *   <div class="doctor-info">
 *     <h3>医師名: 山田 太郎</h3>
 *     <p>専門: 内科</p>
 *     <p>メール: yamada@example.com</p>
 *     <ul>
 *       <li>10:00〜11:00</li>
 *       <li>14:00〜15:00</li>
 *     </ul>
 *   </div>
 *   <div class="doctor-actions">
 *     <button class="delete-btn">削除</button>
 *   </div>
 * </div>
 *
 * 🚀[ログイン済み患者（loggedPatient）の場合]
 * <div class="doctor-card">
 *   <div class="doctor-info">
 *     <h3>医師名: 山田 太郎</h3>
 *     <p>専門: 内科</p>
 *     <p>メール: yamada@example.com</p>
 *     <ul>
 *       <li>10:00〜11:00</li>
 *       <li>14:00〜15:00</li>
 *     </ul>
 *   </div>
 *   <div class="doctor-actions">
 *     <button class="book-btn">予約する</button>
 *   </div>
 * </div>
 *
 * 🚀[未ログイン患者（patient）の場合]
 * <div class="doctor-card">
 *   <div class="doctor-info">
 *     <h3>医師名: 山田 太郎</h3>
 *     <p>専門: 内科</p>
 *     <p>メール: yamada@example.com</p>
 *     <ul>
 *       <li>10:00〜11:00</li>
 *       <li>14:00〜15:00</li>
 *     </ul>
 *   </div>
 *   <div class="doctor-actions">
 *     <button class="book-btn">予約する</button>
 *     <!-- このボタンはログインを促すアラートを出すだけ -->
 *   </div>
 * </div>
 */

export function createDoctorCard(doctor) {
  // メインコンテナ（カード全体）を作成
  const card = document.createElement("div");
  card.className = "doctor-card";

  // 現在のユーザーロールをlocalStorageから取得
  const role = localStorage.getItem("userRole");

  // 医師情報を表示するコンテナを作成
  const infoContainer = document.createElement("div");
  infoContainer.className = "doctor-info";

  // 医師の名前を表示
  const name = document.createElement("h3");
  name.textContent = `医師名: ${doctor.name}`;

  // 専門分野を表示
  const specialization = document.createElement("p");
  specialization.textContent = `専門: ${doctor.specialization}`;

  // メールアドレスを表示
  const email = document.createElement("p");
  email.textContent = `メール: ${doctor.email}`;

  // 空いている予約時間をリストで表示
  const availability = document.createElement("ul");
  availability.textContent = "空き時間:";
  doctor.availability.forEach(time => {
    const li = document.createElement("li");
    li.textContent = time;
    availability.appendChild(li);
  });

  // 情報をinfoContainerにまとめて追加
  infoContainer.appendChild(name);
  infoContainer.appendChild(specialization);
  infoContainer.appendChild(email);
  infoContainer.appendChild(availability);

  // アクションボタン用のコンテナを作成
  const actionsContainer = document.createElement("div");
  actionsContainer.className = "doctor-actions";

  // === 管理者用アクション ===
  if (role === "admin") {
    const deleteBtn = document.createElement("button");
    deleteBtn.textContent = "削除";
    deleteBtn.className = "delete-btn";

    deleteBtn.addEventListener("click", async () => {
      const token = localStorage.getItem("adminToken");
      if (!token) {
        alert("管理者トークンが見つかりません。ログインし直してください。");
        return;
      }

      const confirmed = confirm("本当にこの医師を削除しますか？");
      if (!confirmed) return;

      try {
        const result = await deleteDoctor(doctor._id, token);
        alert(result.message || "医師を削除しました。");
        card.remove(); // カードをDOMから削除
      } catch (err) {
        alert("削除に失敗しました。");
      }
    });

    actionsContainer.appendChild(deleteBtn);
  }

  // === 未ログイン患者のアクション ===
  else if (role === "patient") {
    const bookBtn = document.createElement("button");
    bookBtn.textContent = "予約する";
    bookBtn.className = "book-btn";
    bookBtn.addEventListener("click", () => {
      alert("予約にはログインが必要です。ログインしてください。");
    });
    actionsContainer.appendChild(bookBtn);
  }

  // === ログイン済み患者のアクション ===
  else if (role === "loggedPatient") {
    const bookBtn = document.createElement("button");
    bookBtn.textContent = "予約する";
    bookBtn.className = "book-btn";
    bookBtn.addEventListener("click", async () => {
      const token = localStorage.getItem("patientToken");
      if (!token) {
        alert("セッションが切れました。再ログインしてください。");
        window.location.href = "/"; // トップに戻す
        return;
      }

      try {
        const patient = await fetchPatientDetails(token);
        showBookingOverlay(doctor, patient); // 予約UIを表示
      } catch (err) {
        alert("患者情報の取得に失敗しました。");
      }
    });

    actionsContainer.appendChild(bookBtn);
  }

  // 情報とアクションをカードに追加
  card.appendChild(infoContainer);
  card.appendChild(actionsContainer);

  // 完成したカード要素を返す
  return card;
}







/*
Import the overlay function for booking appointments from loggedPatient.js

  Import the deleteDoctor API function to remove doctors (admin role) from docotrServices.js

  Import function to fetch patient details (used during booking) from patientServices.js

  Function to create and return a DOM element for a single doctor card
    Create the main container for the doctor card
    Retrieve the current user role from localStorage
    Create a div to hold doctor information
    Create and set the doctor’s name
    Create and set the doctor's specialization
    Create and set the doctor's email
    Create and list available appointment times
    Append all info elements to the doctor info container
    Create a container for card action buttons
    === ADMIN ROLE ACTIONS ===
      Create a delete button
      Add click handler for delete button
     Get the admin token from localStorage
        Call API to delete the doctor
        Show result and remove card if successful
      Add delete button to actions container
   
    === PATIENT (NOT LOGGED-IN) ROLE ACTIONS ===
      Create a book now button
      Alert patient to log in before booking
      Add button to actions container
  
    === LOGGED-IN PATIENT ROLE ACTIONS === 
      Create a book now button
      Handle booking logic for logged-in patient   
        Redirect if token not available
        Fetch patient data with token
        Show booking overlay UI with doctor and patient info
      Add button to actions container
   
  Append doctor info and action buttons to the car
  Return the complete doctor card element
*/
