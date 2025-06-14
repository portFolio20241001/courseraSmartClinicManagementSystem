package com.project.back_end.services;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.back_end.DTO.Login;
import com.project.back_end.Entity.Appointment;
import com.project.back_end.Entity.Doctor;
import com.project.back_end.repo.AppointmentRepository;
//import com.project.back_end.services.TokenService;
import com.project.back_end.repo.DoctorRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service  // 1. サービス層コンポーネントとして登録
@RequiredArgsConstructor  // 2. コンストラクタインジェクションを自動生成
@Slf4j  // ログ出力用アノテーション
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final TokenService tokenService;
    
    private final PasswordEncoder passwordEncoder;   // ★ パスワードハッシュ比較用
    
    
    /**
     * システム内の全医師をリストで取得して返すユーティリティ.
     *
     * <p>トランザクションは読み取り専用。</p>
     *
     * @return 全 {@link Doctor} のリスト
     */
    @Transactional(readOnly = true)
    public List<Doctor> findAllDoctors() {
        // 例外処理は呼び出し側でまとめて行いたいので、ここではそのまま返すだけ
        return doctorRepository.findAll();
    }
    
    /**
     * システム内の全医師を取得して返す.
     *
     * @return 下記キーを持つレスポンス
     * <ul>
     *   <li><b>doctors</b> … 取得した {@link Doctor} のリスト</li>
     *   <li><b>error</b>   … 例外発生時のみ設定</li>
     * </ul>
     *
     * <p>HTTP ステータス</p>
     * <ul>
     *   <li>200 … 取得成功</li>
     *   <li>500 … DB 例外など内部エラー</li>
     * </ul>
     */
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> getDoctors() {

        Map<String, Object> body = new HashMap<>();

        try {
            body.put("doctors", findAllDoctors());
            return ResponseEntity.ok(body);

        } catch (Exception e) {
            log.error("Doctor 取得失敗 : {}", e.getMessage());
            body.put("error", "医師情報の取得に失敗しました。");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
        }
    }
    
    

    /**
     * <pre>
     * 指定日の空き時間を返す
     *  - Doctor.availableTimes は "09:00-10:00" 形式で保持
     *  - その日の予約（start-end 範囲）を取得して重複を除外
     * </pre>
     *
     * @param doctorId 医師 ID
     * @param date     対象日（yyyy-MM-dd）
     * @return まだ空いている時間帯のリスト
     */
    @Transactional(readOnly = true)
    public List<String> getDoctorAvailability(Long doctorId, LocalDate date) {

        // 1) 医師存在チェック
        Doctor doctor = doctorRepository.findById(doctorId)
                                        .orElse(null);
        
        if (doctor == null) return Collections.emptyList();

        // 2) その日 0:00〜23:59 で既予約を取得
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay   = date.atTime(LocalTime.MAX);

        List<Appointment> booked = appointmentRepository
                .findByDoctorIdAndAppointmentTimeBetween(doctorId, startOfDay, endOfDay);

        // 3) 予約済みスロットを文字列で取り出し
        Set<String> bookedSlotStarts = booked.stream()
            .map(a -> a.getAppointmentTime().toLocalTime().toString()) // "09:00"
            .collect(Collectors.toSet());

        // 4) availableTimes から予約済みを除外
        return doctor.getAvailableTimes()
                     .stream()
                     .filter(slot -> {                 // "09:00-10:00"
                         String startTime = slot.split("-")[0]; // "09:00"
                         return !bookedSlotStarts.contains(startTime);
                     })
                     .toList();
    }


    /**
     * 新しい医師を保存。メール重複時は -1、成功時は 1、内部エラーは 0 を返す。
     * @param doctor 登録する Doctor エンティティ
     */
    @Transactional
    public int saveDoctor(Doctor doctor) {
        try {
            if (doctorRepository.existsByUser_Username(doctor.getUser().getUsername())) {
                return -1;
            }
            
            doctorRepository.save(doctor);
            
            return 1;
            
        } catch (Exception e) {
        	
            log.error("Doctor登録エラー: {}", e.getMessage());
            
            return 0;
            
        }
    }

    /**
     * 既存医師情報を更新する.
     *
     * @param doctor 更新対象 {@link Doctor}
     * @return 成功・失敗を表すメッセージを格納した {@link ResponseEntity}
     *
     * <ul>
     *   <li>200 … 更新成功</li>
     *   <li>404 … 該当 ID が存在しない</li>
     *   <li>500 … DB 例外など内部エラー</li>
     * </ul>
     */
    @Transactional
    public ResponseEntity<Map<String, String>> updateDoctor(Doctor doctor) {

        Map<String, String> body = new HashMap<>();

        /* --- 1. 存在チェック -------------------- */
        if (!doctorRepository.existsById(doctor.getId())) {
            body.put("error", "指定 ID の医師は存在しません。");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
        }

        /* --- 2. 更新処理 ----------------------- */
        try {
            doctorRepository.save(doctor);
            body.put("message", "医師情報を更新しました。");
            return ResponseEntity.ok(body);

        } catch (Exception e) {
            log.error("Doctor 更新失敗 : {}", e.getMessage());
            body.put("error", "内部エラーにより更新できませんでした。");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
        }
    }


    /**
     * 医師を削除する.
     *
     * @param doctorId 削除対象の医師 ID
     * @return 結果メッセージを格納したレスポンス
     *
     * <p>HTTP ステータス</p>
     * <ul>
     *   <li>200 … 削除成功</li>
     *   <li>404 … 該当 ID が存在しない</li>
     *   <li>500 … 予約の削除や DB 例外で失敗</li>
     * </ul>
     */
    @Transactional
    public ResponseEntity<Map<String, String>> deleteDoctor(Long doctorId) {

        Map<String, String> body = new HashMap<>();

        /* --- 1. 存在チェック -------------------------------- */
        if (!doctorRepository.existsById(doctorId)) {
            body.put("error", "指定 ID の医師は存在しません。");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
        }

        /* --- 2. 予約 & 医師レコード削除 --------------------- */
        try {
            appointmentRepository.deleteAllByDoctorId(doctorId);  // 先に外側を削除
            doctorRepository.deleteById(doctorId);

            body.put("message", "医師を削除しました。");
            return ResponseEntity.ok(body);

        } catch (Exception e) {
            log.error("Doctor 削除失敗 : {}", e.getMessage());
            body.put("error", "内部エラーにより削除できませんでした。");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
        }
    }



    /* =================================================================
     * 0.  医師ログイン  -------------------------------------------------
     * ================================================================= */
    /**
     * <pre>
     * 医師ログイン認証を行い、成功したら JWT トークンを返す。
     *
     * 1) username が存在するかチェック
     * 2) 平文パスワードとハッシュの一致を確認
     * 3) TokenService でトークンを生成
     * </pre>
     *
     * @param login {@link Login} ・・・ username / password を受け取る DTO
     * @return token を格納した 200 OK, 失敗時は 401 あるいは 500
     */
    public ResponseEntity<Map<String, String>> doctorLogin(Login login) {

        Map<String, String> body = new HashMap<>();

        try {

            /* 1. ユーザー名で医師を取得（User.username で検索） */
            Optional<Doctor> opt = doctorRepository.findByUser_Username(login.getUsername());

            if (opt.isEmpty()) {
                body.put("error", "ユーザー名が存在しません。");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
            }

            Doctor doctor = opt.get();

            /* 2. パスワード検証  */
            if (!passwordEncoder.matches(
            							 login.getPassword(),									//平文PW
                                         doctor.getUser().getPasswordHash())) {		//ハッシュPW
            	
                body.put("error", "パスワードが一致しません。");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
                
            }

            /* 3. JWT トークン生成  */
            String token = tokenService.generateToken(doctor.getUser().getUsername());

            body.put("token",   token);
            body.put("message", "ログインに成功しました。");

            return ResponseEntity.ok(body);

        } catch (Exception e) {
            log.error("医師ログイン処理で例外発生: {}", e.getMessage());
            body.put("error", "内部エラーが発生しました。");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
        }
    }

    /**
     * 名前を部分一致で医師検索。
     * @param name 検索文字列
     */
    @Transactional(readOnly = true)
    public List<Doctor> findDoctorByName(String name) {
        return doctorRepository.findByFullNameLikeIgnoreCase(name);
    }


    /**
     * 名前＋時間帯フィルター。
     */
    @Transactional(readOnly = true)
    public List<Doctor> filterDoctorByNameAndTime(String name, String period) {
        return filterDoctorByTime(findDoctorByName(name), period);
    }

    /**
     * 名前＋専門フィルター。
     */
    @Transactional(readOnly = true)
    public List<Doctor> filterDoctorByNameAndSpecility(String name, String specialty) {
    	
        return doctorRepository.findByFullNameContainingIgnoreCaseAndSpecialtyIgnoreCase(name, specialty);
        
    }

    /**
     * 専門＋時間帯フィルター。
     */
    @Transactional(readOnly = true)
    public List<Doctor> filterDoctorByTimeAndSpecility(String specialty, String period) {
    	
        return filterDoctorByTime(doctorRepository.findBySpecialtyIgnoreCase(specialty), period);
        
    }

    /**
     * 専門だけでフィルター。
     */
    @Transactional(readOnly = true)
    public List<Doctor> filterDoctorBySpecility(String specialty) {
    	
        return doctorRepository.findBySpecialtyIgnoreCase(specialty);
        
    }

    /**
     * 時間帯のみでフィルター。
     */
    @Transactional(readOnly = true)
    public List<Doctor> filterDoctorsByTime(String period) {
    	
        return filterDoctorByTime(doctorRepository.findAll(), period);
        
    }

    
    /**
     * 名前・専門・AM/PM を組み合わせて医師をフィルターする。
     *
     * @param name 医師名（部分一致検索、例："山田" → "山田 太郎"にマッチ）
     * @param specialty 専門分野（完全一致、例："内科"）
     * @param period 時間帯（"AM" または "PM"）
     * @return 条件に一致する医師のリスト
     *
     * 例：
     * - "山田", "内科", "AM" の場合：
     *   → 名前に「山田」を含み、専門が「内科」で、午前に診察可能な医師を返す。
     */
    @Transactional(readOnly = true)
    public List<Doctor> filterDoctorsByNameSpecilityandTime(String name, String specialty, String period) {
    	
        // 名前＋専門分野でまず絞り込む
        List<Doctor> list = doctorRepository.findByFullNameContainingIgnoreCaseAndSpecialtyIgnoreCase(name, specialty);
        
        // 午前 or 午後でさらにフィルター
        return filterDoctorByTime(list, period);
    }


    /**
     * 医師リストを AM（午前）/ PM（午後）の時間帯でフィルターする。
     *
     * @param doctors 医師のリスト（すでに名前・専門で絞り込まれたリスト）
     * @param period "AM" または "PM"
     * @return 指定された時間帯に診療可能な医師リスト
     *
     * 例：
     * - 医師Aの availableTimes = ["09:00", "14:00"]
     * - period = "AM" → "09:00" が含まれているのでマッチ
     */
    public List<Doctor> filterDoctorByTime(List<Doctor> doctors, String period) {
    	
        return doctors.stream()
            .filter(d -> d.getAvailableTimes().stream()  // 医師の診察可能時間を調べる
                          .anyMatch(slot -> matchesPeriod(slot, period)))  // どれか1つでも時間帯にマッチしていればOK
            .collect(Collectors.toList());  // 条件に合う医師だけをリストにして返す
        
    }


    /**
     * 時間スロットが AM（午前）か PM（午後）かを判定する。
     *
     * @param slot 時間（例："09:00", "13:30"）
     * @param period "AM" または "PM"
     * @return 指定された時間帯と一致するか（true/false）
     *
     * 例：
     * - slot = "09:00", period = "AM" → true
     * - slot = "14:00", period = "AM" → false
     * - slot = "13:00", period = "PM" → true
     */
    private boolean matchesPeriod(String slot, String period) {
    	
        LocalTime t = LocalTime.parse(slot);  // 文字列を LocalTime に変換（"09:00" → 9時0分）

        // AM: 正午より前（0:00〜11:59）
        if ("AM".equalsIgnoreCase(period)) return t.isBefore(LocalTime.NOON);

        // PM: 正午以降（12:00〜23:59）
        else if ("PM".equalsIgnoreCase(period)) return !t.isBefore(LocalTime.NOON);

        // それ以外の period（例：null）は true 扱い（全時間帯マッチ）
        return true;
    }

}
