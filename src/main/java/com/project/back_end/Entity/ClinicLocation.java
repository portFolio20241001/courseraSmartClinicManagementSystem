package com.project.back_end.Entity;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;



//DDLイメージ
//			create table clinic_locations (
//			        id integer not null auto_increment,
//			        address varchar(255) not null,
//			        created_at datetime(6) not null,
//			        name varchar(100) not null,
//			        phone varchar(13) not null,
//			        primary key (id)
//			    ) engine=InnoDB



@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(name = "clinic_locations")
public class ClinicLocation {

    /**
     * 主キー：所在地ID（自動採番）
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * クリニック名（最大100文字）
     */
    @NotNull
    @Size(max = 100)
    private String name;

    /**
     * 所在地住所（最大255文字）
     */
    @NotNull
    @Size(max = 255)
    private String address;

    /**
     * 電話番号（数字10桁）
     */
    @NotNull(message = "電話番号は必須です。")
    @Pattern(regexp = "^\\d{3}-\\d{4}-\\d{4}$", message = "電話番号はXXX-XXXX-XXXXの形式で入力してください。")
    @Column(length = 13) // varchar(13)で指定
    private String phone;
    
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
     * このクリニックに所属する医師の一覧。
     * 
     * - `Doctor` エンティティの `clinicLocation` フィールドによりマッピングされます（双方向）。
     * - クリニックが削除されると、関連する医師エンティティも自動的に削除されます（`cascade = CascadeType.ALL`）。
     * - `orphanRemoval = true` により、リストから削除された医師は永続化対象からも削除されます。
     */
    @OneToMany(mappedBy = "clinicLocation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Doctor> doctors;

    
	public Integer getId() {
		
		return this.id;
	}
    
	public String getName() {
		
		return this.name;
	}
	
	public String getAddress() {
		
		return this.address;
	}
	
	public String getPhone() {
		
		return this.phone;
	}
    
}
