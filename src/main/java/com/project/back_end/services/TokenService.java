package com.project.back_end.services;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.project.back_end.Entity.Patient;
import com.project.back_end.repo.AdminRepository;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * <h2>TokenService ― JWT を一元管理するサービス</h2>
 *
 * <p>生成／解析／検証の全ロジックをここに集約。</p>
 *
 * <ul>
 *   <li>{@link #generateToken(String)}……ユーザー名を subject にしたトークンを発行</li>
 *   <li>{@link #extractUsername(String)}……トークンからユーザー名を取り出す</li>
 *   <li>{@link #validateToken(String, String)}……ロール別にトークンを検証</li>
 *   <li>{@link #getSigningKey()}……署名・検証に用いる {@link SecretKey} を生成</li>
 * </ul>
 *
 * <p>
 * <b>※ 本実装では「username = メールアドレス相当のユニーク文字列」</b>  
 * を想定して subject に埋め込んでいる。
 * </p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TokenService {

    /* ========== DI ========== */
    private final AdminRepository   adminRepository;
    private final DoctorRepository  doctorRepository;
    private final PatientRepository patientRepository;

    /* ========== アプリ設定から読み込むシークレット ========== */
    @Value("${jwt.secret}")
    private String jwtSecret;               // 256bit(=32byte) 以上推奨

    /* ========== 有効期限（7日） ========== */
    private static final long EXPIRATION_MILLIS = 1000L * 60 * 60 * 24 * 7;

    // ---------------------------------------------------------------------
    //  1. 署名鍵取得メソッド
    // ---------------------------------------------------------------------
    /**
     * application.properties に設定した <code>jwt.secret</code> を
     * HMAC-SHA256 用 {@link SecretKey} に変換して返却する。
     *
     * @return JWT の署名／検証に使用する秘密鍵
     */
    private SecretKey  getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    // ---------------------------------------------------------------------
    //  2.  JWT トークン生成メソッド
    // ---------------------------------------------------------------------
    /**
     * 指定されたユーザー名を subject とする JWT を生成する。
     *
     * @param username  トークンに埋め込むユーザー名
     * @return          生成された JWT 文字列
     */
    public String generateToken(String username) {

        Date issuedAt = new Date();
        Date expired  = new Date(issuedAt.getTime() + EXPIRATION_MILLIS);

        return Jwts.builder()
                   .subject(username)           // 主題にユーザー名
                   .issuedAt(issuedAt)          // 発行日時
                   .expiration(expired)         // 有効期限
                   .signWith(getSigningKey()) 
                   .compact();
    }

    // ---------------------------------------------------------------------
    //  3. トークンからユーザー名を取り出すメソッド
    // ---------------------------------------------------------------------
    /**
     * トークンを検証したうえで subject（＝ユーザー名）を抽出する。
     *
     * @param token  解析対象の JWT
     * @return       ユーザー名（失敗時は {@code null}）
     */
    public String extractUsername(String token) {

        try {
            return Jwts.parser()                                  // 0.12.x：parser() -> build() まで分割
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)                  // 署名付き JWT を解析
                    .getPayload()
                    .getSubject();
            
        } catch (Exception e) {
            log.warn("トークン解析失敗: {}", e.getMessage());
            return null;
        }
    }

    // ---------------------------------------------------------------------
    //  4. トークン有効性検証メソッド
    // ---------------------------------------------------------------------
    /**
     * ロール別にトークンが有効かどうかを判定する。
     *
     * @param token   JWT
     * @param role    "admin" / "doctor" / "patient"
     * @return        {@code true} = 有効 / {@code false} = 無効
     */
    public boolean validateToken(String token, String role) {

        // 1) 期限切れ ＆ 署名不正チェック
        String username = extractUsername(token);
        
        System.out.println("トークン有効性検証対象 username:" + username);
        
        if (username == null) return false;

        // 2) ロール別に DB へ存在確認
        return switch (role.toLowerCase()) {

            case "admin"   -> adminRepository.findByUser_Username(username)         .isPresent();

            case "doctor"  -> doctorRepository.findByUser_Username(username)          .isPresent();

            case "patient" -> patientRepository.findByUser_Username(username)         .isPresent();

            default        -> false;
        };
    }

    // ---------------------------------------------------------------------
    //  5. (補助) トークンが技術的に有効かどうかだけを判定
    // ---------------------------------------------------------------------
    /**
     * 署名検証と有効期限チェックのみを行う簡易バリデータ。
     *
     * @param token JWT
     * @return      {@code true} = トークン構文が正しく期限内
     */
    public boolean isValidToken(String token) {

        try {
            // 1. 署名検証込みのパーサーを組み立てて Claims を取得
            Claims claims = Jwts.parser()                                   // 0.12.x では parser() → verifyWith(...)
                                .verifyWith(getSigningKey()) // 署名鍵を指定
                                .build()                                     // Parser をビルド
                                .parseSignedClaims(token)                    // 署名付き JWT を解析
                                .getPayload();                               // Claims 部分を取得

             // 2. 有効期限をチェック（現在日時より後なら有効）
            return claims.getExpiration().after(new Date());
            
        } catch (Exception e) {
            return false;
        }
    }

    // ---------------------------------------------------------------------
    //  6. (例) Patient 用ヘルパーメソッド
    // ---------------------------------------------------------------------
    /**
     * トークンに紐付く患者の ID を取得するユーティリティ。
     *
     * @param token JWT
     * @return      Patient の ID（該当なし＝{@code null}）
     */
    public Long getPatientIdFromToken(String token) {

        String username = extractUsername(token);
        
        if (username == null) return null;

        return patientRepository.findByUser_Username(username)
                                .map(Patient::getId)
                                .orElse(null);
    }
}
