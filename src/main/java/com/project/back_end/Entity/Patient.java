package com.project.back_end.Entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


//DDLイメージ
//		create table patients (
//		        id bigint not null,
//		        address varchar(255) not null,
//		        created_at datetime(6) not null,
//		        date_of_birth date not null,
//		        gender enum ('female','male','other') not null,
//		        phone varchar(13) not null,
//		        primary key (id)
//		    ) engine=InnoDB

//外部参照キー
//    	 alter table patients 
//    	       add constraint FKn8xphvlp05nd3ydg0p1rbdaom 
//    	       foreign key (id) 
//    	       references users (id)


/**
 * 患者エンティティクラス（patientsテーブルに対応）
 * 
 * Userエンティティにある項目（id, name, email, password, phoneなど）は
 * こちらには持たず、Userと1対1で紐付けています。
 * 患者固有の情報のみ定義します。
 */
@Entity
@Table(name = "patients")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")  // // JSON変換時の循環参照を防止するための設定（オブジェクトIDを使う）
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Patient {

    /**
     * 患者ID（主キー）。UserエンティティのIDと一致。
     */
    @Id
    private Long id;

    /**
     * Userエンティティとの1対1の関連。
     */
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "id")
    private User user;
    
    
    /**
     * 電話番号（例: 080-1234-5678）
     * - ハイフンありの10〜13文字程度
     * - 正規表現で XXX-XXXX-XXXX 形式を検証
     */
    @NotNull(message = "電話番号は必須です。")
    @Pattern(regexp = "^\\d{3}-\\d{4}-\\d{4}$", message = "電話番号はXXX-XXXX-XXXXの形式で入力してください。")
    @Column(length = 13) // varchar(13)で指定
    private String phone;
    

    /**
     * 住所
     * - 最大255文字。
     * - 必須項目。
     */
    @NotNull(message = "住所は必須です。")
    @Size(max = 255, message = "住所は255文字以内で入力してください。")
    private String address;

    /**
     * 生年月日
     * - LocalDate形式。
     * - 必須項目。
     */
    @NotNull(message = "生年月日は必須です。")
    private LocalDate dateOfBirth;

    /**
     * 性別
     * - 列挙型（male / female / other）
     * - 必須項目。
     */
    @NotNull(message = "性別は必須です。")
    @Enumerated(EnumType.STRING)
    private Gender gender;

    /**
     * 登録日時（作成時に自動で設定される）
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * DBに挿入される前に呼ばれ、createdAt に現在日時を設定する。
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    /**
     * 性別列挙型
     */
    public enum Gender {
        male, female, other
    }
}
