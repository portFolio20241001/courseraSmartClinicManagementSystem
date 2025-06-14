package com.project.back_end.Entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.*;
import java.time.Instant;

/**
 * MongoDBコレクション「feedbacks」に対応するドキュメントエンティティ。
 * 患者によるクリニックへのフィードバックを表す。
 */
@Document(collection = "feedbacks")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedbackForMongo {

    /**
     * MongoDBのドキュメントID。
     * 自動的に生成される一意な識別子。
     */
    @Id
    private String id;

    /**
     * 患者ID（MySQLのpatients.idと対応）。
     * フィードバックを投稿した患者を示す。
     */
    @NotNull(message = "患者IDは必須です。")
    private Long patientId;

    /**
     * クリニックID（MySQLのclinics.idと対応）。
     * フィードバックの対象となるクリニックを示す。
     */
    @NotNull(message = "クリニックIDは必須です。")
    private Long clinicId;

    /**
     * 評価スコア（1〜5）。
     * 医療サービスに対する評価点。
     */
    @Min(value = 1, message = "評価は1以上である必要があります。")
    @Max(value = 5, message = "評価は5以下である必要があります。")
    private int rating;

    /**
     * コメント（任意）。
     * 医師やクリニックに対する自由記述のフィードバック。
     */
    @Size(max = 200, message = "コメントは200文字以内で入力してください。")
    private String comments;

    /**
     * フィードバックの投稿日時（ISO 8601形式）。
     * UTCタイムゾーンのInstantを使用。
     */
    @NotNull(message = "作成日時は必須です。")
    private Instant createdAt;
}
