// 各種コンポーネントやサービスをインポート
import { createDoctorCard } from './components/doctorCard.js'; // 医師カード生成用コンポーネント
import { openModal } from './components/modals.js'; // モーダルウィンドウを開く関数
import { filterDoctors, getDoctors } from './services/doctorServices.js'; // 医師情報の取得・フィルタリングサービス
import { patientLogin, patientSignup } from './services/patientServices.js'; // 患者のログイン・サインアップサービス

// ページ読み込み完了時に医師カードを読み込む
document.addEventListener("DOMContentLoaded", () => {
  loadDoctorCards(); // 医師カードをロード
});

// サインアップボタンが押されたらモーダルを表示
document.addEventListener("DOMContentLoaded", () => {
  const btn = document.getElementById("patientSignup"); // サインアップボタン取得
  if (btn) {
    btn.addEventListener("click", () => openModal("patientSignup")); // クリックでサインアップモーダルを開く
  }
});

// ログインボタンが押されたらモーダルを表示
document.addEventListener("DOMContentLoaded", ()=> {
  const loginBtn = document.getElementById("patientLogin"); // ログインボタン取得
  if(loginBtn){
    loginBtn.addEventListener("click" , ()=> {
      openModal("patientLogin"); // クリックでログインモーダルを開く
    });
  }
});

// 医師カードを取得・表示する関数
function loadDoctorCards() {
  getDoctors() // 医師情報を取得
    .then(doctors => {
      const contentDiv = document.getElementById("content"); // 表示先の要素取得
      contentDiv.innerHTML = ""; // 既存の内容をクリア

      // 各医師情報に対してカードを作成して表示
      doctors.forEach(doctor => {
        const card = createDoctorCard(doctor); // カード生成
        contentDiv.appendChild(card); // DOMに追加
      });
    })
    .catch(error => {
      console.error("Failed to load doctors:", error); // エラー出力
    });
}

// 検索・フィルター用のイベントリスナーを設定
document.getElementById("searchBar").addEventListener("input", filterDoctorsOnChange); // 名前検索
document.getElementById("timeFilter").addEventListener("change", filterDoctorsOnChange); // 時間帯フィルター
document.getElementById("specialtyFilter").addEventListener("change", filterDoctorsOnChange); // 専門分野フィルター

// 医師をフィルターして表示を更新する関数
function filterDoctorsOnChange() {
  const searchBar = document.getElementById("searchBar").value.trim(); // 名前検索値取得
  const timeFilter = document.getElementById("timeFilter").value; // 時間帯フィルター値取得
  const specialtyFilter = document.getElementById("specialtyFilter").value; // 専門分野フィルター値取得

  // 空文字列の場合はnullとして扱う（フィルタ条件から除外）
  const name = searchBar.length > 0 ? searchBar : null;
  const time = timeFilter.length > 0 ? timeFilter : null;
  const specialty = specialtyFilter.length > 0 ? specialtyFilter : null;

  // フィルタリング実行
  filterDoctors(name , time ,specialty)
    .then(response => {
      const doctors = response.doctors; // 医師データ取得
      const contentDiv = document.getElementById("content");
      contentDiv.innerHTML = ""; // 表示内容を初期化

      if (doctors.length > 0) {
        console.log(doctors); // ログ出力
        doctors.forEach(doctor => {
          const card = createDoctorCard(doctor); // 各医師カードを作成
          contentDiv.appendChild(card); // 表示に追加
        });
      } else {
        // 該当なしの場合メッセージ表示
        contentDiv.innerHTML = "<p>No doctors found with the given filters.</p>";
        console.log("Nothing");
      }
    })
    .catch(error => {
      // エラー処理
      console.error("Failed to filter doctors:", error);
      alert("❌ An error occurred while filtering doctors."); // アラート表示
    });
}

// 外部から使用される医師カード描画関数
export function renderDoctorCards(doctors) {
  const contentDiv = document.getElementById("content"); // 表示先要素
  contentDiv.innerHTML = ""; // 初期化

  // 各医師に対してカードを作成し表示
  doctors.forEach(doctor => {
    const card = createDoctorCard(doctor);
    contentDiv.appendChild(card);
  });
}

// 登録処理関数　グローバル関数として定義（HTML側から呼び出される）
window.signupPatient = async function () {
  try {
    // 入力フィールドからデータ取得
    const name = document.getElementById("name").value;
    const email = document.getElementById("email").value;
    const password = document.getElementById("password").value;
    const phone = document.getElementById("phone").value;
    const address = document.getElementById("address").value;

    const data = { name, email, password, phone, address }; // オブジェクトにまとめる
    const { success, message } = await patientSignup(data); // サインアップ処理

    if(success){
      alert(message); // 成功時アラート表示
      document.getElementById("modal").style.display = "none"; // モーダルを閉じる
      window.location.reload(); // ページ再読み込み
    }
    else alert(message); // エラー表示
  } catch (error) {
    console.error("Signup failed:", error); // エラーログ出力
    alert("❌ An error occurred while signing up."); // アラート表示
  }
};

// ログイン処理関数（グローバル関数として定義（HTML側から呼び出される））
window.loginPatient = async function(){
  try {
    // 入力値を取得
    const email = document.getElementById("email").value;
    const password = document.getElementById("password").value;

    const data = { email, password }; // データをまとめる
    console.log("loginPatient :: ", data); // ログ出力

    const response = await patientLogin(data); // ログイン処理呼び出し

    console.log("Status Code:", response.status); // ステータス表示
    console.log("Response OK:", response.ok); // 成功判定表示

    if (response.ok) {
      const result = await response.json(); // レスポンスからJSON取得
      console.log(result); // レスポンス表示
      selectRole('loggedPatient'); // ロール設定
      localStorage.setItem('token', result.token ); // トークンを保存
      window.location.href = '/pages/loggedPatientDashboard.html'; // ダッシュボードへ遷移
    } else {
      alert('❌ Invalid credentials!'); // ログイン失敗時のアラート
    }
  }
  catch(error) {
    alert("❌ Failed to Login : ", error); // エラーメッセージ表示
    console.log("Error :: loginPatient :: ", error); // エラーログ出力
  }
};
