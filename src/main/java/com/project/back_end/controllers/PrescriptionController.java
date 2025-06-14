// ================================================
// PrescriptionController.java
// ================================================
package com.project.back_end.controllers;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.back_end.Entity.PrescriptionForMongo;
import com.project.back_end.services.AppointmentService;
import com.project.back_end.services.CommonService;
import com.project.back_end.services.PrescriptionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * <h2>PrescriptionController ― 処方箋関連 REST エンドポイント</h2>
 *
 * <pre>
 * ・処方箋の登録（POST）
 * ・処方箋の取得（GET）
 * ※ すべて JSON を返す
 * </pre>
 *
 * @author back_end team
 */
@RestController
@RequestMapping("${api.path}prescription")   // 例: /api/prescription
@RequiredArgsConstructor                     // Lombok: コンストラクタ注入
@Valid
public class PrescriptionController {

    /** 処方箋の保存／取得ロジック */
    private final PrescriptionService prescriptionService;

    /** 共通ロジック（トークン検証 など） */
    private final CommonService commonService;

    /** 処方箋発行後に予約ステータスを更新するためのサービス */
    private final AppointmentService appointmentService;

    /* ---------------------------------------------------------------------
     * 1. 処方箋を保存（Doctor 権限）
     * ------------------------------------------------------------------ */

    /**
     * <p>指定された予約に対し、新しい処方箋を登録する。</p>
     *
     * @param prescription  登録する {@link PrescriptionForMongo}
     * @param token         医師トークン
     * @return <pre>
     *   201: 登録成功
     *   400: 重複 or バリデーション失敗
     *   401: 認証失敗
     *   500: 内部エラー
     * </pre>
     */
    @PostMapping("/{token}")
    @Transactional                                     // 書き込み系なので付与
    public ResponseEntity<Map<String, String>> savePrescription(
            @RequestBody @Valid PrescriptionForMongo prescription,
            @PathVariable String token) {

        /* ===== ① トークン検証（doctor ロール） ===== */
        ResponseEntity<Map<String, String>> auth =
                commonService.validateToken(token, "doctor");

        if (auth.getBody() != null && !auth.getBody().isEmpty()) {
            return auth;                                // 認証失敗をそのまま返却
        }

        /* ===== ② 処方箋登録処理 ===== */
        ResponseEntity<Map<String, String>> result =
                prescriptionService.savePrescription(prescription);

        /* ===== ③ 予約ステータス更新（成功時のみ）===== */
        if (result.getStatusCode() == HttpStatus.CREATED) {
            // 例: 1 = 完了 としてステータスを変更
            appointmentService.changeStatus(
                    prescription.getAppointmentId(), 1);
        }

        return result;
    }

    /* ---------------------------------------------------------------------
     * 2. 処方箋を取得（Doctor 権限）
     * ------------------------------------------------------------------ */

    /**
     * <p>予約 ID を指定して処方箋を取得する。</p>
     *
     * @param appointmentId 検索対象の予約 ID
     * @param token         医師トークン
     * @return <pre>
     *   200: 取得成功（key = "prescription"）
     *   401: 認証失敗
     *   404: 該当なし
     *   500: 内部エラー
     * </pre>
     */
    @GetMapping("/{appointmentId}/{token}")
    public ResponseEntity<Map<String, Object>> getPrescription(
            @PathVariable Long appointmentId,
            @PathVariable String token) {

        /* ===== ① トークン検証（doctor ロール） ===== */
        ResponseEntity<Map<String, String>> auth =
                commonService.validateToken(token, "doctor");

        if (auth.getBody() != null && !auth.getBody().isEmpty()) {
            // 認証エラーをそのまま返却
            Map<String, Object> err = new HashMap<>(auth.getBody());
            return ResponseEntity.status(auth.getStatusCode()).body(err);
        }

        /* ===== ② 処方箋取得 ===== */
        return prescriptionService.getPrescription(appointmentId);
    }
}
