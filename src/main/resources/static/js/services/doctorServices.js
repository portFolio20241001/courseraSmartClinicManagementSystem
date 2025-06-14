// 設定ファイルからベースURLをインポート
import { API_BASE_URL } from './config.js';

// 医師関連のAPIエンドポイントを定数として定義
const DOCTOR_API = `${API_BASE_URL}/doctors`;

/**
 * 医師一覧を取得する関数
 * @returns {Promise<Array>} 医師オブジェクトの配列
 */
export async function getDoctors() {
  try {

    // APIにGETリクエストを送信
    const response = await fetch(DOCTOR_API);
    const data = await response.json(); // レスポンスをJSONに変換
    return data.doctors; // doctors配列を返す

  } catch (error) {

    console.error('医師の取得に失敗しました:', error);
    return []; // エラー時は空の配列を返す

  }
}

/**
 * 指定した医師を削除する関数
 * @param {string} id 医師のID
 * @param {string} token 認証トークン
 * @returns {Promise<Object>} 削除結果（成功かどうか、メッセージ）
 */
export async function deleteDoctor(id, token) {
  try {
    // DELETEリクエストを送信（URLにIDとトークンを含める）
    const response = await fetch(`${DOCTOR_API}/delete/${id}/${token}`, {
      method: 'DELETE',
    });
    const data = await response.json(); // レスポンスをJSONに変換
    return {
      success: data.success,
      message: data.message,
    };
  } catch (error) {
    console.error('医師の削除に失敗しました:', error);
    return {
      success: false,
      message: '削除に失敗しました',
    };
  }
}

/**
 * 新しい医師を保存（登録）する関数
 * @param {Object} doctor 医師データ
 * @param {string} token 認証トークン
 * @returns {Promise<Object>} 保存結果（成功かどうか、メッセージ）
 */
export async function saveDoctor(doctor, token) {
  try {
    // POSTリクエストを送信（ヘッダーでJSON形式を指定、ボディにdoctorデータを含める）
    const response = await fetch(`${DOCTOR_API}/add/${token}`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(doctor),
    });
    const data = await response.json(); // レスポンスをJSONに変換
    return {
      success: data.success,
      message: data.message,
    };
  } catch (error) {
    console.error('医師の保存に失敗しました:', error);
    return {
      success: false,
      message: '医師の保存に失敗しました',
    };
  }
}

/**
 * フィルター条件に一致する医師を取得する関数
 * @param {string} name 医師の名前
 * @param {string} time 利用可能時間
 * @param {string} specialty 専門分野
 * @returns {Promise<Object>} 医師データを含むオブジェクト
 */
export async function filterDoctors(name, time, specialty) {
  try {
    // パラメータをURLに含めてGETリクエストを送信
    const response = await fetch(`${DOCTOR_API}/filter/${name}/${time}/${specialty}`);
    if (response.ok) {
      const data = await response.json(); // レスポンスをJSONに変換
      return data; // 医師データを返す
    } else {
      console.error('フィルタ結果の取得に失敗しました:', response.statusText);
      return { doctors: [] }; // エラー時は空の結果を返す
    }
  } catch (error) {
    alert('医師のフィルタ中にエラーが発生しました');
    console.error('フィルタ処理エラー:', error);
    return { doctors: [] }; // その他のエラー時にも空の結果を返す
  }
}







/*
  Import the base API URL from the config file
  Define a constant DOCTOR_API to hold the full endpoint for doctor-related actions


  Function: getDoctors
  Purpose: Fetch the list of all doctors from the API

   Use fetch() to send a GET request to the DOCTOR_API endpoint
   Convert the response to JSON
   Return the 'doctors' array from the response
   If there's an error (e.g., network issue), log it and return an empty array


  Function: deleteDoctor
  Purpose: Delete a specific doctor using their ID and an authentication token

   Use fetch() with the DELETE method
    - The URL includes the doctor ID and token as path parameters
   Convert the response to JSON
   Return an object with:
    - success: true if deletion was successful
    - message: message from the server
   If an error occurs, log it and return a default failure response


  Function: saveDoctor
  Purpose: Save (create) a new doctor using a POST request

   Use fetch() with the POST method
    - URL includes the token in the path
    - Set headers to specify JSON content type
    - Convert the doctor object to JSON in the request body

   Parse the JSON response and return:
    - success: whether the request succeeded
    - message: from the server

   Catch and log errors
    - Return a failure response if an error occurs


  Function: filterDoctors
  Purpose: Fetch doctors based on filtering criteria (name, time, and specialty)

   Use fetch() with the GET method
    - Include the name, time, and specialty as URL path parameters
   Check if the response is OK
    - If yes, parse and return the doctor data
    - If no, log the error and return an object with an empty 'doctors' array

   Catch any other errors, alert the user, and return a default empty result
*/
