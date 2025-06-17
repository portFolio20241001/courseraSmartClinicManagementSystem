package com.project.back_end.Entity;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
//			create table doctors (
//			        id bigint not null,
//			        created_at datetime(6) not null,
//			        phone varchar(13) not null,
//			        specialty varchar(50) not null,
//			        clinic_location_id integer not null,
//			        primary key (id)
//			    ) engine=InnoDB

//外部参照キー
//    		Hibernate: 
//    		    alter table doctors 
//    		       add constraint FKasy4plenph3t3v0fu8qhqlq8v 
//    		       foreign key (clinic_location_id) 
//    		       references clinic_locations (id)
//    		Hibernate: 
//    		    alter table doctors 
//    		       add constraint FKgisys6qm9qflq8w4npdhxafne 
//    		       foreign key (id) 
//    		       references users (id)


//@ElementCollectionの箇所は別テーブル作成
//			create table doctor_available_times (
//			        doctor_id bigint not null,
//			        available_times varchar(255)
//			    ) engine=InnoDB


//　IDを1から降りなおしたい場合
//　　　MySQLにて、　ALTER TABLE  doctors AUTO_INCREMENT = 1;


@Entity
@Table(name = "doctors")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")  // // JSON変換時の循環参照を防止するための設定（オブジェクトIDを使う）
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Doctor {

    /**
     * 医師ID（主キー）。UserエンティティのIDと一致。
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
     * 医師が所属するクリニック情報。
     * 多対一の関係：複数の医師が同じクリニックに所属できる。
     */
    @NotNull(message = "所属クリニックは必須です。")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clinic_location_id", nullable = false)
    private ClinicLocation clinicLocation;

    
    /**
     * 専門分野。3〜50文字で必須。
     */
    @NotNull(message = "専門分野は必須です。")
    @Size(min = 2, max = 50, message = "専門分野は2〜50文字で入力してください。")
    private String specialty;

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
     * 診療可能時間帯を文字列リストで管理。
     */
    @ElementCollection
    private List<String> availableTimes;

    /**
     * 登録日時。新規作成時に現在日時をセット。更新不可。
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
    
    @Override
    public String toString() {
        return "Doctor{" +
                "id=" + id +
                ", specialty='" + specialty + '\'' +
                ", phone='" + phone + '\'' +
                ", createdAt=" + createdAt +
                ", user=" + (user != null ? user.getUsername() : "null") +
                ", clinicLocation=" + (clinicLocation != null ? clinicLocation.getName() : "null") +
                ", availableTimes=" + (availableTimes != null ? availableTimes.size() + "件" : "null") +
                '}';
    }
    
}
