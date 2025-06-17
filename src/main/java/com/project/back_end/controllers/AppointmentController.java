package com.project.back_end.controllers;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.back_end.Entity.Appointment;
import com.project.back_end.services.AppointmentService;
import com.project.back_end.services.CommonService;  // ★共通バリデーション用サービス

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;

/**
 * <h2>AppointmentController</h2>
 * <pre>
 * ・予約（Appointment）に関する CRUD エンドポイントを提供する REST コントローラ  
 * ・トークン検証を行い、ユーザーのロール（doctor / patient）に応じてアクセス制御を行う  
 *
 *  エンドポイント一覧
 *  ┌─────────────────────────────────────────────────────────┐
 *  │ GET    /appointments/{date}/{patientName}/{token}   	    │ … 予約一覧取得（医師）  │
 *  │ POST   /appointments/{token}                           					│ … 予約作成（患者）    │
 *  │ PUT    /appointments/{token}                       					    │ … 予約更新（患者）    │
 *  │ DELETE /appointments/{id}/{token}                  			    │ … 予約キャンセル（患者）│
 *  └─────────────────────────────────────────────────────────┘
 * </pre>
 *
 * @author  back_end team
 */
@RestController
@RequestMapping("/appointments")
@Valid
public class AppointmentController {

    /** 予約関連ビジネスロジック */
    private final AppointmentService appointmentService;
    /** トークン検証／予約検証などの共通ロジック */
    private final CommonService       commonService;

    /** コンストラクタ・インジェクション */
    @Autowired
    public AppointmentController(AppointmentService appointmentService,
                                 CommonService service) {
        this.appointmentService = appointmentService;
        this.commonService            = service;
    }
    
    
    @Operation(
    	    summary = "指定日付の医師単位での予約一覧取得（by Doctor）",
    	    description = "指定した医師 ID と日付に基づいて、その日に予約されているすべての予約情報を返します。",
    	    parameters = {
    	        @Parameter(name = "doctorId", description = "医師の ID", example = "2"),
    	        @Parameter(name = "date", description = "予約日 (yyyy-MM-dd)", example = "2025-09-11"),
    	        @Parameter(name = "token", description = "JWT トークン（ROLE_DOCTOR）", example = "eyJhbGciOiJIUzI1NiJ9.tokenSignature")
    	    },
    	    responses = {
    	        @ApiResponse(
    	            responseCode = "200",
    	            description = "該当日の予約リストを返却",
    	            content = @Content(
    	                mediaType = MediaType.APPLICATION_JSON_VALUE,
    	                examples = @ExampleObject(
    	                    name = "予約一覧の例",
    	                    value = """
    	                    {
    	                      "appointments": [
    	                        {
    	                          "id": 1,
    	                          "doctorId": 2,
    	                          "patientId": 12,
    	                          "patientName": "松本 綾香",
    	                          "appointmentTime": "2025-09-11T09:00:00",
    	                          "status": 0,
    	                          "payment": null
    	                        },
    	                        {
    	                          "id": 2,
    	                          "doctorId": 2,
    	                          "patientId": 13,
    	                          "patientName": "佐藤 優子",
    	                          "appointmentTime": "2025-09-11T10:00:00",
    	                          "status": 0,
    	                          "payment": null
    	                        }
    	                      ]
    	                    }
    	                    """
    	                )
    	            )
    	        ),
    	        @ApiResponse(
    	            responseCode = "401",
    	            description = "認証トークンが無効",
    	            content = @Content(
    	                mediaType = MediaType.APPLICATION_JSON_VALUE,
    	                examples = @ExampleObject(
    	                    name = "トークンエラー例",
    	                    value = """
    	                    {
    	                      "error": "トークンが無効です"
    	                    }
    	                    """
    	                )
    	            )
    	        )
    	    }
    	)

    /**
     * 指定日・患者名で医師の予約を検索して返却する。  
     * <p>トークンは <b>doctor</b> ロールとして検証される。</p>
     *
     * @param doctorId         医師のID
     * @param date         予約日 (yyyy-MM-dd)
     * @param token        認証トークン
     *
     * @return 200：予約一覧 / その他：検証失敗
     */
    @GetMapping("/{doctorId}/{date}/{token}")
    public ResponseEntity<Map<String, Object>> getAppointmentsByDate(
            @PathVariable Long doctorId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @PathVariable String token) {

        // トークン検証（doctor ロール）
        Optional<String> hasError = commonService.validateToken(token, "doctor");
        if (hasError.isPresent()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                 .body(Map.of("error", hasError.get()));
        }

        // 指定日付の予約一覧を取得
        return appointmentService.getAppointmentsByDate(doctorId, date, token);
    }
    

