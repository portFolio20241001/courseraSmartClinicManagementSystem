package com.project.back_end.repo;

import com.project.back_end.Entity.PrescriptionForMongo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 処方箋（Prescription）に関する MongoDB リポジトリインターフェース。
 * MongoRepository を継承し、基本的なCRUD機能を提供する。
 */
@Repository
public interface PrescriptionRepository extends MongoRepository<PrescriptionForMongo, String> {

    /**
     * 特定の予約（Appointment）に関連付けられた処方箋リストを取得する。
     * - メソッド名から自動的にクエリが生成される（予約IDで検索）。
     *
     * @param appointmentId 対象の予約ID
     * @return 該当する処方箋のリスト
     */
    List<PrescriptionForMongo> findByAppointmentId(Long appointmentId);
    
    /**
     * 指定した予約IDに紐づく処方箋が存在するか確認する。
     *
     * @param appointmentId 対象の予約ID
     * @return 処方箋が存在すれば true、存在しなければ false
     */
    boolean existsByAppointmentId(Long appointmentId);
    
}
