package com.project.back_end.services;

import com.project.back_end.Entity.PrescriptionForMongo;
import com.project.back_end.repo.PrescriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 処方箋（Prescription）に関するビジネスロジックを提供するサービスクラス。
 */
@Service // SpringによりDI管理されるサービスコンポーネントとしてマーク
@RequiredArgsConstructor // 必要な依存をコンストラクタインジェクションで自動生成
@Slf4j // ログ出力に使用
public class PrescriptionService {

    // 処方箋を保存・取得するためのMongoDBリポジトリ
    private final PrescriptionRepository prescriptionRepository;

    /**
     * 指定された予約（appointmentId）に対する処方箋を保存する。
     * - すでに登録されている場合は保存しない。
     *
     * @param prescription 処方箋データ（MongoDB用のドキュメント形式）
     * @return 処理結果のメッセージとHTTPステータスを含むレスポンス
     */
    public ResponseEntity<Map<String, String>> savePrescription(PrescriptionForMongo prescription) {
        Map<String, String> response = new HashMap<>();
        try {
            
        	// 同一アポイントメントIDの処方箋が存在するかチェック
            boolean exists = prescriptionRepository.existsByAppointmentId(prescription.getAppointmentId());
            
            if (exists) {
                response.put("error", "すでにこの予約には処方箋が登録されています。");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            // 新規保存
            prescriptionRepository.save(prescription);
            response.put("message", "処方箋が正常に保存されました。");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            log.error("処方箋の保存中にエラー: {}", e.getMessage());
            response.put("error", "処方箋の保存に失敗しました。");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 指定された予約（appointmentId）に紐づく処方箋を取得する。
     *
     * @param appointmentId 予約ID
     * @return 処方箋データ（存在する場合）またはエラーメッセージを含むレスポンス
     */
    public ResponseEntity<Map<String, Object>> getPrescription(Long appointmentId) {
        Map<String, Object> response = new HashMap<>();
        try {
        	
            // findByAppointmentId は List を返すため、1件目を取り出す（なければ null）
            PrescriptionForMongo prescription = prescriptionRepository.findByAppointmentId(appointmentId)
                                                                     .stream()
                                                                     .findFirst()
                                                                     .orElse(null);
            
            if (prescription == null) {
                response.put("message", "指定の予約に対する処方箋が見つかりません。");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            response.put("prescription", prescription);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("処方箋の取得中にエラー: {}", e.getMessage());
            response.put("error", "処方箋の取得に失敗しました。");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
