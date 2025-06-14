/**
 * renderHeader - ヘッダーを動的に表示する関数
 *
 * ユーザーのログイン状態やロール情報に基づき、
 * ページのヘッダー部分のHTMLを動的に生成・表示します。
 * ログインしていない場合は簡易ヘッダーを表示し、
 * ログイン状態に応じて各種ボタンを表示・イベント設定を行います。
 */
function renderHeader() {
  // HTML内のid="header"を持つ要素を取得
  const headerDiv = document.getElementById("header");

  //---------------------------------------------------
  // 非ログイン状態（または初期状態）のヘッダーに関する処理
  //---------------------------------------------------

  /**
   * 現在のページのパスがルート("/")かどうか確認。
   * ルートの場合はロール情報を削除してシンプルなヘッダーを描画。
   */
  if (window.location.pathname.endsWith("/")) {
    localStorage.removeItem("userRole"); // セッション初期化
    
    headerDiv.innerHTML = `
      <header class="header">
        <div class="logo-section">
          <img src="../assets/images/logo/logo.png" alt="Hospital CRM Logo" class="logo-img">
          <span class="logo-title">Hospital CMS</span>
        </div>
      </header>`;
    return; // 関数終了
  }

  //-----------------------------------
  // ログイン状態のヘッダーに関する処理
  //-----------------------------------

  // ローカルストレージからユーザーのロールとトークンを取得
  const role = localStorage.getItem("userRole");
  const token = localStorage.getItem("token");

  // ヘッダーの基本HTML（ロゴ部分）
  let headerContent = `<header class="header">
    <div class="logo-section">
      <img src="../assets/images/logo/logo.png" alt="Hospital CRM Logo" class="logo-img">
      <span class="logo-title">Hospital CMS</span>
    </div>
    <nav>`;

  /**
   * ロールは存在するがトークンが無い場合はセッション切れと判断し、
   * ログアウト処理を行ってトップページへリダイレクト。
   */
  if ((role === "loggedPatient" || role === "admin" || role === "doctor") && !token) {
    localStorage.removeItem("userRole");
    alert("セッションの有効期限が切れたか、無効なログインです。再度ログインしてください。");
    window.location.href = "/";
    return;
  }

  /**
   * ロールに応じてヘッダー内に表示するボタンなどを動的に追加。
   * - admin: 「Add Doctor」ボタンとログアウトリンク
   * - doctor: 「Home」ボタンとログアウトリンク
   * - patient: 「Login」「Sign Up」ボタン
   * - loggedPatient: 「Home」「Appointments」「Logout」ボタン
   */
  if (role === "admin") {
    headerContent += `
      <button id="addDocBtn" class="adminBtn" onclick="openModal('addDoctor')">Add Doctor</button>
      <a href="#" onclick="logout()">Logout</a>`;
  } else if (role === "doctor") {
    headerContent += `
      <button class="adminBtn" onclick="selectRole('doctor')">Home</button>
      <a href="#" onclick="logout()">Logout</a>`;
  } else if (role === "patient") {
    headerContent += `
      <button id="patientLogin" class="adminBtn">Login</button>
      <button id="patientSignup" class="adminBtn">Sign Up</button>`;
  } else if (role === "loggedPatient") {
    headerContent += `
      <button id="home" class="adminBtn" onclick="window.location.href='/pages/loggedPatientDashboard.html'">Home</button>
      <button id="patientAppointments" class="adminBtn" onclick="window.location.href='/pages/patientAppointments.html'">Appointments</button>
      <a href="#" onclick="logoutPatient()">Logout</a>`;
  }

  // ナビゲーション終了タグとヘッダー閉じタグを追加
  headerContent += `</nav></header>`;

  // 動的に作成したヘッダーHTMLをDOMに挿入
  headerDiv.innerHTML = headerContent;

  // 各ボタンにイベントリスナーを設定する補助関数を呼び出し
  attachHeaderButtonListeners();
}


/**
 * attachHeaderButtonListeners - ヘッダーボタンのイベントリスナーを設定
 *
 * ログイン・サインアップボタンが存在する場合、
 * クリック時に対応するモーダルを開くイベントを設定します。
 */
function attachHeaderButtonListeners() {
  const patientLoginBtn = document.getElementById("patientLogin");
  const patientSignupBtn = document.getElementById("patientSignup");

  if (patientLoginBtn) {
    patientLoginBtn.addEventListener("click", () => {
      openModal("patientLogin");
    });
  }

  if (patientSignupBtn) {
    patientSignupBtn.addEventListener("click", () => {
      openModal("patientSignup");
    });
  }
}

/**
 * logout - ログアウト処理
 *
 * ユーザーのロールとトークンをlocalStorageから削除し、
 * トップページにリダイレクトします。
 */
function logout() {
  localStorage.removeItem("userRole");
  localStorage.removeItem("token");
  window.location.href = "/";
}

