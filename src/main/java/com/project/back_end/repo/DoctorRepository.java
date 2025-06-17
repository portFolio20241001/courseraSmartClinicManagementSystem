package com.project.back_end.repo;

import com.project.back_end.Entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Doctor（医師）情報をデータベースから取得・操作するためのリポジトリインターフェース。
 */
@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {
	
	
	@Query("SELECT d FROM Doctor d JOIN FETCH d.availableTimes JOIN FETCH d.user JOIN FETCH d.clinicLocation")
	List<Doctor> findAllWithUser();

    /**
     * ユーザー名（Userエンティティのusername）で医師情報を取得する。
     *
     * @param username ユーザー名
     * @return 該当するDoctorエンティティ
     */
    @Query("SELECT d FROM Doctor d WHERE d.user.username = :username")
    Doctor findByUsername(String username);
    
    /**
     * ユーザー名（User.username）で医師を {@link Optional} で取得します。
     *
     * @param username ユーザー名
     * @return 該当する {@link Doctor} を含む Optional（存在しない場合は空）
     */
    @Query("SELECT d FROM Doctor d JOIN FETCH d.user WHERE d.user.username = :username")
    Optional<Doctor> findByUser_Username(String username);

    
    /**
     * ユーザー名（User.username）で医師を {@link Optional} で取得します。
     *
     * @param useid
     * @return 該当する {@link Doctor} を含む Optional（存在しない場合は空）
     */
    @Query("SELECT d FROM Doctor d JOIN FETCH d.user WHERE d.user.id = :id")
    Optional<Doctor> findByUser_UserId(Long id);
    
    
    /**
     * 名前（User.fullName）に部分一致する医師一覧を取得（大文字・小文字を区別せず）。
     *
     * @param name 医師名の一部
     * @return 一致するDoctorエンティティのリスト
     */
    @Query("SELECT d FROM Doctor d "
    		+ "WHERE LOWER(d.user.fullName) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Doctor> findByFullNameLikeIgnoreCase(
    		@Param("name") String name);

    /**
     * 名前（User.fullName）に部分一致、かつ専門分野に完全一致（いずれも大文字小文字を無視）する医師一覧を取得。
     *
     * @param name 名前の一部
     * @param specialty 専門分野
     * @return 一致するDoctorエンティティのリスト
     */
    @Query("SELECT d FROM Doctor d  JOIN FETCH d.user JOIN FETCH d.clinicLocation "
    		+ "WHERE LOWER(d.user.fullName) LIKE LOWER(CONCAT('%', :name, '%')) "
    		+ "AND LOWER(d.specialty) = LOWER(:specialty)")
    List<Doctor> findByFullNameContainingIgnoreCaseAndSpecialtyIgnoreCase(
    		@Param("name") String name, 
    		@Param("specialty") String specialty);



    /**
     * 指定された名前に部分一致する医師一覧を取得（大文字小文字無視）。
     *
     * @param name 医師の名前（部分一致）
     * @return 一致するDoctorエンティティのリスト
     */
    @Query("SELECT d FROM Doctor d JOIN FETCH d.user JOIN FETCH d.clinicLocation " +
           "WHERE LOWER(d.user.fullName) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Doctor> findByFullNameContainingIgnoreCase(@Param("name") String name);

    /**
     * 指定された専門分野（大文字小文字無視）に一致する医師一覧を取得。
     *
     * @param specialty 専門分野
     * @return 一致するDoctorエンティティのリスト
     */
    @Query("SELECT d FROM Doctor d JOIN FETCH d.user JOIN FETCH d.clinicLocation " +
           "WHERE LOWER(d.specialty) = LOWER(:specialty)")
    List<Doctor> findBySpecialtyIgnoreCase(@Param("specialty") String specialty);

    /**
     * すべての医師情報を取得（関連するUser・Clinicも一括取得）。
     *
     * @return 全てのDoctorエンティティのリスト
     */
    @Query("SELECT d FROM Doctor d JOIN FETCH d.user JOIN FETCH d.clinicLocation")
    List<Doctor> findAll();
    
    /**
     * 指定されたユーザー名を持つ医師が存在するかどうかを判定します。
     *
     * @param username ユーザー名
     * @return 医師が存在する場合は {@code true}、存在しない場合は {@code false}
     */
    boolean existsByUser_Username(String username);
    
    
    
}
