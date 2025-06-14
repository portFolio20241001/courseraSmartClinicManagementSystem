// 必要な関数をインポート（仮定：他のモジュールから）
import { getAllAppointments } from './services/appointmentService.js';
import { createPatientRow } from './components/patientRow.js';

// 予約テーブルの tbody 要素を取得
const tableBody = document.getElementById('patientTableBody');

// 本日の日付を 'YYYY-MM-DD' 形式で取得して初期化
let selectedDate = new Date().toISOString().split('T')[0];

// ローカルストレージからトークンを取得（認証に使用）
const token = localStorage.getItem('token');

// 患者名によるフィルタリング用の変数（初期値は null）
let patientName = null;

// 検索バーに 'input' イベントリスナーを追加
document.getElementById('searchBar').addEventListener('input', (e) => {
  const value = e.target.value.trim();
  patientName = value !== '' ? value : null; // 空なら null をセット
  loadAppointments(); // フィルタを適用して再読み込み
});

// 「今日」ボタンにクリックイベントを追加
document.getElementById('todayButton').addEventListener('click', () => {
  selectedDate = new Date().toISOString().split('T')[0]; // 今日の日付に設定
  document.getElementById('datePicker').value = selectedDate; // 日付ピッカーUI更新
  loadAppointments(); // 今日の予約を再読み込み
});

// 日付ピッカーの変更イベントを監視
document.getElementById('datePicker').addEventListener('change', (e) => {
  selectedDate = e.target.value; // 新しい日付に更新
  loadAppointments(); // 該当日の予約を再読み込み
});

/**
 * 予約情報を取得して表示する関数
 * 日付と（オプションで）患者名によってフィルタされる
 */
async function loadAppointments() {
  try {
    // ステップ1: APIから予約データを取得
    const appointments = await getAllAppointments(selectedDate, patientName, token);

    // ステップ2: 表の内容を一度クリア
    tableBody.innerHTML = '';

    // ステップ3: 予約がない場合の表示
    if (!appointments || appointments.length === 0) {
      const row = document.createElement('tr');
      const cell = document.createElement('td');
      cell.colSpan = 4;
      cell.textContent = '本日の予約は見つかりませんでした。';
      row.appendChild(cell);
      tableBody.appendChild(row);
      return;
    }

    // ステップ4: 予約がある場合の処理
    appointments.forEach(app => {
      // 必要な情報を含む患者オブジェクトを作成
      const patient = {
        id: app.id,
        name: app.name,   // 後ほど修正🔥🔥🔥🔥
        phone: app.phone,
        email: app.email
      };

      // 患者情報からテーブル行を作成
      const row = createPatientRow(patient);
      tableBody.appendChild(row); // 行をテーブルに追加
    });
  } catch (error) {
    console.error('予約情報の取得エラー:', error);

    // ステップ5: エラー時の表示
    const row = document.createElement('tr');
    const cell = document.createElement('td');
    cell.colSpan = 4;
    cell.textContent = '予約情報の読み込みに失敗しました。後でもう一度お試しください。';
    row.appendChild(cell);
    tableBody.appendChild(row);
  }
}

// ページが完全に読み込まれたら初期処理を実行
document.addEventListener('DOMContentLoaded', () => {
  renderContent(); // UIレイアウトの初期化（外部関数と仮定）
  loadAppointments(); // 初期状態で本日の予約を表示
});








/*
  Import getAllAppointments to fetch appointments from the backend
  Import createPatientRow to generate a table row for each patient appointment


  Get the table body where patient rows will be added
  Initialize selectedDate with today's date in 'YYYY-MM-DD' format
  Get the saved token from localStorage (used for authenticated API calls)
  Initialize patientName to null (used for filtering by name)


  Add an 'input' event listener to the search bar
  On each keystroke:
    - Trim and check the input value
    - If not empty, use it as the patientName for filtering
    - Else, reset patientName to "null" (as expected by backend)
    - Reload the appointments list with the updated filter


  Add a click listener to the "Today" button
  When clicked:
    - Set selectedDate to today's date
    - Update the date picker UI to match
    - Reload the appointments for today


  Add a change event listener to the date picker
  When the date changes:
    - Update selectedDate with the new value
    - Reload the appointments for that specific date


  Function: loadAppointments
  Purpose: Fetch and display appointments based on selected date and optional patient name

  Step 1: Call getAllAppointments with selectedDate, patientName, and token
  Step 2: Clear the table body content before rendering new rows

  Step 3: If no appointments are returned:
    - Display a message row: "No Appointments found for today."

  Step 4: If appointments exist:
    - Loop through each appointment and construct a 'patient' object with id, name, phone, and email
    - Call createPatientRow to generate a table row for the appointment
    - Append each row to the table body

  Step 5: Catch and handle any errors during fetch:
    - Show a message row: "Error loading appointments. Try again later."


  When the page is fully loaded (DOMContentLoaded):
    - Call renderContent() (assumes it sets up the UI layout)
    - Call loadAppointments() to display today's appointments by default
*/
