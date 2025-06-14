package com.project.back_end.Entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.*;
import java.time.Instant;

/**
 * MongoDBコレクション「activityLogs」に対応するエンティティクラス。
 * 患者などのユーザーが行った操作履歴を記録するログ。
 */
@Document(collection = "activityLogs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivityLogForMongo {

    /**
     * MongoDBのドキュメントID。
     * 自動生成される一意の識別子。
     */
    @Id
    private String id;

    /**
     * ログ対象となる患者のID（MySQLの patients.id に対応）。
     */
    @NotNull(message = "患者IDは必須です。")
    private Long patientId;

    /**
     * 操作を実行したユーザーの情報（userId と role）。
     * 通常は患者自身だが、スタッフなども可能。
     */
    @NotNull(message = "操作実行者の情報は必須です。")
    private PerformedBy performedBy;

    /**
     * 実行されたアクションの説明（例：予約を作成しました）。
     */
    @NotBlank(message = "アクション内容は必須です。")
    private String action;

    /**
     * アクションが行われた日時（ISO 8601形式）。
     */
    @NotNull(message = "日時は必須です。")
    private Instant timestamp;

    /**
     * アクションに関連する追加情報（予約IDやステータスなど）。
     */
    private Details details;

    /**
     * 内部クラス：操作を行ったユーザーの情報。
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PerformedBy {

        /**
         * 操作者のユーザーID。
         */
        @NotNull(message = "ユーザーIDは必須です。")
        private Long userId;

        /**
         * ユーザーの役割（例：patient, doctor, admin）。
         */
        @NotBlank(message = "役割は必須です。")
        private String role;
    }

    /**
     * 内部クラス：アクションに関連する詳細情報。
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Details {

        /**
         * 関連する予約のID（MySQLの appointments.id に対応）。
         */
        private Long appointmentId;

        /**
         * 予約などのステータス情報（例：Scheduled, Cancelled）。
         */
        private String status;
    }
}
