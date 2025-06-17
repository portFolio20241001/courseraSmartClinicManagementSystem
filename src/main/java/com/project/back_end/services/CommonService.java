package com.project.back_end.services;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.back_end.DTO.Login;
import com.project.back_end.Entity.Admin;
import com.project.back_end.Entity.Appointment;
import com.project.back_end.Entity.Doctor;
import com.project.back_end.Entity.Patient;
import com.project.back_end.repo.AdminRepository;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * <h2>共通サービスクラス</h2>
 * <p>
 * 　全ロール共通で使い回す認証・バリデーション・検索系のビジネスロジックをまとめる。
 * </p>
 *
 * <ul>
 *   <li>{@link #validateToken(String, String)}</li>
 *   <li>{@link #validateAdmin(Admin)}</li>
 *   <li>{@link #filterDoctor(String, String, String)}</li>
 *   <li>{@link #validateAppointment(Appointment)}</li>
 *   <li>{@link #validatePatient(Patient)}</li>
 *   <li>{@link #validatePatientLogin(Login)}</li>
 *   <li>{@link #filterPatient(String, String, String)}</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CommonService {

    /* ====== 依存リポジトリ／サービス ====== */

    private final AdminRepository adminRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;

    private final DoctorService doctorService;
    private final PatientService patientService;

	/** JWT トークン生成用サービス */
    private final TokenService tokenService;
    
    /** パスワードのハッシュ化・照合を行うエンコーダー（例：BCrypt） */
    private final PasswordEncoder passwordEncoder;

    /* -------------------------------------------------------------------------
     * 1.  トークン検証
     * ---------------------------------------------------------------------- */

    /**
     * トークンが有効かどうかを検査する。
     *
     * @param token クライアントから渡された JWT
     * @param userRole トークンを検証したいロール（"admin" / "doctor" / "patient" 等）
     * @return 無効・期限切れの場合は 401、問題なければ 200
     */
    public Optional<String> validateToken(String token, String userRole) {

    	if (!tokenService.validateToken(token, userRole)) {
            return Optional.of("トークンが無効です");
        }
        
    	return Optional.empty();
        
    }
    
    
    /**
     * MVC 用の簡易チェック版 : トークンがロールに対して有効かどうか
     *
     * @param token     クライアントから渡された JWT
     * @param userRole  チェックしたいロール ("admin" / "doctor" / "patient")
     * @return true  … 有効 / false … 無効または期限切れ
     */
    public boolean isTokenValid(String token, String userRole) {
        return tokenService.validateToken(token, userRole);
    }
    
    

    /* -------------------------------------------------------------------------
     * 2.  管理者ログインの検証
     * ---------------------------------------------------------------------- */

    /**
     * 管理者のユーザー名／パスワードを検証し、成功時に新規トークンを返す。
     *
     * @param receivedAdmin ログインフォームで受け取った Admin（username・passwordのみ利用）
     * @return 成功時 : 200 + token / 失敗時 : 401 or 500
     */
    public ResponseEntity<Map<String, String>> validateAdmin(Admin receivedAdmin) {
    	
    	System.out.println("CommonService 管理者ログインの検証開始");

        Map<String, String> body = new HashMap<>();

        try {
            /* 1️⃣  ユーザ名で管理者を検索（User.username で一意） */
        	Optional<Admin> optionalAdmin = adminRepository
                    .findByUser_Username(receivedAdmin.getUser().getUsername());

			if (optionalAdmin.isEmpty()) {
				body.put("error", "ユーザー名が存在しません。");
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
			}
			
			Admin stored = optionalAdmin.get();  // この時点で null ではない

            /* 2️⃣  パスワードハッシュを照合
                   - フロントから受信した平文パスワードと、DB に保存済みのハッシュPWを比較          */
            boolean matches = passwordEncoder.matches(
                                   receivedAdmin.getUser().getPasswordHash(),   // 平文PW
                                   stored.getUser().getPasswordHash());              // ハッシュPW

            if (!matches) {
                body.put("error", "パスワードが一致しません。");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
            }

            /* 3️⃣  認証成功 → JWT 発行 */
            String token = tokenService.generateToken(stored.getUser().getUsername());
            
            body.put("token", token);
            body.put("message", "ログインに成功しました。");

            return ResponseEntity.ok(body);

        } catch (Exception e) {
            log.error("管理者認証失敗 : {}", e.getMessage(), e);
            body.put("error", "内部エラーが発生しました。");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
        }
    }

    /* -------------------------------------------------------------------------
     * 3.  医師フィルタリング
     * ---------------------------------------------------------------------- */

    /**
     * 名前・専門・時間帯を組み合わせて医師を検索する。
     *
     * @param name      医師氏名（部分一致, null可）
     * @param specialty 専門分野（完全一致, null可）
     * @param time      "AM" / "PM"（null の場合は時間帯フィルタなし）
     * @return key = "doctors" に List&lt;Doctor&gt; を格納したマップ
     */
    @Transactional(readOnly = true)
    public Map<String, Object> filterDoctor(String name, String specialty, String time) {

        List<Doctor> doctors;

        // ★ 3パターンの組み合わせで呼び分け
        if (name != null && specialty != null && time != null) {
            doctors = doctorService.filterDoctorsByNameSpecilityandTime(name, specialty, time);

        } else if (name != null && time != null) {
            doctors = doctorService.filterDoctorByNameAndTime(name, time);

        } else if (specialty != null && time != null) {
            doctors = doctorService.filterDoctorByTimeAndSpecility(specialty, time);

        } else if (name != null && specialty != null) {
            doctors = doctorService.filterDoctorByNameAndSpecility(name, specialty);

        } else if (name != null) {
            doctors = doctorService.findDoctorByName(name);              // 名前のみ
        } else if (specialty != null) {
            doctors = doctorService.filterDoctorBySpecility(specialty); // 専門のみ
        } else if (time != null) {
            doctors = doctorService.filterDoctorsByTime(time);          // 時間帯のみ
        } else {
            doctors = doctorService.findAllDoctors();                       // フィルタなし => 全件
        }

        Map<String, Object> result = new HashMap<>();
        result.put("doctors", doctors);
        return result;
    }

    
    
    /* -------------------------------------------------------------------------
     * 4.  予約可能チェック（日時の重複チェック
     * ---------------------------------------------------------------------- */

    /**
     * 予約が医師の空き時間に合致するか検証する。
     *
     * @param appointment 予約エンティティ
     * @return 1:OK / 0:空きなし / -1:医師未存在
     */
    @Transactional(readOnly = true)
    public int validateAppointment(Appointment appointment) {

        Long doctorId = appointment.getDoctor().getId();
        Optional<Doctor> opt = doctorRepository.findById(doctorId);
        
        //　そもそも指定した医師がいなけば-1を返す
        if (opt.isEmpty()) return -1;
        
        Doctor realDoctor = opt.get();
        LocalDateTime targetTime = appointment.getAppointmentTime();
        
        // ①DoctorのavailableTimes にその指定の時間が含まれているか？
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        boolean isAvailable = realDoctor.getAvailableTimes()
            .stream()
            .anyMatch(t -> LocalDateTime.parse(t, formatter).equals(targetTime));
        

        System.out.println("availableTimesチェック");
        System.out.println("isAvailable:"+isAvailable);

        
        
        //　含まれていなけばその時間に指定の医師は空いていないってことで-1を返す
        if (!isAvailable) return 0;

        // 予約日の 00:00:00〜23:59:59 を算出
        LocalDateTime start = appointment.getAppointmentTime().toLocalDate().atStartOfDay();
        LocalDateTime end   = appointment.getAppointmentTime().toLocalDate().atTime(LocalTime.MAX);

        // ②対象のDoctorに対して、既に同じ日時で予約が入っていないか確認
        List<Appointment> exists = appointmentRepository
                .findByDoctorIdAndAppointmentTimeBetween(doctorId, start, end)
                .stream()
                .filter(a -> a.getAppointmentTime().isEqual(appointment.getAppointmentTime()))
                .toList();
        
        System.out.println("appointment重複チェック");
        System.out.println("exists: " + exists);

        return exists.isEmpty() ? 1 : 0;
        
        
        
    }

    /* -------------------------------------------------------------------------
     * 5.  患者情報の重複チェック
     * ---------------------------------------------------------------------- */

    /**
     * ユーザ名または電話番号の重複登録を防止。
     *
     * @param patient 患者エンティティ
     * @return true:登録可 / false:既に存在
     */
    public boolean validatePatient(Patient patient) {

        return patientRepository
                .findByUser_UsernameOrPhone(patient.getUser().getUsername(),
                                            patient.getPhone())
                .isEmpty();
    }

    /* -------------------------------------------------------------------------
     * 6.  患者ログイン検証
     * ---------------------------------------------------------------------- */

    /**
     * 患者ログイン用の認証ロジック。
     *
     * @param login フロントから渡された username / password
     * @return 成功時: token, 失敗時: 401
     */
    public ResponseEntity<Map<String, String>> validatePatientLogin(Login login) {

        Map<String, String> body = new HashMap<>();

        Patient stored = patientRepository
                .findByUser_Username(login.getUsername())
                .orElse(null);

        if (stored == null) {
            body.put("error", "ユーザー名が存在しません。");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
        }

        /* 2. パスワード検証  */
        if (!passwordEncoder.matches(
        							 login.getPassword(),									//平文PW
        							 stored.getUser().getPasswordHash())) {		//ハッシュPW
        	
            body.put("error", "パスワードが一致しません。");
            
            System.out.println("login.getPassword():" + "[" + login.getPassword() + "]");
            System.out.println("patient.getUser().getPasswordHash():" + stored.getUser().getPasswordHash());

            
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
        }

        String token = tokenService.generateToken(login.getUsername());
        body.put("token", token);
        body.put("message", "ログインに成功しました。");
        
        return ResponseEntity.ok(body);
        
    }

    /* -------------------------------------------------------------------------
     * 7.  患者側の予約履歴フィルタ
     * ---------------------------------------------------------------------- */

    /**
     * 条件・医師名に基づいて予約履歴を絞り込む。
     *
     * @param condition "past" / "future" / null
     * @param doctorName 医師名（部分一致, null 可）
     * @param token 患者特定用トークン
     * @return フィルタ結果を格納した ResponseEntity
     */
    public ResponseEntity<Map<String, Object>> filterPatient(String condition,
                                                             String doctorName,
                                                             String token) {
    	
    	System.out.println("condition:"+condition);
    	System.out.println("doctorName:"+doctorName);
    	System.out.println("token:"+token);
    	
    	
        Map<String, Object> body = new HashMap<>();

        // トークンから username を抽出
        String username = tokenService.extractUsername(token);
        
        if (username == null) {
            body.put("error", "トークンが無効です。");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
        }

        // 患者ID の取得
        Optional<Patient> opt = patientRepository.findByUser_Username(username);
        if (opt.isEmpty()) {
            body.put("error", "患者情報が見つかりません。");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
        }
        Long patientId = opt.get().getId();
        
        
        // 「文字列 "null"」は null として扱う
        if ("null".equalsIgnoreCase(condition)) {
            condition = null;
        }
        if ("null".equalsIgnoreCase(doctorName)) {
            doctorName = null;
        }
        

        /* ------ 条件分岐して PatientService の既存ロジックを呼び出す ------ */
        ResponseEntity<?> result;
        if (condition != null && doctorName != null) {
        	
        	System.out.println("b");
        	
            result = patientService.filterByDoctorAndCondition(doctorName, patientId, condition);
        } else if (condition != null) {
        	System.out.println("bb");
        	
            result = patientService.filterByCondition(condition, patientId);
        } else if (doctorName != null) {
        	System.out.println("bbb");
        	
            result = patientService.filterByDoctor(doctorName, patientId);
        } else {
        	
        	System.out.println("bbbb");
        	
            result = patientService.getPatientAppointment(patientId);
        }

        // PatientService からの戻り値をそのままラップ
        body.put("data", result.getBody());
        return ResponseEntity.status(result.getStatusCode()).body(body);
        
    }
}