    /* ====================================================================
     * ① 予約一覧取得（医師）
     * ===================================================================*/
    @Operation(
        summary     = "医師の予約一覧取得 (by Doctor)",
        description = "指定日と患者名（部分一致可）で、医師が持つ予約を取得します。",
        parameters  = {
            @Parameter(name = "doctorId", description = "医師 ID", example = "2"),
            @Parameter(name = "date",     description = "検索対象日 (yyyy-MM-dd)", example = "2025-09-11"),
            @Parameter(name = "patientName", description = "患者名（部分一致）例: \"松本\"(null指定で全件指定)", example = "松本"),
            @Parameter(name = "token",    description = "Doctor ログイントークン", example = "eyJhbGciOiJIUzI1NiJ9.doctorTokenSig")
        },
        responses = {
            @ApiResponse(
                responseCode = "200",
                description  = "検索成功",
                content      = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    examples  = @ExampleObject(
                        name    = "Success",
                        summary = "検索結果例",
                        value   = """
                        {
                          "appointments": [
                            {
                              "id": 1,
                              "doctor": { "id": 2 },
                              "patient": { "id": 12 },
                              "appointmentTime": "2025-09-11T09:00:00",
                              "status": 0
                            },
                            {
                              "id": 4,
                              "doctor": { "id": 2 },
                              "patient": { "id": 15 },
                              "appointmentTime": "2025-09-11T14:00:00",
                              "status": 1
                            }
                          ]
                        }
                        """
                    )
                )
            ),
            @ApiResponse(
                responseCode = "401",
                description  = "トークン無効／期限切れ",
                content      = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    examples  = @ExampleObject(value = """
                    { "error": "トークンが無効です。" }
                    """)
                )
            )
        }
    )
    /**
     * 指定日・患者名で医師の予約を検索して返却する。  
     * <p>トークンは <b>doctor</b> ロールとして検証される。</p>
     *
     * @param doctorId         医師のID
     * @param date         予約日 (yyyy-MM-dd)
     * @param patientName  患者氏名（部分一致）  
     *                     `"null"` など空文字の場合はフィルタしない
     * @param token        認証トークン
     *
     * @return 200：予約一覧 / その他：検証失敗
     */
    @GetMapping("/{doctorId}/{date}/{patientName}/{token}")
    public ResponseEntity<Map<String, Object>> getAppointments(
            @PathVariable Long doctorId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @PathVariable String patientName,
            @PathVariable String token) {

        /* ===== 1. トークン検証（doctor ロール） ===== */
    	Optional<String> hasError = commonService.validateToken(token, "doctor");  

        System.out.println("ポイント1");
        
        if (hasError.isPresent()) {
            // 認証エラーをそのまま返す
        	return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", hasError.get()));
        }
        
        System.out.println("aaaaaa");

        /* ===== 2. 予約一覧取得　返却 ===== */
        return appointmentService.getAppointments(doctorId, date, patientName, token);

    }

    /* =====================================================================
     * ② 予約作成（患者）
     * ===================================================================*/
    @Operation(
    	    summary     = "新規予約作成＋支払い情報（by Patient）",
    	    description = "患者が医師の空き時間に予約を入れ、必要であれば支払い情報も同時に登録します。",
    	    parameters  = @Parameter(
    	        name        = "token",
    	        description = "Patient ログイントークン",
    	        example     = "eyJhbGciOiJIUzI1NiJ9.patientTokenSig"
    	    ),
    	    requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
    	        required    = true,
    	        description = "予約内容（支払い情報は任意）",
    	        content     = @Content(
    	            mediaType = "application/json",
    	            schema    = @Schema(implementation = Appointment.class),
    	            examples  = @ExampleObject(
    	                name   = "予約と支払い登録例",
    	                value  = """
    	                {
    	                  "doctor":   { "id": 2 },
    	                  "patient":  { "id": 12 },
    	                  "appointmentTime": "2025-09-17T11:00:00",
    	                  "status": 0,
    	                  "payment": {
    	                    "paymentMethod": "credit",
    	                    "paymentStatus": "Pending"
    	                  }
    	                }
    	                """
    	            )
    	        )
    	    ),
    	    responses = {
    	        @ApiResponse(
    	            responseCode = "201",
    	            description  = "予約と支払いが正常に登録されました。",
    	            content      = @Content(
    	                mediaType = "application/json",
    	                examples  = @ExampleObject(value = """
    	                { "message": "予約と支払いが正常に登録されました。" }
    	                """)
    	            )
    	        ),
    	        @ApiResponse(
    	            responseCode = "400",
    	            description  = "バリデーションエラー / 医師不在・重複予約など",
    	            content      = @Content(
    	                examples = @ExampleObject(value = """
    	                { "message": "指定時間はすでに予約済み、または医師が不在です。" }
    	                """)
    	            )
    	        ),
    	        @ApiResponse(
    	            responseCode = "401",
    	            description  = "認証失敗（無効なトークン）",
    	            content      = @Content(
    	                examples = @ExampleObject(value = """
    	                { "error": "トークンが無効です。" }
    	                """)
    	            )
    	        )
    	    }
    	)
    /**
     * 新規予約を作成する。
     *
     * @param appointment  予約情報（JSON）
     * @param token        患者トークン
     * @return 201：作成成功 / 400・409 など
     */
    @PostMapping("/{token}")
    public ResponseEntity<Map<String, String>> bookAppointment(
            @RequestBody @Valid Appointment appointment,
            @PathVariable String token) {

        /* トークン検証（patient ロール） */
    	Optional<String> hasError = commonService.validateToken(token, "patient");  

        System.out.println("ポイント1");
        
        if (hasError.isPresent()) {
            // 認証エラーをそのまま返す
        	return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", hasError.get()));
        }

        Map<String, String> res = new HashMap<>();

        /* 予約可能か検証 */
        int chk = commonService.validateAppointment(appointment);
        
        
        if (chk == -1) {
            res.put("message", "指定した医師が存在しません。");
            return ResponseEntity.badRequest().body(res);
        }
        if (chk == 0) {
            res.put("message", "指定時間に医師の空きがありません。");
            return ResponseEntity.badRequest().body(res);
        }

        /* 予約処理 */
        return appointmentService.bookAppointment(appointment);
    }

    /* =====================================================================
     * ③ 予約更新（患者）
     * ===================================================================*/
    @Operation(
        summary     = "予約時間の更新 (by Patient)",
        description = "患者自身の既存予約を変更します。",
        parameters  = @Parameter(
            name        = "token",
            description = "Patient ログイントークン",
            example     = "eyJhbGciOiJIUzI1NiJ9.patientTokenSig"
        ),
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = "更新内容（ID 必須）",
            content = @Content(
                schema   = @Schema(implementation = Appointment.class),
                examples = @ExampleObject(
                    name  = "更新例",
	                value  = """
	                {
	                  "id": 58,
	                  "doctor":   { "id": 2 },
	                  "patient":  { "id": 12 },
	                  "appointmentTime": "2025-09-18T11:00:00",
	                  "status": 0,
	                  "payment": {
	                    "paymentMethod": "credit",
	                    "paymentStatus": "Pending"
	                  }
	                }
	                """
                )
            )
        ),
        responses = @ApiResponse(
            responseCode = "200",
            description  = "更新成功",
            content      = @Content(
                examples = @ExampleObject(value = """
                { "message": "予約が正常に更新されました。" }
                """)
            )
        )
    )

    /**
     * 既存予約を更新する。
     *
     * @param token       患者トークン
     * @param appointment 更新内容
     * @return 200：更新成功 / 4xx：エラー
     */
    @PutMapping("/{token}")
    public ResponseEntity<Map<String, String>> updateAppointment(
            @PathVariable String token,
            @RequestBody @Valid Appointment appointment) {

        /* トークン検証（patient ロール） */
    	Optional<String> hasError = commonService.validateToken(token, "patient");  

        System.out.println("ポイント1");
        
        if (hasError.isPresent()) {
            // 認証エラーをそのまま返す
        	return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", hasError.get()));
        }

        return appointmentService.updateAppointment(appointment);
    }

    /* =====================================================================
     * ④ 予約キャンセル（患者）
     * ===================================================================*/
    @Operation(
        summary     = "予約キャンセル (by Patient)",
        description = "患者が自身の予約をキャンセルします。",
        parameters  = {
            @Parameter(name = "id",    description = "キャンセルする予約 ID", example = "1"),
            @Parameter(name = "token", description = "Patient ログイントークン", example = "eyJhbGciOiJIUzI1NiJ9.patientTokenSig")
        },
        responses   = {
            @ApiResponse(
                responseCode = "200",
                description  = "キャンセル成功",
                content      = @Content(
                    examples = @ExampleObject(value = """
                    { "message": "予約がキャンセルされました。" }
                    """)
                )
            ),
            @ApiResponse(
                responseCode = "404",
                description  = "予約が存在しない",
                content      = @Content(
                    examples = @ExampleObject(value = """
                    { "error": "予約が見つかりません。" }
                    """)
                )
            )
        }
    )

    /**
     * 予約をキャンセルする。
     *
     * @param id    予約 ID
     * @param token 患者トークン
     * @return 200：キャンセル成功 / 4xx：エラー
     */
    @DeleteMapping("/{id}/{token}")
    public ResponseEntity<Map<String, String>> cancelAppointment(
            @PathVariable Long id,
            @PathVariable String token) {

        /* トークン検証（patient ロール） */
    	Optional<String> hasError = commonService.validateToken(token, "patient");  

        System.out.println("ポイント1");
        
        if (hasError.isPresent()) {
            // 認証エラーをそのまま返す
        	return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", hasError.get()));
        }

        return appointmentService.cancelAppointment(id, token);
    }
}
