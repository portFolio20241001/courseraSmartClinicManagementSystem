package com.project.back_end.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;



//DDLイメージ
//			create table payments (
//			        id integer not null auto_increment,
//			        amount decimal(10,2) not null,
//			        created_at datetime(6) not null,
//			        paid_at datetime(6),
//			        payment_method enum ('cash','credit','insurance') not null,
//			        payment_status enum ('Failed','Paid','Pending') not null,
//			        appointment_id bigint not null,
//			        primary key (id)
//			    ) engine=InnoDB

//外部参照キー
//			Hibernate: 
//			    alter table payments 
//			       add constraint FK9a0odew03qao7nlbdsesrux5u 
//			       foreign key (appointment_id) 
//			       references appointments (id)

//
//ユニークキー
//    		Hibernate: 
//    		    alter table payments 
//    		       drop index UK2kxb37oip0md9ggekjbjmana4
//    		Hibernate: 
//    		    alter table payments 
//    		       add constraint UK2kxb37oip0md9ggekjbjmana4 unique (appointment_id)
    


/**
 * 支払い情報を表すエンティティクラス。
 * 患者の予約に対して発生する支払いの詳細を管理します。
 * 支払い方法やステータス、支払い日時などの情報を保持します。
 */
@Entity
@Table(name = "payments")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")  // // JSON変換時の循環参照を防止するための設定（オブジェクトIDを使う）
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    /**
     * 支払い情報の一意な識別子（主キー）。
     * データベースで自動採番されます。
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * この支払いが紐づく予約情報。
     * 外部キーとしてappointmentsテーブルのIDを参照します。
     * 支払いは1つの予約に対して1つだけ存在します。
     */
    @NotNull(message = "予約情報は必須です。")
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "appointment_id", nullable = false)
    private Appointment appointment;

    /**
     * 支払い金額（日本円）。
     * 円単位での整数管理（小数なし）。
     */
    @Column(nullable = true, precision = 10, scale = 0)
    private BigDecimal amount;

    /**
     * 支払い方法を表す列挙型。
     * 現状は現金（cash）、クレジットカード（credit）、保険（insurance）が指定可能です。
     */
    public enum PaymentMethod {
        cash, credit, insurance
    }

    /**
     * 支払い方法。
     * 'cash', 'credit', 'insurance' のいずれかの値が格納されます。
     * データベースには文字列として保存されます。
     */
    @NotNull(message = "支払い方法は必須です。")
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 20)
    private PaymentMethod paymentMethod;

    /**
     * 支払いステータスを表す列挙型。
     * 支払いが完了した（Paid）、保留中（Pending）、失敗した（Failed）を示します。
     */
    public enum PaymentStatus {
        Paid, Pending, Failed
    }

    /**
     * 支払いの状態を示すステータス。
     * 'Paid', 'Pending', 'Failed' のいずれかの値が格納されます。
     * 支払い状況の管理に使用されます。
     */
    @NotNull(message = "支払いステータスは必須です。")
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 20)
    private PaymentStatus paymentStatus;

    /**
     * 実際に支払いが行われた日時。
     * 支払いが未完了の場合はnullとなります。
     */
    @Column(name = "paid_at", columnDefinition = "DATETIME")
    private LocalDateTime paidAt;

    /**
     * レコードの作成日時。
     * データベースへの登録時に自動的に現在日時がセットされ、
     * 以後の更新は行いません。
     */
    @NotNull(message = "作成日時は必須です。")
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "DATETIME")
    private LocalDateTime createdAt;

    /**
     * エンティティが新規に永続化される際に呼び出され、
     * createdAtフィールドに現在日時を自動設定します。
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
