package com.project.back_end.DTO;

import java.time.LocalDateTime;

import com.project.back_end.Entity.Appointment;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 診察予約DTOクラス - エンティティとクライアント間のデータ転送に使用
 */
@Data // Lombokで全フィールドのGetter/Setter、toString, equals, hashCodeを自動生成
@AllArgsConstructor // 全フィールドを引数に取るコンストラクタを自動生成
public class AppointmentDTO {

    // 診察予約のID（主キー）
    private Long id;

    // 医師ID（Doctorオブジェクトの代わりにIDを持つ）
	@NotNull(message = "医師IDは必須です。")
    private Long doctorId;

    // 患者ID（Patientオブジェクトの代わりにIDを持つ）
	@NotNull(message = "患者IDは必須です。")
    private Long patientId;
	
	private String patientName;

    // 診察予約の日時（開始時間）
    @NotNull(message = "予約日時は必須です。")
    @Future(message = "予約日時は未来の日時を指定してください。")
    private LocalDateTime appointmentTime;

    // ステータス（0: 予約済み, 1: 完了, 2: キャンセル など）
	@NotNull(message = "statusは必須です。0~2を指定ください。")
    private int status;
    
	 // --- 支払い情報 ---
	private PaymentDTO payment; // ← Payment情報をまとめて持つ
	
	// AppointmentエンティティからDTOを生成するコンストラクタ
	public AppointmentDTO(Appointment appointment) {
	    
	    this.id = appointment.getId(); // 予約IDを設定
	    this.doctorId = appointment.getDoctor().getId(); // 医師IDを設定（DoctorエンティティのIDを取得）
	    this.patientId = appointment.getPatient().getId(); // 患者IDを設定（PatientエンティティのIDを取得）
	    this.patientName = appointment.getPatient().getUser().getFullName();  // 明示的に参照
	    this.appointmentTime = appointment.getAppointmentTime();	// 予約日時（開始時間）を設定

	    // ステータスを設定（0: 予約済み, 1: 完了, 2: キャンセル）
	    this.status = appointment.getStatus();

	    // Payment情報が存在する場合、DTOに変換して設定
	    if (appointment.getPayment() != null) {
	        this.payment = new PaymentDTO(
	            appointment.getPayment().getAmount(),                          // 支払い金額
	            appointment.getPayment().getPaymentMethod().toString(),      // 支払い方法（Enum → 文字列）
	            appointment.getPayment().getPaymentStatus().toString()       // 支払いステータス（Enum → 文字列）
	        );
	    }
	}



}
