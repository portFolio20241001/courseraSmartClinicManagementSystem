package com.project.back_end.controllers;

import java.security.Provider.Service;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.back_end.DTO.Login;
import com.project.back_end.Entity.Admin;
import com.project.back_end.Entity.User;
import com.project.back_end.services.CommonService;        // 共通ロジックをまとめたサービス

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * <h2>管理者関連 REST コントローラー</h2>
 *
 * <p>
 * 管理者ログインをはじめ、管理者専用 API エンドポイントをまとめるクラス。  
 * クライアント ⇔ サーバ間の通信は JSON で行うため、{@link org.springframework.web.bind.annotation.RestController}
 * を付与している。
 * </p>
 *
 * <pre>
 * ┌──────────────────────────────┐
 * │ BasePath : {api.path}admin   │  application.properties 例
 * │  api.path=/api/v1/           │→ /api/v1/admin/login
 * └──────────────────────────────┘
 * </pre>
 *
 * @author your-name
 */
@RestController
@RequestMapping("${api.path}admin")
@RequiredArgsConstructor          // Lombok: 必須依存性をコンストラクタ経由で注入
@Slf4j                            // ログ出力用
@Valid                        // @Valid, @Valid を使った入力バリデーションを有効化
public class AdminController {

    /** ビジネスロジックを集約した共通サービス */
    private final CommonService commonService;

    // ------------------------------------------------------------------
    // 1. 管理者ログイン
    // ------------------------------------------------------------------

    /**
     * <h3>管理者ログイン</h3>
     *
     * <p>
     * 受け取ったユーザー名・パスワードを {@link Service#validateAdmin(Admin)} に委譲し、
     * 認証結果（成功時は JWT トークン）を返却する。
     * </p>
     *
     * <ul>
     *   <li><b>成功</b> : HTTP&nbsp;200 + body = {"token":"xxxxx..."}</li>
     *   <li><b>認証失敗</b> : HTTP&nbsp;401 + body = {"error":"Invalid credentials"}</li>
     *   <li><b>その他エラー</b> : HTTP&nbsp;500 + body = {"error":"Internal server error"}</li>
     * </ul>
     *
     
     
     
     @Operation(
    summary     = "管理者ログイン",
    description = "ユーザー名とパスワードで認証し、成功時に JWT を返します。",
    requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
        required    = true,
        description = "管理者のログイン情報（username / password）",
        content     = @Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema    = @Schema(implementation = Login.class),
            examples  = @ExampleObject(
                name    = "AdminLoginSample",
                summary = "adminUser1 でログイン",
                value   = """
                    {
                      "username": "adminUser1",
                      "password": "adminPassPlain"
                    }"""
            )
        )
    ),
    responses = {
        @ApiResponse(
            responseCode = "200",
            description  = "認証成功",
            content      = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema    = @Schema(
                    type    = "object",
                    example = """
                      {
                        "token"  : "eyJhbGciOiJIUzI1NiJ9.adminTokenSig",
                        "message": "ログインに成功しました。"
                      }"""
                )
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description  = "ユーザー名またはパスワード不一致",
            content      = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema    = @Schema(
                    example = "{\"error\":\"ユーザー名が存在しません。\"}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description  = "内部サーバエラー",
            content      = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema    = @Schema(
                    example = "{\"error\":\"内部エラーが発生しました。\"}"
                )
            )
        )
    }
)
     * @param receivedAdmin リクエストボディで送られてくる管理者のユーザー名・パスワード
     * @return 認証結果（トークン or エラーメッセージ）を格納した {@link ResponseEntity}
     */
    
    @Operation(
    	    summary     = "管理者ログイン",
    	    description = "ユーザー名とパスワードで認証し、成功時に JWT を返します。",
    	    requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
    	        required    = true,
    	        description = "管理者のログイン情報（username / password）",
    	        content     = @Content(
    	            mediaType = MediaType.APPLICATION_JSON_VALUE,
    	            schema    = @Schema(implementation = Login.class),
    	            examples  = @ExampleObject(
    	                name    = "AdminLoginSample",
    	                summary = "adminUser1 でログイン",
    	                value   = """
    	                    {
    	                      "username": "adminUser1",
    	                      "password": "addpass1"
    	                    }"""
    	            )
    	        )
    	    ),
    	    responses = {
    	        @ApiResponse(
    	            responseCode = "200",
    	            description  = "認証成功",
    	            content      = @Content(
    	                mediaType = MediaType.APPLICATION_JSON_VALUE,
    	                schema    = @Schema(
    	                    type    = "object",
    	                    example = """
    	                      {
    	                        "token"  : "eyJhbGciOiJIUzI1NiJ9.adminTokenSig",
    	                        "message": "ログインに成功しました。"
    	                      }"""
    	                )
    	            )
    	        ),
    	        @ApiResponse(
    	            responseCode = "401",
    	            description  = "ユーザー名またはパスワード不一致",
    	            content      = @Content(
    	                mediaType = MediaType.APPLICATION_JSON_VALUE,
    	                schema    = @Schema(
    	                    example = "{\"error\":\"ユーザー名が存在しません。\"}"
    	                )
    	            )
    	        ),
    	        @ApiResponse(
    	            responseCode = "500",
    	            description  = "内部サーバエラー",
    	            content      = @Content(
    	                mediaType = MediaType.APPLICATION_JSON_VALUE,
    	                schema    = @Schema(
    	                    example = "{\"error\":\"内部エラーが発生しました。\"}"
    	                )
    	            )
    	        )
    	    }
    	)
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> adminLogin(@Valid @RequestBody Login Login) {
    	
        log.info("★ 管理者ログイン要求: username={}", Login.getUsername());
        
        // --- DTO → Admin エンティティへ詰め替え ----------------------------
        User user = User.builder()
                        .username(Login.getUsername())
                        .passwordHash(Login.getPassword())   // ハッシュ化していない平文PWセット
                        .build();

        Admin receivedAdmin = Admin.builder()
                           .user(user)
                           .build();
        
        return commonService.validateAdmin(receivedAdmin);
    }

    // ここに「Add-Doctor」「Delete-Doctor」等、管理者専用 API を追加していくことができます。
}
