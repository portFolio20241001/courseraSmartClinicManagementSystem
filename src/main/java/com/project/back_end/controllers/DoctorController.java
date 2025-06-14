package com.project.back_end.controllers;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.back_end.DTO.Login;
import com.project.back_end.Entity.Doctor;
import com.project.back_end.services.CommonService;
import com.project.back_end.services.DoctorService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * <h2>DoctorController ― 医師関連 REST エンドポイント</h2>
 * <pre>
 * ・登録／更新／削除
 * ・ログイン
 * ・空き時間確認
 * ・名前・専門・時間帯でのフィルタ
 * ※ すべて JSON を返却
 * </pre>
 */
@RestController
@RequestMapping("${api.path}doctor")          // 例: /api/doctor
@RequiredArgsConstructor                      // Lombok: 依存性はコンストラクタ注入
@Slf4j
@Valid
public class DoctorController {

    /** 医師固有ロジックを扱うサービス */
    private final DoctorService doctorService;

    /** トークン検証や汎用フィルタを行う共通サービス */
    private final CommonService commonService;

    /** API パスの前置き文字列（ログ用） */
    @Value("${api.path}")
    private String apiPrefix;

    /* ------------------------------------------------------------------
     * 1) 医師の空き時間を取得
     * ----------------------------------------------------------------- */

    /**
     * 指定日の医師の空き時間を取得する。
     *
     * @param doctorId 医師 ID
     * @param date     対象日 (yyyy-MM-dd)
     * @param token    認証トークン（doctor もしくは admin ロール想定）
     * @return 空き時間リスト or エラー
     */
    @GetMapping("/availability/{doctorId}/{date}/{token}")
    public ResponseEntity<Map<String, Object>> getDoctorAvailability(
            @PathVariable Long doctorId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @PathVariable String token) {

        /* ---- ① トークン検証（doctor / admin）---- */
        ResponseEntity<Map<String, String>> auth =
                commonService.validateToken(token, "doctor");   // doctor または admin で OK としたい場合は共通サービス側で判定

        if (auth.getBody() != null && !auth.getBody().isEmpty()) {
            // 認証エラーをそのまま返す
            return ResponseEntity.status(auth.getStatusCode())
                                 .body(new HashMap<>(auth.getBody()));
        }

        /* ---- ② 空き時間取得 ---- */
        Map<String, Object> body = new HashMap<>();
        
        body.put("availableTimes",
                 doctorService.getDoctorAvailability(doctorId, date));
        
        return ResponseEntity.ok(body);
    }

    /* ------------------------------------------------------------------
     * 2) すべての医師を取得
     * ----------------------------------------------------------------- */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getDoctors() {
    	
    	log.info("★ GET /doctor  全医師取得リクエスト受信");
    	
        // Service から ResponseEntity を直接受け取り、そのまま応答
        return doctorService.getDoctors();
        
    }

    /* ------------------------------------------------------------------
     * 3) 医師登録（Admin 権限）
     * ----------------------------------------------------------------- */
    @PostMapping("/{token}")
    public ResponseEntity<Map<String, String>> saveDoctor(
            @RequestBody @Valid Doctor doctor,
            @PathVariable String token) {

        /* ---- トークン検証（admin）---- 
         * 
         * commonService.validateToken(token, "admin") で JWT を解析し、「有効な admin トークンか」をチェック。
		 * 戻り値は ResponseEntity<Map<String,String>> で、
		 * 成功時 ── body: 空 or null  •  status: 200 OK
		 * 失敗時 ── body: {"error": "...メッセージ"} • status: 401 など
         * 
         * */
        ResponseEntity<Map<String, String>> auth = commonService.validateToken(token, "admin");
        
        if (auth.getBody() != null && !auth.getBody().isEmpty()) {
            return auth;
        }

        /* ---- 登録処理 ---- */
        Map<String, String> body = new HashMap<>();
        
        int result = doctorService.saveDoctor(doctor);

        if (result == -1) {
            body.put("error", "同じユーザー名の医師が既に存在します。");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
        } else if (result == 0) {
            body.put("error", "内部エラーが発生しました。");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
        }

        body.put("message", "医師を登録しました。");
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    /* ------------------------------------------------------------------
     * 4) 医師ログイン
     * ----------------------------------------------------------------- */
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> doctorLogin(
            @RequestBody @Valid Login login) {

        log.info("★ 医師ログイン要求: username={}", login.getUsername());
        
        return doctorService.doctorLogin(login);
    }

    /* ------------------------------------------------------------------
     * 5) 医師情報更新（Admin 権限）
     * ----------------------------------------------------------------- */
    @PutMapping("/{token}")
    public ResponseEntity<Map<String, String>> updateDoctor(
            @RequestBody @Valid Doctor doctor,
            @PathVariable String token) {

        ResponseEntity<Map<String, String>> auth = commonService.validateToken(token, "admin");
        
        if (auth.getBody() != null && !auth.getBody().isEmpty()) {
            return auth;
        }
        
        return doctorService.updateDoctor(doctor);
    }

    /* ------------------------------------------------------------------
     * 6) 医師削除（Admin 権限）
     * ----------------------------------------------------------------- */
    @DeleteMapping("/{doctorId}/{token}")
    public ResponseEntity<Map<String, String>> deleteDoctor(
            @PathVariable Long doctorId,
            @PathVariable String token) {

        ResponseEntity<Map<String, String>> auth = commonService.validateToken(token, "admin");
        if (auth.getBody() != null && !auth.getBody().isEmpty()) {
            return auth;
        }
        return doctorService.deleteDoctor(doctorId);
    }

    /* ------------------------------------------------------------------
     * 7) 医師フィルタ（名前・専門・AM/PM）
     * ----------------------------------------------------------------- */
    @GetMapping("/filter/{name}/{specialty}/{period}")
    public ResponseEntity<Map<String, Object>> filterDoctors(
            @PathVariable String name,
            @PathVariable String specialty,
            @PathVariable String period) {

        Map<String, Object> body = new HashMap<>();
        body.put("doctors",
                 doctorService.filterDoctorsByNameSpecilityandTime(name, specialty, period));
        return ResponseEntity.ok(body);
    }
}
