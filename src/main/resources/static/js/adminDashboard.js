// DOM読み込み後の処理を設定
document.addEventListener('DOMContentLoaded', () => {
  // 医師一覧カードを読み込む
  loadDoctorCards();

  // 検索バーとフィルタのイベントを設定
  document.getElementById('searchBar').addEventListener('input', filterDoctorsOnChange);
  document.getElementById('timeFilter').addEventListener('change', filterDoctorsOnChange);
  document.getElementById('specialtyFilter').addEventListener('change', filterDoctorsOnChange);
});

// 「医師追加」ボタンがクリックされたらモーダルを開く
document.getElementById('addDoctorBtn').addEventListener('click', () => {
  openModal('addDoctor'); // モーダルID 'addDoctor' を開く
});

/**
 * 医師カードをすべて読み込む関数
 * getDoctors()でデータ取得し、各医師をカード化して表示
 */
async function loadDoctorCards() {
  try {
    const doctors = await getDoctors(); // サービス層から医師データ取得
    renderDoctorCards(doctors); // 医師一覧を描画
  } catch (error) {
    console.error('医師データの取得に失敗しました:', error);
  }
}

/**
 * フィルタや検索入力が変更されたときに呼ばれる関数
 * 医師リストを条件に応じて絞り込む
 */
async function filterDoctorsOnChange() {
  // 入力フィールドの値を取得
  const name = document.getElementById('searchBar').value.trim() || null;
  const time = document.getElementById('timeFilter').value || null;
  const specialty = document.getElementById('specialtyFilter').value || null;

  try {
    const doctors = await filterDoctors(name, time, specialty); // フィルタに応じて取得
    if (doctors.length > 0) {
      renderDoctorCards(doctors); // 医師カードを描画
    } else {
      // 医師が見つからなかった場合の処理
      const content = document.getElementById('doctorCardContainer');
      content.innerHTML = '<p>該当する医師は見つかりませんでした。</p>';
    }
  } catch (error) {
    alert('フィルタ処理中にエラーが発生しました');
    console.error('フィルタエラー:', error);
  }
}

/**
 * 医師カードのリストを描画するヘルパー関数
 * @param {Array} doctors 医師データ配列
 */
function renderDoctorCards(doctors) {
  const content = document.getElementById('doctorCardContainer');
  content.innerHTML = ''; // 一度すべてクリア
  doctors.forEach(doctor => {
    const card = createDoctorCard(doctor); // 医師カードを生成
    content.appendChild(card); // カードをコンテナに追加
  });
}

/**
 * 管理者が新しい医師を追加する処理
 * モーダルの入力値を取得して保存
 */
async function adminAddDoctor() {
  // フォーム入力値を取得
  const name = document.getElementById('doctorName').value;
  const email = document.getElementById('doctorEmail').value;
  const phone = document.getElementById('doctorPhone').value;
  const password = document.getElementById('doctorPassword').value;
  const specialty = document.getElementById('doctorSpecialty').value;
  const availableTimes = document.getElementById('doctorAvailableTimes').value;
  
  //あとで追加🔥🔥🔥🔥
  //clinic
  //full_name
  //role

  const token = localStorage.getItem('token'); // 認証トークンを取得

  if (!token) {
    alert('ログインが必要です');
    return;
  }

  // 医師オブジェクトを構築
  const doctor = {
    name,
    email,
    phone,
    password,
    specialty,
    availableTimes

  //あとで追加🔥🔥🔥🔥
  //clinic
  //full_name
  //role

  };

  try {
    const result = await saveDoctor(doctor, token); // 医師を保存
    if (result.success) {
      alert('医師が正常に追加されました');
      closeModal('addDoctor'); // モーダルを閉じる
      window.location.reload(); // ページをリロード
    } else {
      alert(`保存に失敗しました: ${result.message}`);
    }
  } catch (error) {
    alert('医師の追加中にエラーが発生しました');
    console.error('保存エラー:', error);
  }
}









/*
  This script handles the admin dashboard functionality for managing doctors:
  - Loads all doctor cards
  - Filters doctors by name, time, or specialty
  - Adds a new doctor via modal form


  Attach a click listener to the "Add Doctor" button
  When clicked, it opens a modal form using openModal('addDoctor')


  When the DOM is fully loaded:
    - Call loadDoctorCards() to fetch and display all doctors


  Function: loadDoctorCards
  Purpose: Fetch all doctors and display them as cards

    Call getDoctors() from the service layer
    Clear the current content area
    For each doctor returned:
    - Create a doctor card using createDoctorCard()
    - Append it to the content div

    Handle any fetch errors by logging them


  Attach 'input' and 'change' event listeners to the search bar and filter dropdowns
  On any input change, call filterDoctorsOnChange()


  Function: filterDoctorsOnChange
  Purpose: Filter doctors based on name, available time, and specialty

    Read values from the search bar and filters
    Normalize empty values to null
    Call filterDoctors(name, time, specialty) from the service

    If doctors are found:
    - Render them using createDoctorCard()
    If no doctors match the filter:
    - Show a message: "No doctors found with the given filters."

    Catch and display any errors with an alert


  Function: renderDoctorCards
  Purpose: A helper function to render a list of doctors passed to it

    Clear the content area
    Loop through the doctors and append each card to the content area


  Function: adminAddDoctor
  Purpose: Collect form data and add a new doctor to the system

    Collect input values from the modal form
    - Includes name, email, phone, password, specialty, and available times

    Retrieve the authentication token from localStorage
    - If no token is found, show an alert and stop execution

    Build a doctor object with the form values

    Call saveDoctor(doctor, token) from the service

    If save is successful:
    - Show a success message
    - Close the modal and reload the page

    If saving fails, show an error message
*/
