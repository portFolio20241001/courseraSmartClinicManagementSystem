package com.project.back_end.services;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
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
import com.project.back_end.Entity.User;
import com.project.back_end.repo.AppointmentRepository;
//import com.project.back_end.services.TokenService;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Service  // 1. サービス層コンポーネントとして登録
@RequiredArgsConstructor  // 2. コンストラクタインジェクションを自動生成
@Slf4j  // ログ出力用アノテーション
public class DoctorService {

    private final UserRepository userRepository;
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
    	
    	log.info("★ ポイント1");
    	
    	// 例外処理は呼び出し側でまとめて行いたいので、ここではそのまま返すだけ
    	List<Doctor> doctorList =doctorRepository.findAllWithUser();
    	
    	for (Doctor doctor : doctorList) {
    	    log.info("★ doctor.toString: {}", doctor.toString());
    	}
    	
        
        return doctorList;
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
            
        	log.info("★ ポイント2");
            
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
    	
    	System.out.println("◆ 入力 date: " + date); // ← 追加

        // 1) 医師存在チェック
        Doctor doctor = doctorRepository.findById(doctorId)
                                        .orElse(null);
        
        if (doctor == null) return Collections.emptyList();

        // 2) その日 0:00〜23:59 で既予約を取得
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay   = date.atTime(LocalTime.MAX);
        
        System.out.println("◆ startOfDay: " + startOfDay);
        System.out.println("◆ endOfDay: " + endOfDay);
        
        // 3. 指定日の予約を取得
        List<Appointment> booked = appointmentRepository
                .findByDoctorIdAndAppointmentTimeBetween(doctorId, startOfDay, endOfDay);
        
