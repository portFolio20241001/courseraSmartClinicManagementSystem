package com.project.back_end.repo;

import com.project.back_end.Entity.Appointment;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository  // SpringがこのインタフェースをJPAリポジトリとして認識するためのアノテーション
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

	/**
	 * 指定医師の予約を「日付」単位で取得  
	 * 例: 2025-06-15 00:00 〜 2025-06-15 23:59 の間に入っている予約
	 */
    @Query("SELECT a FROM Appointment a " +
    		     " LEFT JOIN FETCH a.doctor d " +
    		     "WHERE d.id = :doctorId " +
    		     "AND a.appointmentTime BETWEEN :startOfDay AND :endOfDay")
    List<Appointment> findByDoctorIdAndAppointmentTimeBetween(
            @Param("doctorId") Long doctorId,
            @Param("start") LocalDateTime startOfDay,
            @Param("end") LocalDateTime endOfDay
    );

    /**
     * 指定された医師ID・患者名（大文字小文字区別なし）・時間範囲に一致する予約を取得
     */
    @Query("SELECT a FROM Appointment a " +
    	       "LEFT JOIN FETCH a.doctor d " +
    	       "LEFT JOIN FETCH a.patient p " +
    	       "LEFT JOIN FETCH p.user u " +
    	       "WHERE d.id = :doctorId " +
    	       "AND LOWER(u.fullName) LIKE LOWER(CONCAT('%', :patientName, '%')) " +
    	       "AND a.appointmentTime BETWEEN :start AND :end")
    	List<Appointment> findByDoctorIdAndPatientFullNameContainingIgnoreCaseAndAppointmentTimeBetween(
    	        @Param("doctorId") Long doctorId,
    	        @Param("patientName") String patientName,
    	        @Param("start") LocalDateTime start,
    	        @Param("end") LocalDateTime end);


    /**
     * 指定された医師IDに関連するすべての予約を削除する
     * 更新系クエリのため @Modifying と @Transactional が必要
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM Appointment a WHERE a.doctor.id = :doctorId")
    void deleteAllByDoctorId(@Param("doctorId") Long doctorId);

    /**
     * 指定された患者IDに関連するすべての予約を取得する
     */
    List<Appointment> findByPatientId(Long patientId);

    /**
     * 指定された患者IDとステータスに一致する予約を取得（予約時間の昇順でソート）
     */
    List<Appointment> findByPatient_IdAndStatusOrderByAppointmentTimeAsc(Long patientId, int status);

    /**
     * 医師の氏名（User.fullName）に部分一致し、患者IDに一致する予約を取得（LIKE検索）
     */
    @Query("SELECT a FROM Appointment a "
         + "WHERE LOWER(a.doctor.user.fullName) LIKE LOWER(CONCAT('%', :doctorName, '%')) "
         + "AND a.patient.id = :patientId")
    List<Appointment> filterByDoctorNameAndPatientId(
            @Param("doctorName") String doctorName, 
            @Param("patientId") Long patientId);

    /**
     * 医師の氏名（User.fullName）に部分一致し、かつ患者IDおよび予約ステータスに一致する予約を取得
     */
    @Query("SELECT a FROM Appointment a "
         + "WHERE LOWER(a.doctor.user.fullName) LIKE LOWER(CONCAT('%', :doctorName, '%')) "
         + "AND a.patient.id = :patientId AND a.status = :status")
    List<Appointment> filterByDoctorNameAndPatientIdAndStatus(
            @Param("doctorName") String doctorName,
            @Param("patientId") Long patientId,
            @Param("status") int status);


    /**
     * 指定された予約IDのステータスを更新する
     * 更新系クエリには @Modifying と @Transactional が必要
     */
    @Modifying
    @Transactional
    @Query("UPDATE Appointment a SET a.status = :status WHERE a.id = :id")
    void updateStatus(
    		@Param("status") int status, 
    		@Param("id") long id);
    
    /**
     * 指定された医師IDと予約日時に一致する予約一覧を取得します。
     * 
     * @param doctorId 医師のID
     * @param appointmentTime 予約日時（LocalDateTime）
     * @return 指定された医師と日時に該当する予約のリスト
     */
    @Query("SELECT a FROM Appointment a WHERE a.doctor.id = :doctorId AND a.appointmentTime = :appointmentTime")
    List<Appointment> findByDoctorIdAndAppointmentTime(
        @Param("doctorId") Long doctorId,
        @Param("appointmentTime") LocalDateTime appointmentTime);

    
    
}
