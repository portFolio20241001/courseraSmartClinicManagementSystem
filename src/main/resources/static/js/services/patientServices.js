// 設定ファイルからベースAPI URLをインポート
import { API_BASE_URL } from './config.js';

// 患者用のAPIエンドポイントを定数として定義
const PATIENT_API = `${API_BASE_URL}/patient`;

/**
 * 新規患者を登録する関数
 * @param {Object} data 患者の登録情報（名前、メールアドレス、パスワードなど）
 * @returns {Promise<Object>} 成功・失敗の情報とメッセージ
 */
export async function patientSignup(data) {
  try {
    // POSTリクエストを送信（ヘッダーでJSON形式を指定し、データをJSON文字列に変換して送信）
    const response = await fetch(PATIENT_API, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(data),
    });

    const result = await response.json(); // レスポンスをJSONに変換

    if (!response.ok) {
      // エラーがあれば例外を投げる
      throw new Error(result.message || '登録に失敗しました');
    }

    return {
      success: true,
      message: result.message,
    };
  } catch (error) {
    console.error('患者登録エラー:', error);
    return {
      success: false,
      message: error.message || '不明なエラーが発生しました',
    };
  }
}

/**
 * 患者のログイン認証を行う関数
 * @param {Object} data ログイン情報（emailとpassword）
 * @returns {Promise<Response>} fetchの生のレスポンス（呼び出し元で処理）
 */
export async function patientLogin(data) {
  try {
    // `/login`エンドポイントにPOSTリクエストを送信
    return await fetch(`${PATIENT_API}/login`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(data),
    });
  } catch (error) {
    console.error('ログインリクエストに失敗しました:', error);
    throw error;
  }
}

/**
 * トークンを使って患者の基本情報を取得する関数
 * @param {string} token 認証トークン
 * @returns {Promise<Object|null>} 患者情報 または null（失敗時）
 */
export async function getPatientData(token) {
  try {
    const response = await fetch(`${PATIENT_API}/${token}`);
    if (response.ok) {
      const result = await response.json();
      return result.patient; // 患者情報を返す
    }
    console.warn('患者情報の取得に失敗しました');
    return null;
  } catch (error) {
    console.error('患者情報取得中のエラー:', error);
    return null;
  }
}

/**
 * 医師または患者の予約情報を取得する関数
 * @param {string} id ユーザーID
 * @param {string} user 'doctor' または 'patient'
 * @param {string} token 認証トークン
 * @returns {Promise<Array|null>} 予約データの配列 または null（失敗時）
 */
export async function getPatientAppointments(id, user, token) {
  try {
    const response = await fetch(`${PATIENT_API}/${id}/${user}/${token}`);
    if (response.ok) {
      const result = await response.json();
      return result.appointments; // 予約データを返す
    }
    console.warn('予約データの取得に失敗しました');
    return null;
  } catch (error) {
    console.error('予約取得エラー:', error);
    return null;
  }
}

/**
 * 状態や名前に基づいて予約をフィルタリングする関数
 * @param {string} condition 状態（例：'approved', 'pending'など）
 * @param {string} name 患者の名前
 * @param {string} token 認証トークン
 * @returns {Promise<Object>} フィルタリングされた予約データ
 */
export async function filterAppointments(condition, name, token) {
  try {
    const response = await fetch(`${PATIENT_API}/filter/${condition}/${name}/${token}`);
    if (response.ok) {
      const result = await response.json();
      return result; // フィルタされた予約情報を返す
    } else {
      console.warn('フィルタ処理に失敗しました');
      return { appointments: [] };
    }
  } catch (error) {
    alert('予約のフィルタ中にエラーが発生しました');
    console.error('フィルタエラー:', error);
    return { appointments: [] };
  }
}










/*
  Import the base API URL from the config file
  Create a constant named PATIENT_API by appending '/patient' to the base URL


  Function  patientSignup
  Purpose  Register a new patient in the system

     Send a POST request to PATIENT_API with 
    - Headers  Content-Type set to 'application/json'
    - Body  JSON.stringify(data) where data includes patient details

    Convert the response to JSON and check for success
    - If response is not OK, throw an error with the message from the server

    Return an object with 
    - success  true or false
    - message  feedback from the server

    Use try-catch to handle network or API errors
    - Log errors and return a failure response with the error message


  Function  patientLogin
  Purpose  Authenticate a patient with email and password

     Send a POST request to `${PATIENT_API}/login`
    - Include appropriate headers and the login data in JSON format

    Return the raw fetch response to be handled where the function is called
    - The caller will check the response status and process the token or error


  Function  getPatientData
  Purpose  Fetch basic patient information using a token

     Send a GET request to `${PATIENT_API}/${token}`
    Parse the response and return the 'patient' object if response is OK
    If there's an error or the response is not OK, return null
    Catch and log any network or server errors


  Function  getPatientAppointments
  Purpose  Retrieve appointment data for a specific user (doctor or patient)

     Send a GET request to `${PATIENT_API}/${id}/${user}/${token}`
    - 'id' is the user’s ID, 'user' is either 'doctor' or 'patient', and 'token' is for auth

    Parse the response and return the 'appointments' array if successful
    If the response fails or an error occurs, return null
    Log any errors for debugging


  Function  filterAppointments
  Purpose  Retrieve filtered appointments based on condition and patient name

   Send a GET request to `${PATIENT_API}/filter/${condition}/${name}/${token}`
    - This allows filtering based on status or search criteria

   Parse the response if it's OK and return the data
   If the response fails, return an empty appointments array
   Use a try-catch to handle errors gracefully and notify the user
*/
