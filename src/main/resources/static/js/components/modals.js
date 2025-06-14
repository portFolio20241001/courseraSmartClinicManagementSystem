// modals.js

// モーダルを開く関数。引数 type に応じて表示内容を切り替える
export function openModal(type) {
  let modalContent = '';

  // 医師追加フォーム（管理者用）
  if (type === 'addDoctor') {
    modalContent = `
         <h2>医師の追加</h2>
         <input type="text" id="doctorName" placeholder="医師の名前" class="input-field">
         <select id="specialization" class="input-field select-dropdown">
             <option value="">専門分野を選択</option>
             <option value="cardiologist">循環器科</option>
             <option value="dermatologist">皮膚科</option>
             <option value="neurologist">神経内科</option>
             <option value="pediatrician">小児科</option>
             <option value="orthopedic">整形外科</option>
             <option value="gynecologist">婦人科</option>
             <option value="psychiatrist">精神科</option>
             <option value="dentist">歯科</option>
             <option value="ophthalmologist">眼科</option>
             <option value="ent">耳鼻科</option>
             <option value="urologist">泌尿器科</option>
             <option value="oncologist">腫瘍内科</option>
             <option value="gastroenterologist">消化器内科</option>
             <option value="general">内科</option>
        </select>
        <input type="email" id="doctorEmail" placeholder="メールアドレス" class="input-field">
        <input type="password" id="doctorPassword" placeholder="パスワード" class="input-field">
        <input type="text" id="doctorPhone" placeholder="電話番号" class="input-field">
        <div class="availability-container">
          <label class="availabilityLabel">診察可能な時間帯を選択:</label>
          <div class="checkbox-group">
              <label><input type="checkbox" name="availability" value="09:00-10:00"> 9:00〜10:00</label>
              <label><input type="checkbox" name="availability" value="10:00-11:00"> 10:00〜11:00</label>
              <label><input type="checkbox" name="availability" value="11:00-12:00"> 11:00〜12:00</label>
              <label><input type="checkbox" name="availability" value="12:00-13:00"> 12:00〜13:00</label>
          </div>
        </div>
        <button class="dashboard-btn" id="saveDoctorBtn">登録する</button>
      `;
  }

  // 患者ログインフォーム
  else if (type === 'patientLogin') {
    modalContent = `
        <h2>患者ログイン</h2>
        <input type="text" id="email" placeholder="メールアドレス" class="input-field">
        <input type="password" id="password" placeholder="パスワード" class="input-field">
        <button class="dashboard-btn" id="loginBtn">ログイン</button>
      `;
  }

  // 患者新規登録フォーム
  else if (type === "patientSignup") {
    modalContent = `
      <h2>患者登録</h2>
      <input type="text" id="name" placeholder="お名前" class="input-field">
      <input type="email" id="email" placeholder="メールアドレス" class="input-field">
      <input type="password" id="password" placeholder="パスワード" class="input-field">
      <input type="text" id="phone" placeholder="電話番号" class="input-field">
      <input type="text" id="address" placeholder="住所" class="input-field">
      <button class="dashboard-btn" id="signupBtn">登録</button>
    `;
  }

  // 管理者ログインフォーム
  else if (type === 'adminLogin') {
    modalContent = `
        <h2>管理者ログイン</h2>
        <input type="text" id="username" name="username" placeholder="ユーザー名" class="input-field">
        <input type="password" id="password" name="password" placeholder="パスワード" class="input-field">
        <button class="dashboard-btn" id="adminLoginBtn">ログイン</button>
      `;
  }

  // 医師ログインフォーム
  else if (type === 'doctorLogin') {
    modalContent = `
        <h2>医師ログイン</h2>
        <input type="text" id="email" placeholder="メールアドレス" class="input-field">
        <input type="password" id="password" placeholder="パスワード" class="input-field">
        <button class="dashboard-btn" id="doctorLoginBtn">ログイン</button>
      `;
  }

  // モーダルの中身を設定
  document.getElementById('modal-body').innerHTML = modalContent;

  // モーダルを表示（ポップアップ表示）
  document.getElementById('modal').style.display = 'block';

  // 閉じるボタンをクリックしたときの動作（非表示にする）
  document.getElementById('closeModal').onclick = () => {
    document.getElementById('modal').style.display = 'none';
  };

  // イベント登録：モーダルの種類によって処理を振り分け
  if (type === "patientSignup") {
    document.getElementById("signupBtn").addEventListener("click", signupPatient);
  }

  if (type === "patientLogin") {
    document.getElementById("loginBtn").addEventListener("click", loginPatient);
  }

  if (type === 'addDoctor') {
    document.getElementById('saveDoctorBtn').addEventListener('click', adminAddDoctor);
  }

  if (type === 'adminLogin') {
    document.getElementById('adminLoginBtn').addEventListener('click', adminLoginHandler);
  }

  if (type === 'doctorLogin') {
    document.getElementById('doctorLoginBtn').addEventListener('click', doctorLoginHandler);
  }
}
