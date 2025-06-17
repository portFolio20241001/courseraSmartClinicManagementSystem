package com.project.back_end.controllers;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.back_end.DTO.AppointmentDTO;
import com.project.back_end.DTO.Login;
import com.project.back_end.Entity.Patient;
import com.project.back_end.services.CommonService;
import com.project.back_end.services.PatientService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

    /* =====================================================================
     * 1. 患者基本情報取得
     * ===================================================================*/
    @Operation(
    	    summary     = "トークンから患者情報を取得 (by Patient)",
    	    description = "JWT に含まれる <code>username</code> をキーに患者情報を返します。",
    	    parameters  = @Parameter(
    	        name        = "token",
    	        description = "患者 JWT",
    	        example     = "eyJhbGciOiJIUzI1NiJ9.patientTokenSig"
    	    )
    	)
    	@ApiResponses({
    	    @ApiResponse(
    	        responseCode = "200",
    	        description  = "取得成功",
    	        content      = @Content(
    	            examples = @ExampleObject(
    	                name  = "成功レスポンス",
    	                value = """
    	                        {
    	                          "patient": {
    	                            "id": 12,
    	                            "user": {
    	                              "id": 12,
    	                              "username": "patientUser1",
    	                              "fullName": "松本 綾香",
    	                              "role": "ROLE_PATIENT"
    	                            },
    	                            "address": "東京都新宿区西新宿2-8-1",
    	                            "dateOfBirth": "1990-01-15",
    	                            "gender": "male",
    	                            "phone": "080-1234-0012",
    	                            "createdAt": "2025-06-10T19:31:14"
    	                          }
    	                        }"""
    	            ),
    	            schema = @Schema(example = "{\"patient\":{...}}")   // Schema 表示が崩れないよう簡易指定
    	        )
    	    ),
    	    @ApiResponse(
    	        responseCode = "404",
    	        description  = "username に紐づく患者が存在しない",
    	        content      = @Content(
    	            examples = @ExampleObject(
    	                value = "{\"error\":\"該当する患者が見つかりません\"}"
    	            )
    	        )
    	    ),
    	    @ApiResponse(
    	        responseCode = "401",
    	        description  = "トークンが無効 / 期限切れ",
    	        content      = @Content(
    	            examples = @ExampleObject(
    	                value = "{\"error\":\"トークンが無効です。\"}"
    	            )
    	        )
    	    )
    	})
    
    /*
     * JWT トークンから患者の基本情報を取得する。
     *
     * @param token  患者トークン
     * @return <b>patient</b> キーで {@link Patient} を返却
     */
    @GetMapping("/{token}")
    public ResponseEntity<Map<String, Object>> getPatient(@PathVariable String token) {

        /* ① トークン検証（patient ロールのみ許可） */
    	Optional<String> hasError = commonService.validateToken(token, "patient");  

        System.out.println("ポイント1");
        
        if (hasError.isPresent()) {
            // 認証エラーをそのまま返す
        	return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", hasError.get()));
        }

        /* ② Service から患者情報を取得して返却 */
        return patientService.getPatientDetails(token);
    }

    /* =========================================================================
     * 2. 患者登録（サインアップ）
     * ========================================================================= */
    @Operation(
    	    summary     = "患者新規登録（サインアップ）",
    	    description = "重複チェックを行ったうえで新しい患者レコードを作成します。",
    	    requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(   // ← fully-qualified
    	        required = true,
    	        content  = @Content(
    	            schema   = @Schema(implementation = Patient.class),
    	            examples = @ExampleObject(
    	                name  = "新規患者リクエスト",
    	                value = """
    	                        {
    	                          "user": {
    	                            "username": "patientUser11",
    	                            "passwordHash": "plainPassword",
    	                            "fullName": "高田 海斗",
    	                            "role": "ROLE_PATIENT"
    	                          },
    	                          "address": "東京都港区芝公園4-2-8",
    	                          "dateOfBirth": "1994-08-19",
    	                          "gender": "male",
    	                          "phone": "080-1234-0022"
    	                        }"""
    	            )
    	        )
    	    ),
    	    responses = {
    	        @ApiResponse(
    	            responseCode = "201",
    	            description  = "登録成功",
    	            content      = @Content(
    	                examples = @ExampleObject(
    	                    value = "{\"message\":\"患者を登録しました。\"}"
    	                )
    	            )
    	        ),
    	        @ApiResponse(
    	            responseCode = "409",
    	            description  = "同一ユーザー名または電話番号が既に存在",
    	            content      = @Content(
    	                examples = @ExampleObject(
    	                    value = "{\"error\":\"このユーザー名または電話番号は既に登録されています。\"}"
    	                )
    	            )
    	        ),
    	        @ApiResponse(
    	            responseCode = "500",
    	            description  = "内部エラー（DB 例外など）",
    	            content      = @Content(
    	                examples = @ExampleObject(
    	                    value = "{\"error\":\"内部エラーが発生しました。\"}"
    	                )
    	            )
    	        )
    	    }
    	)
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
    @Operation(
    	    summary     = "患者ログイン（JWT 発行）",
    	    description = "username / password を認証し、成功時に JWT を返却します。",
    	    requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
    	        required = true,
    	        content  = @Content(
    	            schema   = @Schema(implementation = Login.class),
    	            examples = @ExampleObject(
    	                name  = "LoginRequest",
    	                value = """
    	                        {
    	                          "username": "patientUser1",
    	                          "password": "plainPassword"
    	                        }"""
    	            )
    	        )
    	    ),
    	    responses = {
    	        @ApiResponse(
    	            responseCode = "200",
    	            description  = "認証成功・JWT 返却",
    	            content      = @Content(
    	                mediaType = org.springframework.http.MediaType.APPLICATION_JSON_VALUE,
    	                examples  = @ExampleObject(
    	                    value = "{\"token\":\"eyJhbGciOiJIUzI1NiJ9.patientTokenSig\"}"
    	                )
    	            )
    	        ),
    	        @ApiResponse(
    	            responseCode = "401",
    	            description  = """
    	                           <ul>
    	                             <li>ユーザー名が存在しない</li>
    	                             <li>パスワード不一致</li>
    	                           </ul>""",
    	            content      = @Content(
    	                examples = {
    	                    @ExampleObject(
    	                        name  = "UserNotFound",
    	                        value = "{\"error\":\"ユーザー名が存在しません。\"}"
    	                    ),
    	                    @ExampleObject(
    	                        name  = "PasswordMismatch",
    	                        value = "{\"error\":\"パスワードが一致しません。\"}"
    	                    )
    	                }
    	            )
    	        )
    	    }
    	)
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
    @Operation(
    	    summary     = "患者予約一覧を取得 (by Patient)",
    	    description = "patientId に紐づくすべての予約を `AppointmentDTO` 配列で返却します。",
    	    parameters  = {
    	        @Parameter(
    	            name        = "id",
    	            description = "患者 ID",
    	            example     = "12",
    	            required    = true,
    	            in          = ParameterIn.PATH,
    	            schema      = @Schema(type = "integer", format = "int64")
    	        ),
    	        @Parameter(
    	            name        = "token",
    	            description = "患者の JWT",
    	            example     = "eyJhbGciOiJIUzI1NiJ9.patientTokenSig",
    	            required    = true,
    	            in          = ParameterIn.PATH,
    	            schema      = @Schema(type = "string")
    	        )
    	    },
    	    responses = {
    	        @ApiResponse(
    	            responseCode = "200",
    	            description  = "取得成功",
    	            content      = @Content(
    	                mediaType = org.springframework.http.MediaType.APPLICATION_JSON_VALUE,
    	                array     = @ArraySchema(schema = @Schema(implementation = AppointmentDTO.class)),
    	                examples  = @ExampleObject(
    	                    name  = "AppointmentsForPatient12",
    	                    value = """
    	                            [
    	                              {
    	                                "id": 1,
    	                                "doctorId": 2,
    	                                "patientId": 12,
    	                                "appointmentTime": "2025-09-11T09:00:00",
    	                                "status": 0,
    	                                "payment": null
    	                              },
    	                              {
    	                                "id": 11,
    	                                "doctorId": 4,
    	                                "patientId": 12,
    	                                "appointmentTime": "2025-09-11T15:00:00",
    	                                "status": 1,
    	                                "payment": null
    	                              },
    	                              {
    	                                "id": 21,
    	                                "doctorId": 7,
    	                                "patientId": 12,
    	                                "appointmentTime": "2025-09-12T13:00:00",
    	                                "status": 2,
    	                                "payment": null
    	                              },
    	                              {
    	                                "id": 31,
    	                                "doctorId": 9,
    	                                "patientId": 12,
    	                                "appointmentTime": "2025-09-13T11:30:00",
    	                                "status": 0,
    	                                "payment": null
    	                              }
    	                            ]"""
    	                )
    	            )
    	        ),
    	        @ApiResponse(
    	            responseCode = "401",
    	            description  = "トークン検証失敗",
    	            content      = @Content(
    	                examples = @ExampleObject(
    	                    value = "{\"error\":\"トークンが無効です。\"}"
    	                )
    	            )
    	        ),
    	        @ApiResponse(
    	            responseCode = "500",
    	            description  = "内部エラー",
    	            content      = @Content(
    	                examples = @ExampleObject(
    	                    value = "{\"error\":\"予約取得に失敗しました\"}"
    	                )
    	            )
    	        )
    	    }
    	)
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

    	Optional<String> hasError = commonService.validateToken(token, "patient");  

        System.out.println("ポイント1");
        
        if (hasError.isPresent()) {
            // 認証エラーをそのまま返す
        	return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", hasError.get()));
        }
        
        return patientService.getPatientAppointment(id);
    }

    /* =========================================================================
     * 5. 患者の予約フィルタ
     * ========================================================================= */
    @Operation(
    	    summary     = "予約履歴をフィルタ (by Patient)",
    	    description = """
    	        <p>患者自身の予約を <code>condition</code>（<code>past / future / cancel </code>）や
    	        医師名（部分一致）で絞り込みます。<br>
    	        いずれも <code>null</code> または空文字を渡すとその条件は無視されます。</p>
    	        <ul>
    	          <li><b>condition</b> … <code>past</code>= <code>status=1</code> / <code>future</code>=<code>status=0</code></li>
    	          <li><b>name</b>      … 医師 User.fullName の部分一致</li>
    	        </ul>
    	        """,
    	    parameters  = {
    	        @Parameter(
    	            name        = "condition",
    	            description = "\"past\" / \"future\" / \"cancel\" / \"null\"",
    	            example     = "future",
    	            in          = ParameterIn.PATH,
    	            schema      = @Schema(type = "string")
    	        ),
    	        @Parameter(
    	            name        = "name",
    	            description = "医師名（部分一致）",
    	            example     = "鈴木",
    	            in          = ParameterIn.PATH,
    	            schema      = @Schema(type = "string")
    	        ),
    	        @Parameter(
    	            name        = "token",
    	            description = "患者 JWT",
    	            example     = "eyJhbGciOiJIUzI1NiJ9.patientTokenSig",
    	            required    = true,
    	            in          = ParameterIn.PATH,
    	            schema      = @Schema(type = "string")
    	        )
    	    },
    	    responses = {
    	        @ApiResponse(
    	            responseCode = "200",
    	            description  = "フィルタ結果",
    	            content      = @Content(
    	                mediaType = org.springframework.http.MediaType.APPLICATION_JSON_VALUE,
    	                schema    = @Schema(                            // {"data":[...]}
    	                    type  = "object",
    	                    example = "{\n  \"data\": [ /* see below */ ]\n}"
    	                ),
    	                examples = @ExampleObject(
    	                    name  = "FutureAppointmentsBySuzuki",
    	                    summary = "patientId=12 の future & \"鈴木\" でフィルタ",
    	                    description = "status=0 (future) かつ 医師『鈴木 花子』の予約2件",
    	                    value = """
    	                        {
    	                          "data": [
    	                            {
    	                              "id": 1,
    	                              "doctorId": 2,
    	                              "patientId": 12,
    	                              "appointmentTime": "2025-09-11T09:00:00",
    	                              "status": 0,
    	                              "payment": null
    	                            },
    	                            {
    	                              "id": 31,
    	                              "doctorId": 9,
    	                              "patientId": 12,
    	                              "appointmentTime": "2025-09-13T11:30:00",
    	                              "status": 0,
    	                              "payment": null
    	                            }
    	                          ]
    	                        }"""
    	                )
    	            )
    	        ),
    	        @ApiResponse(
    	            responseCode = "401",
    	            description  = "トークン検証失敗",
    	            content      = @Content(
    	                examples = @ExampleObject(
    	                    value = "{\"error\":\"トークンが無効または期限切れです。\"}"
    	                )
    	            )
    	        ),
    	        @ApiResponse(
    	            responseCode = "404",
    	            description  = "患者レコードが見つからない",
    	            content      = @Content(
    	                examples = @ExampleObject(
    	                    value = "{\"error\":\"患者情報が見つかりません。\"}"
    	                )
    	            )
    	        ),
    	        @ApiResponse(
    	            responseCode = "500",
    	            description  = "内部エラー",
    	            content      = @Content(
    	                examples = @ExampleObject(
    	                    value = "{\"error\":\"内部エラーが発生しました。\"}"
    	                )
    	            )
    	        )
    	    }
    	)
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
        
        
    	Optional<String> hasError = commonService.validateToken(token, "patient");  

        System.out.println("ポイント1");
        
        if (hasError.isPresent()) {
            // 認証エラーをそのまま返す
        	return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", hasError.get()));
        }
        
        return commonService.filterPatient(condition, name, token);
    }
}
