package com.project.back_end.Entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


//DDLイメージ
//			create table appointments (
//			        id bigint not null auto_increment,
//			        appointment_time datetime(6) not null,
//			        status integer not null,
//			        doctor_id bigint not null,
//			        patient_id bigint not null,
//			        primary key (id)
//			    ) engine=InnoDB
    

//外部参照キー
//    	    alter table appointments 
//    	       add constraint FKmujeo4tymoo98cmf7uj3vsv76 
//    	       foreign key (doctor_id) 
//    	       references doctors (id)
//    	Hibernate: 
//    	    alter table appointments 
//    	       add constraint FK8exap5wmg8kmb1g1rx3by21yt 
//    	       foreign key (patient_id) 
//    	       references patients (id)




@Entity
@Table(name = "appointments")
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id"
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Appointment {

    /**
     * 主キー、予約ID。DBで自動生成される。
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 医師情報（多対一）。予約は一人の医師に紐づく。必須。
     */
    @NotNull(message = "医師は必須です。")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    /**
     * 患者情報（多対一）。予約は一人の患者に紐づく。必須。
     */
    @NotNull(message = "患者は必須です。")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    /**
     * 予約日時。未来日時のみ許可。必須。
     */
    @NotNull(message = "予約日時は必須です。")
    @Future(message = "予約日時は未来の日時を指定してください。")
    @Column(name = "appointment_time", nullable = false, columnDefinition = "DATETIME")
    private LocalDateTime appointmentTime;

    /**
     * 予約ステータスを整数で管理（0=Scheduled, 1=Completed, 2=Canceled）。必須。
     */
    @NotNull(message = "ステータスは必須です。")
    @Column(nullable = false)
    private Integer status;
    
    /**
     * この予約に紐づく支払い情報（1対1の関連）。
     * Paymentエンティティの appointment フィールドによってマッピングされます。
     *
     * - mappedBy: 「支払い情報（Payment）」側が関係の所有者（主導側）であることを示す。
     * - cascade: この予約が保存・更新・削除されると、関連する支払い情報にも同様の操作が自動的に適用される。
     * - fetch: 遅延読み込み（LAZY）により、支払い情報は必要になるまで読み込まれない。
     * - optional: 支払い情報が存在しない予約も許容する（NULL可）。
     */
    @OneToOne(mappedBy = "appointment", cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = true)
    private Payment payment;


    /**
     * 予約終了時間を返す計算フィールド（DBには保存しない）。
     * 予約開始時間の1時間後を返す。
     */
    @Transient
    public LocalDateTime getEndTime() {
        return appointmentTime != null ? appointmentTime.plusHours(1) : null;
    }

    /**
     * 予約日時から日付部分のみを取得する計算フィールド（DB保存なし）。
     */
    @Transient
    public LocalDate getAppointmentDate() {
        return appointmentTime != null ? appointmentTime.toLocalDate() : null;
    }

    /**
     * 予約日時から時間部分のみを取得する計算フィールド（DB保存なし）。
     */
    @Transient
    public LocalTime getAppointmentTimeOnly() {
        return appointmentTime != null ? appointmentTime.toLocalTime() : null;
    }


    // コンストラクタは@NoArgsConstructor、@AllArgsConstructorで自動生成済み
    // Getter/SetterはLombokの@Getter/@Setterで自動生成済み
}
