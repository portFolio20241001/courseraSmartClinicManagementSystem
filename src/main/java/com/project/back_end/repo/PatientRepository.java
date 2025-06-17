package com.project.back_end.repo;

import com.project.back_end.Entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository  // このインターフェースはSpringにより自動的に実装されるリポジトリ
public interface PatientRepository extends JpaRepository<Patient, Long> {

    /**
     * ユーザー名（User.username）からUserを介して患者（Patient）を検索。
     * - Patient は User エンティティと 1対1 で紐づいている前提。
     * - 内部結合のような形で、User.username が一致する Patient を取得。
     *
     * @param username 検索対象のユーザー名
     * @return 該当する Patient（存在しない場合は空）
     */
	@Query("SELECT p FROM Patient p JOIN FETCH p.user WHERE p.user.username = :username")
    Optional<Patient> findByUser_Username(String username);

    /**
     * ユーザー名（User.username）または患者の電話番号（Patient.phone）で検索。
     * - ユーザー名は User エンティティから、
     * - 電話番号は Patient エンティティ自身のフィールドから検索。
     * - いずれか一方が一致すれば該当の Patient を返す。
     *
     * @param username ユーザー名
     * @param phone 電話番号（患者の電話）
     * @return 該当する Patient（存在しない場合は空）
     */
    Optional<Patient> findByUser_UsernameOrPhone(String username, String phone);
    
    /**
     * 指定されたユーザー名を持つ患者が存在するかどうかを判定します。
     *
     * @param username ユーザー名
     * @return 患者が存在する場合は {@code true}、存在しない場合は {@code false}
     */
    boolean existsByUser_Username(String username);

}
