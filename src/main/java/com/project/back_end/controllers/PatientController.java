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

import com.project.back_end.DTO.Login;
import com.project.back_end.Entity.Patient;
import com.project.back_end.services.CommonService;
import com.project.back_end.services.PatientService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * <h2>PatientController ― 患者関連 REST エンドポイント</h2>
 *
 * <pre>
 * 役割
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 * ■ 患者登録（サインアップ）
 * ■ 患者ログイン（JWT 発行）
 * ■ トークンから患者基本情報を取得
 * ■ 患者予約の取得／フィルタ
 * </pre>
 *
 * @author back_end team
 */
@RestController
@RequestMapping("/patient")      // 例: /patient/**
@RequiredArgsConstructor         // Lombok: 依存性をコンストラクタ注入
@Valid
@Slf4j
public class PatientController {

    /*─────────────────────────────────── 依存サービス ───────────────────────────────────*/
    /** 患者固有ロジック（登録・予約取得など） */
    private final PatientService patientService;

    /** トークン検証や共通バリデーションを行うサービス */
    private final CommonService  commonService;

    /* =========================================================================
     * 1. 患者基本情報の取得
     * ========================================================================= */
    /**
     * JWT トークンから患者の基本情報を取得する。
     *
     * @param token  患者トークン
     * @return <b>patient</b> キーで {@link Patient} を返却
     */
    @GetMapping("/{token}")
    public ResponseEntity<Map<String, Object>> getPatient(@PathVariable String token) {

        /* ① トークン検証（patient ロールのみ許可） */
        ResponseEntity<Map<String, String>> auth =
                commonService.validateToken(token, "patient");

        if (auth.getBody() != null && !auth.getBody().isEmpty()) {
            // 認証失敗時はそのままエラーを返却
            return ResponseEntity.status(auth.getStatusCode())
                                 .body(new HashMap<>(auth.getBody()));
        }

        /* ② Service から患者情報を取得して返却 */
        return patientService.getPatientDetails(token);
    }

    /* =========================================================================
     * 2. 患者登録（サインアップ）
     * ========================================================================= */
    /**
     * 新規患者を登録する。
     *
     * @param patient 登録する {@link Patient}
     * @return 登録結果メッセージ
     */
    @PostMapping
    @Transactional
    public ResponseEntity<Map<String, String>> createPatient(@RequestBody @Valid Patient patient) {

        log.info("★ 患者サインアップ要求: username={}", patient.getUser().getUsername());

        /* ① 重複チェック */
        if (!commonService.validatePatient(patient)) {           // 既に存在する場合 false
            Map<String, String> body = new HashMap<>();
            body.put("error", "このユーザー名または電話番号は既に登録されています。");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
        }

        /* ② 登録処理 */
        return patientService.createPatient(patient);
    }

    /* =========================================================================
     * 3. 患者ログイン
     * ========================================================================= */
    /**
     * 患者ログインを行い、成功時は JWT トークンを返す。
     *
     * @param login {@link Login}（username / password）
     * @return token or error
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody @Valid Login login) {

        log.info("★ 患者ログイン要求: username={}", login.getUsername());
        
        return commonService.validatePatientLogin(login);
        
    }

    /* =========================================================================
     * 4. 患者の予約取得
     * ========================================================================= */
    /**
     * 指定患者の予約一覧を取得する。
     *
     * @param id    患者 ID
     * @param token 認証トークン（patient ロール）
     * @return 患者の予約リスト
     */
    @GetMapping("/appointments/{id}/{token}")
    public ResponseEntity<?> getPatientAppointment(
            @PathVariable Long id,
            @PathVariable String token) {

        ResponseEntity<Map<String, String>> auth = commonService.validateToken(token, "patient");
        if (auth.getBody() != null && !auth.getBody().isEmpty()) {
            return auth;
        }
        return patientService.getPatientAppointment(id);
    }

    /* =========================================================================
     * 5. 患者の予約フィルタ
     * ========================================================================= */
    /**
     * 患者予約を状態や医師名でフィルタする。
     *
     * @param condition past / future など
     * @param name      医師名（部分一致）
     * @param token     認証トークン
     * @return フィルタ結果
     */
    @GetMapping("/appointments/filter/{condition}/{name}/{token}")
    public ResponseEntity<?> filterPatientAppointment(
            @PathVariable String condition,
            @PathVariable String name,
            @PathVariable String token) {

        log.debug("患者予約フィルタ: condition={}, name={}", condition, name);
        return commonService.filterPatient(condition, name, token);
    }
}