        Set<String> bookedStartTimes = booked.stream()
                .map(a -> a.getAppointmentTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                .collect(Collectors.toSet());
        
        
        System.out.println("bookedStartTimes: " + bookedStartTimes);
        

            // 4. 利用可能スロットから予約済を除外（指定日でフィルタ）
            return doctor.getAvailableTimes().stream()
//                .filter(slot -> {
//                    // 例: "2025-06-11 09:00-10:00" の開始部分を取り出す
//                    String[] parts = slot.split("-");
//                    String startTimeStr = parts[0]; // "2025-06-11 09:00"
//
//                    // 指定日のみ対象
//                    if (!startTimeStr.startsWith(date.toString())) {
//                        return false;
//                    }
//
//                    // 予約済みかどうか
//                    return !bookedStartTimes.contains(startTimeStr);
//                })
//                .toList();
            
     	    .filter(slot -> {
     	        // スロット例: "2025-06-11 09:00-10:00"
     	        String[] dateAndTime = slot.split(" "); // ["2025-06-11", "09:00-10:00"]
     	        if (dateAndTime.length != 2) return false;

     	        String datePart = dateAndTime[0];        // "2025-06-11"
     	        String timeRange = dateAndTime[1];       // "09:00-10:00"
     	        String startTime = timeRange.split("-")[0]; // "09:00"
     	        String startDateTimeStr = datePart + " " + startTime; // "2025-06-11 09:00"
                
                System.out.println("startDateTimeStr:"+startDateTimeStr);
                

                // パースしてLocalDateを比較する
       	        LocalDate slotDate = LocalDate.parse(datePart);

                // 日付が一致し、予約されていないものだけ
                return slotDate.equals(date) && !bookedStartTimes.contains(startDateTimeStr);
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
            // ユーザー名が既に存在するか確認
            if (doctorRepository.existsByUser_Username(doctor.getUser().getUsername())) {
                return -1;  // ユーザー名重複
            }

            // パスワードのハッシュ化（必要なら）
            String rawPassword = doctor.getUser().getPasswordHash();
            String hashedPassword = passwordEncoder.encode(rawPassword);
            doctor.getUser().setPasswordHash(hashedPassword);

            // ロールの明示的設定（安全性のため）
            doctor.getUser().setRole(User.Role.ROLE_DOCTOR);

            // 双方向関連の明示（必要なら）
            doctor.getUser().setDoctor(doctor);

            // 保存
            doctorRepository.save(doctor);  // CascadeType.ALL により user も保存される

            return 1;  // 成功
            
        } catch (Exception e) {
        	
            log.error("Doctor登録エラー: {}", e.getMessage(), e);
            return 0;  // 内部エラー
            
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
    	
    	System.out.println("USER: " + doctor.getUser().getFullName());

        Map<String, String> body = new HashMap<>();
        
        try {
        
	        /* --- doctorId nullチェック -------------------- */
	        if (doctor.getId() == null) {
	            throw new IllegalArgumentException("Doctor IDを更新するならIDがNULLはダメです。");
	        }

	        /* --- 1. 存在チェック -------------------- */
	        if (!doctorRepository.existsById(doctor.getId())) {
	            body.put("error", "指定 ID の医師は存在しません。");
	            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
	        }
	        
	        System.out.println("doctor.getId():" + doctor.getId());
	        
	        Doctor existingDoctor = doctorRepository.findByUser_UserId(doctor.getId())
	                .orElseThrow(() -> new RuntimeException("ユーザーが存在しません"));

	        // 既存のUserを取得して上書き
	        User existingUser = existingDoctor.getUser();

	        existingUser.setUsername(doctor.getUser().getUsername());
	        existingUser.setPasswordHash(passwordEncoder.encode(doctor.getUser().getPasswordHash()));
	        existingUser.setFullName(doctor.getUser().getFullName());
	        existingUser.setRole(User.Role.ROLE_DOCTOR); // 明示的にセット
	        existingUser.setDoctor(existingDoctor);      // 双方向の整合性

	        // doctor に既存 user を再セット（←ここ重要）
	        doctor.setUser(existingUser);
            
            System.out.println("あああ");
            System.out.println("あdoctor："+ doctor);
            

        	/* --- 2. 更新処理 ----------------------- */

            doctorRepository.save(doctor);
            body.put("message", "医師情報を更新しました。");
            return ResponseEntity.ok(body);

        } catch (Exception e) {
            log.error("Doctor 更新失敗 : {}", e.getMessage());
            body.put("error", e.getMessage());
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

        
        System.out.println("eee");
        
        /* --- 1. 存在チェック -------------------------------- */
        if (!doctorRepository.existsById(doctorId)) {
            body.put("error", "指定 ID の医師は存在しません。");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
        }
        
        System.out.println("fff");

        /* --- 2. 予約 & 医師レコード削除 --------------------- */
        try {
        	
            System.out.println("ggg");
        	
            appointmentRepository.deleteAllByDoctorId(doctorId);  // 先に外側を削除
            
            doctorRepository.deleteById(doctorId);
            
            userRepository.deleteById(doctorId);  // ← ここで User も削除

            body.put("message", "医師(および紐づく予定履歴)を削除しました。");
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
                
                System.out.println("login.getPassword():" + "[" + login.getPassword() + "]");
                System.out.println("doctor.getUser().getPasswordHash():" + doctor.getUser().getPasswordHash());

                
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
    	
        // null または "null" の扱いを共通で処理
    	String safeName = (name == null || name.equalsIgnoreCase("null")) ? "" : name;
    	String safeSpecialty = (specialty == null || specialty.equalsIgnoreCase("null")) ? "" : specialty;


        System.out.println("safeName:" + safeName);
        System.out.println("specialty:" + safeSpecialty);
        
        
        // 名前 or 専門が空なら全件検索、そうでなければ条件付きで検索
        List<Doctor> list;
        if (safeName.isEmpty() && safeSpecialty.isEmpty()) {
            list = doctorRepository.findAll(); // 全件
        } else if (safeName.isEmpty()) {
            list = doctorRepository.findBySpecialtyIgnoreCase(safeSpecialty);
        } else if (safeSpecialty.isEmpty()) {
            list = doctorRepository.findByFullNameContainingIgnoreCase(safeName);
        } else {
            list = doctorRepository.findByFullNameContainingIgnoreCaseAndSpecialtyIgnoreCase(safeName, safeSpecialty);
        }
    	
        // 名前＋専門分野でまず絞り込む
//        List<Doctor> list = doctorRepository.findByFullNameContainingIgnoreCaseAndSpecialtyIgnoreCase(safeName, safeSpecialty);
        
        System.out.println("list:" + list);
        
        // 午前 or 午後でさらにフィルター
        List<Doctor> filterdList = filterDoctorByTime(list, period);
        
        System.out.println("filterdList:" + filterdList);
        
        return filterdList;
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
                .map(d -> {
                    // 条件にマッチした availableTimes のみを残す
                    List<String> filteredSlots = d.getAvailableTimes().stream()
                        .filter(slot -> matchesPeriod(slot, period))
                        .collect(Collectors.toList());

                    // availableTimes を上書き（破壊的変更）
                    d.setAvailableTimes(filteredSlots);

                    return d;
                })
                .filter(d -> !d.getAvailableTimes().isEmpty()) // フィルター後の availableTimes が空でない医師のみ返す

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
    	
    	try {
	    	// "2025-06-11 09:00" 形式 → LocalDateTime にパース
	    	// 先頭16文字だけを取り出す → "2025-06-20 09:00"
	        String start = slot.substring(0, 16);
	        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
	        LocalTime time = LocalDateTime.parse(start, formatter).toLocalTime();
	        
	        System.out.println("LocalTime:" + time );
	
	        // AM: 正午より前（0:00〜11:59）
	        if ("AM".equalsIgnoreCase(period)) return time.isBefore(LocalTime.NOON);
	
	        // PM: 正午以降（12:00〜23:59）
	        else if ("PM".equalsIgnoreCase(period)) return !time.isBefore(LocalTime.NOON);
	
	        // それ以外の period（例：null）は true 扱い（全時間帯マッチ）
	        return true;
	        
    	} catch (DateTimeParseException | StringIndexOutOfBoundsException e) {
    		
            // フォーマット不正などで例外が出た場合は無視して false 扱い
            System.err.println("パースエラー: " + slot);
            return false;
            
        }
    	
    }

}
