package com.project.back_end.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.back_end.DTO.AppointmentDTO;
import com.project.back_end.Entity.Appointment;
import com.project.back_end.Entity.Patient;
import com.project.back_end.Entity.User;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.PatientRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 患者に関するビジネスロジックを提供するサービスクラス。
 */
@Service // 1. Spring管理のサービスクラスとして定義
@RequiredArgsConstructor // 2. コンストラクタによる依存性注入を自動生成
@Slf4j
public class PatientService {

    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final TokenService tokenService;
    
    private final PasswordEncoder passwordEncoder;   // ★ パスワードハッシュ比較用


    /* =====================================================================
     * 3. 患者の新規登録
     * =================================================================== */

    /**
     * 患者を作成し DB に保存する。
     *
     * @param patient 追加する {@link Patient}
     * @return <pre>
     *   201 … {"message": "..."}
     *   500 … {"error":   "..."}
     * </pre>
     */
    @Transactional
    public ResponseEntity<Map<String, String>> createPatient(Patient patient) {

        Map<String, String> body = new HashMap<>();

        try {
            // ユーザー名の重複チェック
            if (patientRepository.existsByUser_Username(patient.getUser().getUsername())) {
                body.put("error", "同じユーザー名の患者が既に存在します。");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
            }

            // パスワードのハッシュ化
            String rawPassword = patient.getUser().getPasswordHash();
            String hashedPassword = passwordEncoder.encode(rawPassword);
            patient.getUser().setPasswordHash(hashedPassword);

            // ロールの明示的設定
            patient.getUser().setRole(User.Role.ROLE_PATIENT);

            // 双方向関連の明示（必要なら）
            patient.getUser().setPatient(patient);

            // 登録処理
            patientRepository.save(patient);

            body.put("message", "患者を登録しました。");
            return ResponseEntity.status(HttpStatus.CREATED).body(body);

        } catch (Exception e) {
            log.error("患者作成に失敗しました: {}", e.getMessage());

            body.put("error", "内部エラーが発生しました。");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
        }
    }


    /**
     * 4. 指定された患者IDの予約一覧を取得する（DTO形式）
     * @param id 患者ID
     * @return 予約のDTOリスト
     */
    @Transactional
    public ResponseEntity<?> getPatientAppointment(Long id) {
    	
        try {
            List<Appointment> appointments = appointmentRepository.findByPatientId(id);
            
            List<AppointmentDTO> dtoList = 
            		appointments.stream()
                    .map(AppointmentDTO::new)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(dtoList);
            
        } catch (Exception e) {
            log.error("予約取得失敗: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("予約取得に失敗しました");
        }
    }

    /**
     * 5. 条件に基づいて予約をフィルタ（"past" or "future"）
     */
    public ResponseEntity<?> filterByCondition(String condition, Long patientId) {
    	
        try {
        	// ステータスを設定（0: 予約済み, 1: 完了, 2: キャンセル）今回は0と1のみ
            int status = "past".equalsIgnoreCase(condition) ? 1 
            				: "future".equalsIgnoreCase(condition) ? 0
            				: "cancel".equalsIgnoreCase(condition) ? 2 : -1;
              
            if (status == -1) return ResponseEntity.badRequest().body("条件が無効です");
            
            System.out.println("status:" + status);
            
            List<AppointmentDTO> result = appointmentRepository
            		.findByPatient_IdAndStatusOrderByAppointmentTimeAsc(patientId, status)
                    .stream().map(AppointmentDTO::new)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
        	
            log.error("条件フィルター失敗: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("フィルターに失敗しました");
        }
    }

    /**
     * 6. 医師名で予約をフィルタ
     */
    public ResponseEntity<?> filterByDoctor(String doctorName ,Long patientId ) {
    	
        try {
            List<AppointmentDTO> result = appointmentRepository.
            		filterByDoctorNameAndPatientId(doctorName, patientId)
                    .stream().map(AppointmentDTO::new)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
        	
            log.error("医師名フィルター失敗: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("フィルターに失敗しました");
        }
    }

    /**
     * 7. 医師名と条件の両方で予約をフィルタ
     */
    public ResponseEntity<?> filterByDoctorAndCondition(String doctorName, Long patientId, String condition) {
    	
        try {
        	
        	// ステータスを設定（0: 予約済み, 1: 完了, 2: キャンセル）今回は0と1のみ
            int status = "past".equalsIgnoreCase(condition) ? 1 :
            				"future".equalsIgnoreCase(condition) ? 0 : 
            				"cancel".equalsIgnoreCase(condition) ? 2 : -1;
            
            System.out.println("status:" + status);
            
            if (status == -1) return ResponseEntity.badRequest().body("条件が無効です");

            List<AppointmentDTO> result = appointmentRepository
                    .filterByDoctorNameAndPatientIdAndStatus(doctorName, patientId , status)
                    .stream().map(AppointmentDTO::new)
                    .collect(Collectors.toList());
            
            System.out.println("いいいいい");
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
        	
            log.error("医師と条件フィルター失敗: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("フィルターに失敗しました");
        }
    }

    /* ----------------------------------------------------------------
     * 8. トークンを使って患者情報を取得
     * ------------------------------------------------------------- */
    /**
     * JWT トークン内の username を元に、対応する患者情報を返却する。
     *
     * @param token JWT トークン
     * @return key = <b>patient</b> に {@link Patient} を格納したレスポンス
     */
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> getPatientDetails(String token) {

        Map<String, Object> body = new HashMap<>();

        try {
            /* 1) トークンから username を抽出 */
            String username = tokenService.extractUsername(token);

            /* 2) username で患者を検索（User.username 経由） */
            Patient patient = patientRepository.findByUser_Username(username)
                                               .orElseThrow(() -> 
                                                   new RuntimeException("該当する患者が見つかりません"));

            body.put("patient", patient);
            return ResponseEntity.ok(body);

        } catch (RuntimeException e) {
            log.warn("患者情報取得 : {}", e.getMessage());
            body.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);

        } catch (Exception e) {
            log.error("患者情報取得失敗: {}", e.getMessage());
            body.put("error", "患者情報の取得に失敗しました。");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
        }
    }

}
