package com.project.back_end.services;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.back_end.DTO.AppointmentDTO;
import com.project.back_end.Entity.Appointment;
import com.project.back_end.Entity.Payment;
import com.project.back_end.repo.AppointmentRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final TokenService tokenService;
    private final CommonService service; // validateAppointment()を持つサービス

//  private final PatientRepository patientRepository;
//  private final DoctorRepository doctorRepository;

    /**
     * 新しい予約を登録するメソッド。
     * 予約保存に成功した場合はHTTP 200を返し、失敗時は500エラーを返す。
     * @param appointment 登録する予約情報
     * @return 処理結果のメッセージを含むHTTPレスポンス
     */
    @Transactional
    public ResponseEntity<Map<String, String>> bookAppointment(Appointment appointment) {
    	
        Map<String, String> response = new HashMap<>();
        
        try {
            // Payment が含まれているかチェック
            if (appointment.getPayment() != null) {
            	
                Payment payment = appointment.getPayment();
                payment.setAppointment(appointment); // リレーションの逆方向もセット
                
            }

            appointmentRepository.save(appointment); // Cascade.ALL により Payment も保存される

            response.put("message", "予約と支払い情報を登録しました。");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (Exception e) {
        	
            log.error("予約保存に失敗しました: {}", e.getMessage());
            response.put("error", "予約保存に失敗しました。");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            
        }
    }

    /**
     * 既存の予約を更新するメソッド。
     * - 予約が存在するか確認
     * - 更新権限（患者IDの一致）をチェック
     * - 医師の空き時間をservice.validateAppointment()で検証
     * - 問題なければ予約情報を更新し保存
     * @param updatedAppointment 更新内容を含む予約オブジェクト
     * @return 処理結果メッセージを含むHTTPレスポンス
     */
    @Transactional
    public ResponseEntity<Map<String, String>> updateAppointment(Appointment updatedAppointment) {
    	
        Map<String, String> response = new HashMap<>();

        // 予約の存在確認
        Optional<Appointment> optionalAppointment = appointmentRepository.findById(updatedAppointment.getId());
        if (optionalAppointment.isEmpty()) {
            response.put("message", "予約が存在しません。");
            return ResponseEntity.badRequest().body(response);
        }

        Appointment existingAppointment = optionalAppointment.get();

        // 更新権限チェック（予約患者と更新患者が同一か）
        if (!existingAppointment.getPatient().getId().equals(updatedAppointment.getPatient().getId())) {
            response.put("message", "この予約を更新する権限がありません。");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }

        // --- 医師空き時間チェック ---
        int avail = service.validateAppointment(updatedAppointment);
        
        if (avail == -1) {
        	response.put("message", "医師が存在しません。");
            return ResponseEntity.badRequest().body(response);
        }
        
        if (avail == 0) {
        	response.put("message", "指定時間に医師の空きがありません。");
            return ResponseEntity.badRequest().body(response);
        }

        // 予約情報の更新
        existingAppointment.setAppointmentTime(updatedAppointment.getAppointmentTime());
        existingAppointment.setDoctor(updatedAppointment.getDoctor());
        // 他に更新が必要なフィールドがあればここで設定

        // 更新を保存
        appointmentRepository.save(existingAppointment);

        response.put("message", "予約が正常に更新されました。");
        return ResponseEntity.ok(response);
    }

    /**
     * 予約をキャンセルするメソッド。
     * - トークンから患者IDを取得し認証
     * - 予約の存在確認
     * - キャンセル権限（患者IDの一致）をチェック
     * - 予約を削除
     * @param appointmentId キャンセル対象の予約ID
     * @param token 患者認証用トークン
     * @return 処理結果メッセージを含むHTTPレスポンス
     */
    @Transactional
    public ResponseEntity<Map<String, String>> cancelAppointment(Long appointmentId, String token) {
        Map<String, String> response = new HashMap<>();

        // トークンから患者IDを取得
        Long patientId = tokenService.getPatientIdFromToken(token);
        
        if (patientId == null) {
            response.put("error", "無効なトークンです。");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        // 予約の存在確認
        Optional<Appointment> optionalAppointment = appointmentRepository.findById(appointmentId);
        
        if (optionalAppointment.isEmpty()) {
            response.put("error", "予約が見つかりません。");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        Appointment appointment = optionalAppointment.get();

        // キャンセル権限チェック（予約患者とトークン患者が同一か）
        if (!appointment.getPatient().getId().equals(patientId)) {
            response.put("error", "この予約をキャンセルする権限がありません。");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }

        // ステータス変更による論理キャンセル（例: 2 = キャンセル）
        appointment.setStatus(2);
        appointmentRepository.save(appointment);

        response.put("message", "予約がキャンセルされました。");
        return ResponseEntity.ok(response);
    }
    
    /**
     * 医師ID・日付に基づいて予約リストを取得するメソッド。
     * - トークンの有効性を検証
     * - 日付の開始～終了までの範囲で絞り込み
     * - 患者名指定があれば部分一致検索
     * @param doctorId 医師ID
     * @param date 検索対象の日付
     * @param token 認証トークン
     * @return 予約リストを含むレスポンス
     */
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> getAppointmentsByDate(Long doctorId, LocalDate date, String token) {
    	
        Map<String, Object> response = new HashMap<>();
        
        // トークンの有効性チェック
        if (!tokenService.isValidToken(token)) {
            response.put("error", "無効なトークンです。");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        List<Appointment> appointments = appointmentRepository
            .findByDoctorIdAndAppointmentTimeBetween(doctorId, startOfDay, endOfDay);
        
        System.out.println("ccc");
        
        // DTOに詰め替えるのはベストプラクティス　できれば他もそうしたほうがよき
        List<AppointmentDTO> dtos = appointments.stream()
                .map(AppointmentDTO::new)
                .toList();

        response.put("appointments", dtos);
        return ResponseEntity.ok(response);
    }

    /**
     * 医師ID・日付・患者名（任意）に基づいて予約リストを取得するメソッド。
     * - トークンの有効性を検証
     * - 日付の開始～終了までの範囲で絞り込み
     * - 患者名指定があれば部分一致検索
     * @param doctorId 医師ID
     * @param date 検索対象の日付
     * @param patientName 検索に使う患者名（任意）
     * @param token 認証トークン
     * @return 予約リストを含むレスポンス
     */
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> getAppointments(Long doctorId, LocalDate date, String patientName, String token) {
    	
        Map<String, Object> response = new HashMap<>();

        // トークンの有効性チェック
        if (!tokenService.isValidToken(token)) {
            response.put("error", "無効なトークンです。");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        // 指定日の00:00～23:59:59.999を表すLocalDateTimeを作成
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
        
        // 「文字列 "null"」は null として扱う
        if ("null".equalsIgnoreCase(patientName)) {
        	patientName = null;
        }

        List<Appointment> appointments;
        if (patientName != null && !patientName.isBlank()) {
        	
        	System.out.println("ddd");
        	
            // 患者名部分一致で絞り込み
            appointments = appointmentRepository.findByDoctorIdAndPatientFullNameContainingIgnoreCaseAndAppointmentTimeBetween(
                    doctorId, patientName, startOfDay, endOfDay);
            
            System.out.println("appointments"+appointments);
            
        } else {
            // 患者名指定なしの場合
            appointments = appointmentRepository.findByDoctorIdAndAppointmentTimeBetween(
                    doctorId, startOfDay, endOfDay);
        }
        
        // DTOに詰め替えるのはベストプラクティス　できれば他もそうしたほうがよき
        List<AppointmentDTO> dtos = appointments.stream()
                .map(AppointmentDTO::new)
                .toList();

        response.put("appointments", dtos);
        return ResponseEntity.ok(response);
    }
    
    
    /**
     * 予約ステータスを更新するユーティリティ.
     *
     * <pre>
     * ・存在チェックのみ行い、見つからなければ何もしない
     * ・更新系なので @Transactional を付与
     * ・内部エラーは WARN ログに残すだけ（呼び出し元では握りつぶす想定）
     * </pre>
     *
     * @param appointmentId 更新対象の予約 ID
     * @param newStatus     新しいステータス値
     */
    @Transactional
    public void changeStatus(Long appointmentId, int newStatus) {

        // 1) レコード存在確認
        if (!appointmentRepository.existsById(appointmentId)) {
            log.warn("changeStatus: 指定 ID の予約が存在しません -> id={}", appointmentId);
            return;                                     // 何もしない
        }

        try {
            // 2) ステータス更新（リポジトリの@Modifying JPQL を利用）
            appointmentRepository.updateStatus(newStatus, appointmentId);
            log.info("予約ID={} のステータスを {} に更新しました。", appointmentId, newStatus);

        } catch (Exception e) {
            // 3) 例外が出ても Prescription 登録自体は失敗させたくないため WARN で記録
            log.warn("予約ステータス更新に失敗しました。id={}, status={}, cause={}",
                      appointmentId, newStatus, e.toString());
        }
    }
    
}
