package com.project.back_end.Entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.*;
import jakarta.validation.constraints.*;

/**
 * @Documentアノテーション:
 * - このクラスはMongoDBの"prescriptions"コレクションに対応するドキュメントを表します。
 */
@Document(collection = "prescriptions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrescriptionForMongo {

    /**
     * MongoDB固有のドキュメントID。
     * @Idアノテーションで主キーを示す。
     */
    @Id
    private String id;

    /**
     * 患者の名（MySQLのpatientsテーブルと紐づく値）。
     * @NotNullで必須項目。
     */
    @NotNull(message = "患者Idは必須です。")
    private String patientId;

    /**
     * 予約（アポイントメント）のID（MySQLのappointmentsテーブルのID）。
     * @NotNullで必須項目。
     */
    @NotNull(message = "予約IDは必須です。")
    private Long appointmentId;

    /**
     * 処方された薬剤名。
     * @NotNullかつ3文字以上100文字以下の文字数制限。
     */
    @NotNull(message = "薬剤名は必須です。")
    @Size(min = 3, max = 100, message = "薬剤名は3文字以上100文字以内で入力してください。")
    private String medication;

    /**
     * 用量情報。
     * @NotNullかつ3文字以上20文字以下の文字数制限。
     */
    @NotNull(message = "用量は必須です。")
    @Size(min = 3, max = 20, message = "用量は3文字以上20文字以内で入力してください。")
    private String dosage;

    /**
     * 医師からの追加指示やメモ。
     * 最大200文字に制限。
     */
    @Size(max = 200, message = "医師の指示は200文字以内で入力してください。")
    private String doctorNotes;

}