/**
 * logoutPatient - ログイン済み患者のログアウト処理
 *
 * ログアウト後に患者用ダッシュボードへリダイレクトします。
 */
function logoutPatient() {
  localStorage.removeItem("userRole");
  localStorage.removeItem("token");
  window.location.href = "/";
}

// ページ読み込み時にヘッダー初期化を実行
renderHeader();









/*
  Step-by-Step Explanation of Header Section Rendering

  This code dynamically renders the header section of the page based on the user's role, session status, and available actions (such as login, logout, or role-switching).

  1. Define the `renderHeader` Function

     * The `renderHeader` function is responsible for rendering the entire header based on the user's session, role, and whether they are logged in.

  2. Select the Header Div

     * The `headerDiv` variable retrieves the HTML element with the ID `header`, where the header content will be inserted.
       ```javascript
       const headerDiv = document.getElementById("header");
       ```

  3. Check if the Current Page is the Root Page

     * The `window.location.pathname` is checked to see if the current page is the root (`/`). If true, the user's session data (role) is removed from `localStorage`, and the header is rendered without any user-specific elements (just the logo and site title).
       ```javascript
       if (window.location.pathname.endsWith("/")) {
         localStorage.removeItem("userRole");
         headerDiv.innerHTML = `
           <header class="header">
             <div class="logo-section">
               <img src="../assets/images/logo/logo.png" alt="Hospital CRM Logo" class="logo-img">
               <span class="logo-title">Hospital CMS</span>
             </div>
           </header>`;
         return;
       }
       ```

  4. Retrieve the User's Role and Token from LocalStorage

     * The `role` (user role like admin, patient, doctor) and `token` (authentication token) are retrieved from `localStorage` to determine the user's current session.
       ```javascript
       const role = localStorage.getItem("userRole");
       const token = localStorage.getItem("token");
       ```

  5. Initialize Header Content

     * The `headerContent` variable is initialized with basic header HTML (logo section), to which additional elements will be added based on the user's role.
       ```javascript
       let headerContent = `<header class="header">
         <div class="logo-section">
           <img src="../assets/images/logo/logo.png" alt="Hospital CRM Logo" class="logo-img">
           <span class="logo-title">Hospital CMS</span>
         </div>
         <nav>`;
       ```

  6. Handle Session Expiry or Invalid Login

     * If a user with a role like `loggedPatient`, `admin`, or `doctor` does not have a valid `token`, the session is considered expired or invalid. The user is logged out, and a message is shown.
       ```javascript
       if ((role === "loggedPatient" || role === "admin" || role === "doctor") && !token) {
         localStorage.removeItem("userRole");
         alert("Session expired or invalid login. Please log in again.");
         window.location.href = "/";   or a specific login page
         return;
       }
       ```

  7. Add Role-Specific Header Content

     * Depending on the user's role, different actions or buttons are rendered in the header:
       - **Admin**: Can add a doctor and log out.
       - **Doctor**: Has a home button and log out.
       - **Patient**: Shows login and signup buttons.
       - **LoggedPatient**: Has home, appointments, and logout options.
       ```javascript
       else if (role === "admin") {
         headerContent += `
           <button id="addDocBtn" class="adminBtn" onclick="openModal('addDoctor')">Add Doctor</button>
           <a href="#" onclick="logout()">Logout</a>`;
       } else if (role === "doctor") {
         headerContent += `
           <button class="adminBtn"  onclick="selectRole('doctor')">Home</button>
           <a href="#" onclick="logout()">Logout</a>`;
       } else if (role === "patient") {
         headerContent += `
           <button id="patientLogin" class="adminBtn">Login</button>
           <button id="patientSignup" class="adminBtn">Sign Up</button>`;
       } else if (role === "loggedPatient") {
         headerContent += `
           <button id="home" class="adminBtn" onclick="window.location.href='/pages/loggedPatientDashboard.html'">Home</button>
           <button id="patientAppointments" class="adminBtn" onclick="window.location.href='/pages/patientAppointments.html'">Appointments</button>
           <a href="#" onclick="logoutPatient()">Logout</a>`;
       }
       ```



  9. Close the Header Section



  10. Render the Header Content

     * Insert the dynamically generated `headerContent` into the `headerDiv` element.
       ```javascript
       headerDiv.innerHTML = headerContent;
       ```

  11. Attach Event Listeners to Header Buttons

     * Call `attachHeaderButtonListeners` to add event listeners to any dynamically created buttons in the header (e.g., login, logout, home).
       ```javascript
       attachHeaderButtonListeners();
       ```


  ### Helper Functions

  13. **attachHeaderButtonListeners**: Adds event listeners to login buttons for "Doctor" and "Admin" roles. If clicked, it opens the respective login modal.

  14. **logout**: Removes user session data and redirects the user to the root page.

  15. **logoutPatient**: Removes the patient's session token and redirects to the patient dashboard.

  16. **Render the Header**: Finally, the `renderHeader()` function is called to initialize the header rendering process when the page loads.
*/
   
