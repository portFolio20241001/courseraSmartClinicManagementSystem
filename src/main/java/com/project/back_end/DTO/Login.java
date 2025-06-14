package com.project.back_end.DTO;

import jakarta.validation.constraints.NotBlank;

/**
 * ログイン用DTOクラス。
 * ユーザーがログイン時に入力するユーザー名とパスワードを保持するためのクラス。
 */
public class Login {

    // ユーザー名（ログインに使用）
    // ユーザー認証に必要な識別子として機能します。
	@NotBlank(message = "ユーザー名は必須です")
    private String username;

    // パスワード（ログインに使用）
    // ユーザー名に対応する秘密の文字列。認証時に検証されます。
	@NotBlank(message = "パスワードは必須です")
    private String password;

    // デフォルトコンストラクタ
    public Login() {
    }

    // usernameのゲッターメソッド
    public String getUsername() {
        return username;
    }

    // usernameのセッターメソッド
    public void setUsername(String username) {
        this.username = username;
    }

    // passwordのゲッターメソッド
    public String getPassword() {
        return password;
    }

    // passwordのセッターメソッド
    public void setPassword(String password) {
        this.password = password;
    }
}
