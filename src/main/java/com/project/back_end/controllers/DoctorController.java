package com.project.back_end.controllers;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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

    /* =====================================================================
     * 1) 医師の空き時間を取得
     * ===================================================================*/
    // SwaggerUI表示
    @Operation(
            summary = "医師の空き時間を取得 (by Patient)",
            description = "指定した <b>doctorId</b> と <b>date</b> について、未予約の時間帯リストを返します。",
            parameters = {
                @Parameter(
                    name = "doctorId",
                    description = "医師ID",
                    example = "2"
                ),
                @Parameter(
                    name = "date",
                    description = "対象日 (YYYY-MM-DD)",
                    example = "2025-09-11"
                ),
                @Parameter(
                    name = "token",
                    description = "JWT トークン（ROLE_DOCTOR）",
                    example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJkb2N0b3JVc2VyMSIsImlhdCI6MTY5NjM0NTYwMCwiZXhwIjoyMDExMjQ1NjAwfQ.dummySignature"
                )
            },
            responses = {
                @ApiResponse(
                    responseCode = "200", description = "取得成功",
                    content = @Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        examples = @ExampleObject(value = """
                            {
                              "availableTimes": [
                                "09:00-10:00",
                                "11:00-12:00",
                                "15:00-16:00"
                              ]
                            }""")
                    )
                ),
                @ApiResponse(
        	            responseCode = "401",
        	            description = "トークン無効",
        	            content = @Content(
        	                mediaType = "application/json",
        	                examples = @ExampleObject(
        	                    name = "無効トークン",
        	                    value = """
        	                        {
        	                          "error": "トークンが無効です"
        	                        }"""
        	                )
        	            )
        	        )
            }
        )
    /**
     * 指定日の医師の空き時間を取得する。
     *
     * @param doctorId 医師 ID
     * @param date     対象日 (yyyy-MM-dd)
     * @param token    認証トークン（patient ロール想定）
     * @return 空き時間リスト or エラー
     */
    @GetMapping("/availability/{doctorId}/{date}/{token}")
    public ResponseEntity<Map<String, Object>> getDoctorAvailability(
            @PathVariable Long doctorId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @PathVariable String token) {
    	
    	
    	System.out.println("@GetMapping(\"/availability/{doctorId}/{date}/{token}\")開始");
    	
        /* ---- ① トークン検証（patient）---- */
    	Optional<String> hasError = commonService.validateToken(token, "patient");  

        System.out.println("ポイント1");
        
        if (hasError.isPresent()) {
            // 認証エラーをそのまま返す
        	return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", hasError.get()));
        }
        
        System.out.println("① トークン検証（patient）通過");

        /* ---- ② 空き時間取得 ---- */
        Map<String, Object> body = new HashMap<>();
        
        
        body.put("message", "トークンは有効です。");
        body.put("availableTimes",
                 doctorService.getDoctorAvailability(doctorId, date));
        
        System.out.println("② トークン検証（patient）通過");
        
        return ResponseEntity.ok(body);
    }

    /* =====================================================================
     * 2) すべての医師を取得
     * ===================================================================*/
    @Operation(
        summary = "全ての医師を取得",
        description = "登録されている医師レコードをすべて返します。",
        responses = @ApiResponse(
            responseCode = "200",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                examples = @ExampleObject(value = """
                    {
                      "doctors": [
                        {
                          "id": 2,
                          "specialty": "心臓内科",
                          "phone": "080-1111-0002",
                          "clinicLocation": {
                            "id": 1,
                            "name": "中央クリニック"
                          }
                        },
                        {
                          "id": 3,
                          "specialty": "神経内科",
                          "phone": "080-1111-0003",
                          "clinicLocation": {
                            "id": 1,
                            "name": "中央クリニック"
                          }
                        }
                      ]
                    }""")
            )
        )
    )

    @GetMapping
    @Transactional
    public ResponseEntity<Map<String, Object>> getDoctors() {
    	
    	log.info("★ GET /doctor  全医師取得リクエスト受信");
    	
        // Service から ResponseEntity を直接受け取り、そのまま応答
        return doctorService.getDoctors();
        
    }

    /* =====================================================================
     * 3) 医師登録（Admin）
     * ===================================================================*/
    @Operation(
    	    summary = "医師登録（by Admin）",
    	    description = "Admin が新しい医師を登録します。",
    	    requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
    	        required = true,
    	        content = @Content(
    	            schema = @Schema(implementation = Doctor.class),
    	            examples = @ExampleObject(name = "doctorRequest", value = """
    	                {
    	                  "specialty": "小児科",
    	                  "phone": "090-1234-5678",
    	                  "clinicLocation": { "id": 1 },
    	                  "availableTimes": [
    	                    "2025-06-20 09:00-10:00",
    	                    "2025-06-21 14:00-15:00"
    	                  ],
    	                  "user": {
    	                    "username": "doctorUser11",
    	                    "passwordHash": "docpass11",
    	                    "role": "ROLE_DOCTOR",
    	                    "fullName": "佐藤 太郎"
    	                  }
    	                }""")
    	        )
    	    ),
    	    parameters = @Parameter(
    	        name = "token",
    	        description = "Admin の JWT トークン",
    	        example = "eyJhbGciOiJIUzI1NiJ9.adminTokenSignature"
    	    ),
    	    responses = {
    	        @ApiResponse(
    	            responseCode = "201",
    	            description = "登録成功",
    	            content = @Content(
    	                mediaType = "application/json",
    	                examples = @ExampleObject(
    	                    name = "登録成功",
    	                    value = """
    	                        {
    	                          "message": "医師を登録しました。"
    	                        }"""
    	                )
    	            )
    	        ),
    	        @ApiResponse(
    	            responseCode = "409",
    	            description = "重複ユーザーあり",
    	            content = @Content(
    	                mediaType = "application/json",
    	                examples = @ExampleObject(
    	                    name = "重複ユーザー",
    	                    value = """
    	                        {
    	                          "error": "同じユーザー名の医師が既に存在します。"
    	                        }"""
    	                )
    	            )
    	        ),
    	        @ApiResponse(
    	            responseCode = "401",
    	            description = "トークン無効",
    	            content = @Content(
    	                mediaType = "application/json",
    	                examples = @ExampleObject(
    	                    name = "無効トークン",
    	                    value = """
    	                        {
    	                          "error": "トークンが無効です"
    	                        }"""
    	                )
    	            )
    	        )
    	    }
    	)

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
    	Optional<String> hasError = commonService.validateToken(token, "admin");  

        System.out.println("ポイント1");
        
        if (hasError.isPresent()) {
            // 認証エラーをそのまま返す
        	return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", hasError.get()));
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

    /* =====================================================================
     * 4) 医師ログイン
     * ===================================================================*/
    @Operation(
    	    summary = "医師ログイン",
    	    description = "ユーザー名とパスワードでログインし、JWT トークンを取得します。",
    	    requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
    	        required = true,
    	        content = @Content(
    	            schema = @Schema(implementation = Login.class),
    	            examples = @ExampleObject(
    	                name = "ログイン例",
    	                value = """
    	                    {
    	                      "username": "doctorUser1",
    	                      "password": "password123"
    	                    }"""
    	            )
    	        )
    	    ),
    	    responses = {
    	        @ApiResponse(
    	            responseCode = "200",
    	            description = "ログイン成功",
    	            content = @Content(
    	                mediaType = MediaType.APPLICATION_JSON_VALUE,
    	                examples = @ExampleObject(
    	                    name = "成功例",
    	                    value = """
    	                        {
    	                          "token": "eyJhbGciOiJIUzI1NiJ9.generatedToken",
    	                          "message": "ログインに成功しました。"
    	                        }"""
    	                )
    	            )
    	        ),
    	        @ApiResponse(
    	        	    responseCode = "401",
    	        	    description = "認証失敗（ユーザー名不一致 または パスワード不一致）",
    	        	    content = @Content(
    	        	        mediaType = MediaType.APPLICATION_JSON_VALUE,
    	        	        examples = {
    	        	            @ExampleObject(
    	        	                name = "ユーザー名不一致",
    	        	                value = """
    	        	                    {
    	        	                      "error": "ユーザー名が存在しません。"
    	        	                    }"""
    	        	            ),
    	        	            @ExampleObject(
    	        	                name = "パスワード不一致",
    	        	                value = """
    	        	                    {
    	        	                      "error": "パスワードが一致しません。"
    	        	                    }"""
    	        	            )
    	        	        }
    	        	    )
    	        	)
    	        }
    	)
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> doctorLogin(
            @RequestBody @Valid Login login) {

        log.info("★ 医師ログイン要求: username={}", login.getUsername());
        
        return doctorService.doctorLogin(login);
    }

    /* =====================================================================
     * 5) 医師情報更新（Adminロールで更新）
     * ===================================================================*/
    @Operation(
    	    summary = "医師情報更新（Admin）",
    	    description = "Admin が医師情報（電話番号・専門分野・所属クリニックなど）を更新します。",
    	    parameters = {
    	        @Parameter(
    	            name = "token",
    	            description = "Admin の JWT",
    	            example = "eyJhbGciOiJIUzI1NiJ9.adminTokenSig"
    	        )
    	    },
    	    requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
    	        required = true,
    	        content = @Content(
    	            mediaType = "application/json",
    	            schema = @Schema(implementation = Doctor.class),
    	            examples = @ExampleObject(
    	                name = "更新例",
    	                summary = "医師情報更新（id:28）",
    	                description = "ID 28 の医師（佐藤 花子）の情報を更新する例",
    	                value = """
    	                {
    	                  "id": 28,
    	                  "specialty": "産婦人科",
    	                  "phone": "090-1234-5678",
    	                  "clinicLocation": { "id": 1 },
    	                  "availableTimes": [
    	                    "2025-06-20 09:00-10:00",
    	                    "2025-06-21 14:00-15:00"
    	                  ],
    	                  "user": {
    	                    "username": "doctorUser13",
    	                    "passwordHash": "docpass13",
    	                    "role": "ROLE_DOCTOR",
    	                    "fullName": "佐藤 花子"
    	                  }
    	                }
    	                """
    	            )
    	        )
    	    ),
    	    responses = {
    	        @ApiResponse(
    	            responseCode = "200",
    	            description = "更新成功",
    	            content = @Content(
    	                mediaType = "application/json",
    	                examples = @ExampleObject(
    	                    name = "成功レスポンス",
    	                    value = "{ \"message\": \"医師情報を更新しました。\" }"
    	                )
    	            )
    	        ),
    	        @ApiResponse(
    	            responseCode = "404",
    	            description = "該当 ID が存在しない",
    	            content = @Content(
    	                mediaType = "application/json",
    	                examples = @ExampleObject(
    	                    name = "存在しないID",
    	                    value = "{ \"error\": \"指定 ID の医師は存在しません。\" }"
    	                )
    	            )
    	        ),
    	        @ApiResponse(
    	            responseCode = "500",
    	            description = "更新リクエストの異常（ID未指定など）",
    	            content = @Content(
    	                mediaType = "application/json",
    	                examples = @ExampleObject(
    	                    name = "ID未指定エラー",
    	                    value = "{ \"error\": \"Doctor IDを更新するならIDがNULLはダメです。\" }"
    	                )
    	            )
    	        )
    	    }
    	)

    @PutMapping("/{token}")
    public ResponseEntity<Map<String, String>> updateDoctor(
            @RequestBody @Valid Doctor doctor,
            @PathVariable String token) {

    	Optional<String> hasError = commonService.validateToken(token, "admin");  

    	System.out.println("username: " + doctor.getUser().getUsername());
    	System.out.println("password: " + doctor.getUser().getPasswordHash());
    	System.out.println("fullname: " + doctor.getUser().getFullName());
    	System.out.println("role: " + doctor.getUser().getRole());
        
        if (hasError.isPresent()) {
            // 認証エラーをそのまま返す
        	return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", hasError.get()));
        }
        
        return doctorService.updateDoctor(doctor);
    }

    /* =====================================================================
     * 6) 医師削除（Admin）
     * ===================================================================*/
    @Operation(
    	    summary = "医師削除（by Admin）",
    	    description = "Admin が医師レコードを削除します。",
    	    parameters = {
    	        @Parameter(
    	            name = "doctorId",
    	            description = "削除対象の医師ID",
    	            example = "11"
    	        ),
    	        @Parameter(
    	            name = "token",
    	            description = "Admin の JWT トークン",
    	            example = "eyJhbGciOiJIUzI1NiJ9.adminTokenSig"
    	        )
    	    },
    	    responses = {
    	        @ApiResponse(
    	            responseCode = "200",
    	            description = "削除成功",
    	            content = @Content(
    	                mediaType = MediaType.APPLICATION_JSON_VALUE,
    	                examples = @ExampleObject(
    	                    name = "削除成功",
    	                    value = """
    	                        {
    	                          "message": "医師(および紐づく予定履歴)を削除しました。"
    	                        }"""
    	                )
    	            )
    	        ),
    	        @ApiResponse(
    	            responseCode = "404",
    	            description = "該当IDの医師が存在しない",
    	            content = @Content(
    	                mediaType = MediaType.APPLICATION_JSON_VALUE,
    	                examples = @ExampleObject(
    	                    name = "医師IDなし",
    	                    value = """
    	                        {
    	                          "error": "指定 ID の医師は存在しません。"
    	                        }"""
    	                )
    	            )
    	        ),
    	        @ApiResponse(
    	            responseCode = "401",
    	            description = "認証失敗（トークン不正）",
    	            content = @Content(
    	                mediaType = MediaType.APPLICATION_JSON_VALUE,
    	                examples = @ExampleObject(
    	                    name = "不正トークン",
    	                    value = """
    	                        {
    	                          "error": "トークンが無効です"
    	                        }"""
    	                )
    	            )
    	        )
    	    }
    	)

    @DeleteMapping("/{doctorId}/{token}")
    public ResponseEntity<Map<String, String>> deleteDoctor(
            @PathVariable Long doctorId,
            @PathVariable String token) {

    	Optional<String> hasError = commonService.validateToken(token, "admin");  

        System.out.println("ポイント1");
        
        if (hasError.isPresent()) {
            // 認証エラーをそのまま返す
        	return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", hasError.get()));
        }
        
        return doctorService.deleteDoctor(doctorId);
    }

    
    /* =====================================================================
     * 7) 医師フィルタ（名前・専門・AM/PM）
     * ===================================================================*/
    @Operation(
        summary = "医師フィルタ",
        description = """
            3 つのパラメータを組み合わせて医師を検索します。  
            <ul>
              <li><code>name</code>      … 医師名（部分一致）例: <i>鈴木</i></li>
              <li><code>specialty</code> … 専門分野           例: <i>心臓内科</i></li>
              <li><code>period</code>    … 午前/午後           例: <i>AM</i> または <i>PM</i></li>
            </ul>
            各値が <code>null</code>（空文字）の場合、その条件は無視されます。
            """,
        parameters = {
            @Parameter(
                name        = "name",
                description = "医師名（部分一致）",
                example     = "鈴木"
            ),
            @Parameter(
                name        = "specialty",
                description = "専門分野（完全一致）",
                example     = "心臓内科"
            ),
            @Parameter(
                name        = "period",
                description = "時間帯フィルタ: AM = 0:00-11:59, PM = 12:00-23:59",
                example     = "AM"
            )
        },
        responses = {
            @ApiResponse(
                responseCode = "200",
                description  = "条件にマッチした医師一覧",
                content      = @Content(
                    mediaType = "application/json",
                    examples  = @ExampleObject(
                        name    = "取得例",
                        summary = "AM・心臓内科・鈴木 で検索",
                        value   = """
                        {
                          "doctors": [
                            {
                              "id": 2,
                              "user": {
                                "username": "doctorUser1",
                                "fullName": "鈴木 花子"
                              },
                              "phone": "080-1111-0002",
                              "specialty": "心臓内科",
                              "clinic": {
                                "id": 1,
                                "name": "中央クリニック"
                              },
                              "availableTimes": [
                                "09:00-10:00",
                                "10:00-11:00",
                                "11:00-12:00"
                              ]
                            }
                          ]
                        }
                        """
                    )
                )
            )
        }
    )
    @GetMapping("/filter/{name}/{specialty}/{period}")
    public ResponseEntity<Map<String, Object>> filterDoctors(
            @PathVariable String name,
            @PathVariable String specialty,
            @PathVariable String period) {

        Map<String, Object> body = new HashMap<>();
        
        log.info("★ /filter/{name}/{specialty}/{period}開始");

        System.out.println("name:" + name );
        System.out.println("specialty:" + specialty );
        System.out.println("period:" + period );
        
        
        List<Doctor>doctorFilterList =  doctorService.filterDoctorsByNameSpecilityandTime(name, specialty, period);
        
        System.out.println("【Contoroller】doctorFilterList:"+ doctorFilterList);
        
        body.put("doctors",doctorFilterList );
        
        return ResponseEntity.ok(body);
    }
}
