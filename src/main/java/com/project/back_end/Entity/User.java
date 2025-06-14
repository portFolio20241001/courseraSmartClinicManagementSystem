package com.project.back_end.Entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;


//DDLイメージ
//create table users (
//        id bigint not null auto_increment,
//        created_at datetime(6) not null,
//        password_hash varchar(255) not null,
//        role enum ('ADMIN','DOCTOR','PATIENT') not null,
//        username varchar(50) not null,
//        primary key (id)
//    ) engine=InnoDB
    
//	既存のユニークインデックス（または制約）を一旦削除して、同じ名前で、username にユニーク制約を再追加（unique = trueにより追加）
//    	Hibernate: 
//    	    alter table users 
//    	       drop index UKr43af9ap4edm43mmtq01oddj6
//    	Hibernate: 
//    	    alter table users 
//    	       add constraint UKr43af9ap4edm43mmtq01oddj6 unique (username)




/**
 * アプリケーションの共通ユーザーエンティティ。
 * <p>
 * このエンティティは、システム上のすべてのユーザー（Admin / Doctor / Patient）に共通する情報を保持します。
 * それぞれの具体的なユーザー情報（Admin等）は、1対1の関係で別エンティティと連携されます。
 */
@Entity
@Table(name = "users")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")  // // JSON変換時の循環参照を防止するための設定（オブジェクトIDを使う）
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    /**
     * ユーザーのロールを定義する列挙型。
     * それぞれのロールは、適切な詳細情報エンティティ（Admin, Doctor, Patient）と1対1で対応します。
     */
    public enum Role {
    	ROLE_ADMIN,
    	ROLE_DOCTOR,
    	ROLE_PATIENT
    }

    /**
     * 主キー（ユーザーID）。
     * 自動生成され、各サブテーブル（admin, doctor, patient）にもそのまま使われます。
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * ユーザー名。ログイン時に使用。
     * 一意であり、最大50文字に制限されます。
     */
    @NotNull(message = "ユーザー名は必須です。")
    @Size(max = 50, message = "ユーザー名は50文字以内で入力してください。")
    @Column(nullable = false, unique = true, length = 50)
    private String username;

    /**
     * パスワードのハッシュ値。最大255文字。
     * JSONシリアライズ時には出力されず、受け取り専用。
     */
    @NotNull(message = "パスワードは必須です。")
    @Size(max = 255)
    @Column(name = "password_hash", nullable = false, length = 255)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String passwordHash;

    /**
     * ユーザーのロール（ADMIN / DOCTOR / PATIENT）。
     * EnumType.STRINGにより、データベースには文字列として保存されます。
     */
    @NotNull(message = "ロールは必須です。")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;
    
    @NotNull(message = "フルネームは必須です。")
    @Size(max = 100, message = "フルネームは100文字以内で入力してください。")
    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    /**
     * 管理者（Admin）エンティティとの1対1のマッピング。
     * 双方向関連として定義され、Userが親エンティティになります。
     */
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private Admin admin;

    /**
     * 医師（Doctor）エンティティとの1対1のマッピング。
     * 双方向関連として定義され、Userが親エンティティになります。
     */
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private Doctor doctor;

    /**
     * 患者（Patient）エンティティとの1対1のマッピング。
     * 双方向関連として定義され、Userが親エンティティになります。
     */
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private Patient patient;

    /**
     * アカウント作成日時。
     * 初回挿入時に自動設定され、更新不可。
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * エンティティが最初にDBに保存される直前に呼び出され、
     * createdAt に現在時刻が設定されます。
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
