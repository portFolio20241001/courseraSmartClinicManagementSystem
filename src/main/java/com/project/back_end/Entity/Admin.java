package com.project.back_end.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;


//　DDLイメージ
//	    create table admins (
//	        id bigint not null,
//	        created_at datetime(6) not null,
//	        primary key (id)
//	    ) engine=InnoDB

//外部参照キー
//		alter table admins 
//		add constraint FKanhsicqm3lc8ya77tr7r0je18 
//		foreign key (id) 
//		references users (id)


@Entity
@Table(name = "admins")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")  // // JSON変換時の循環参照を防止するための設定（オブジェクトIDを使う）
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Admin {

    /**
     * 管理者ID（主キー）。UserエンティティのIDと一致。
     * @MapsId により User の ID をそのまま使用する。
     */
    @Id
    private Long id;

    /**
     * Userエンティティとの1対1の関連。
     * このAdminはUser情報にひもづいており、UserのIDがそのままAdminのIDになる。
     */
    @OneToOne
    @MapsId
    @JoinColumn(name = "id")
    private User user;

    /**
     * 登録日時。新規登録時に現在日時をセットし、更新不可。
     */
    @NotNull(message = "登録日時は必須です。")
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * エンティティがDBに最初に挿入される直前に呼ばれ、
     * createdAtフィールドに現在日時をセットする。
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
