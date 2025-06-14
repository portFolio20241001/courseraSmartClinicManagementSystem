package com.project.back_end.controllers;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * <h2>ValidationFailed ― バリデーション失敗時のグローバル例外ハンドラ</h2>
 *
 * <pre>
 * すべての @RestController で発生した {@link MethodArgumentNotValidException}
 * （@Valid 失敗）を捕捉し、統一された JSON 形式で
 * 400 Bad Request を返却する。
 * </pre>
 *
 * 例：リクエストボディのバリデーションエラー
 * <pre>
 * {
 *   "message": "ユーザー名は必須です。 / 電話番号はXXX-XXXX-XXXX形式で入力してください。"
 * }
 * </pre>
 *
 * @author back_end team
 */
@RestControllerAdvice
public class ValidationFailed {

    /* ------------------------------------------------------------------
     *  バリデーション例外ハンドリング
     * ----------------------------------------------------------------- */

    /**
     * {@link MethodArgumentNotValidException} を捕捉し、
     * フィールドごとのデフォルトメッセージを連結して返す。
     *
     * @param ex リクエストボディ検証失敗時に投げられる例外
     * @return 400 Bad Request とエラーメッセージ
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationException(
            MethodArgumentNotValidException ex) {

        /* 1) すべての FieldError からメッセージを抽出し " / " で連結 */
        String msg = ex.getBindingResult()
                       .getFieldErrors()
                       .stream()
                       .map(FieldError::getDefaultMessage)
                       .collect(Collectors.joining(" / "));

        /* 2) レスポンスボディ作成 */
        Map<String, String> body = new HashMap<>();
        body.put("message", msg);

        /* 3) 400 Bad Request で返却 */
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(body);
    }
}
