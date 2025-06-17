package com.project.back_end.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.project.back_end.Entity.Admin;

/**
 * Admin（管理者）エンティティ用のリポジトリインタフェース。
 * Spring Data JPAにより、自動的に実装が提供されます。
 */
@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {

    /**
     * ユーザー名からAdminを検索。
     * AdminエンティティがUserに依存しているため、user.username で検索する。
     *
     * @param username ユーザー名
     * @return Adminエンティティ（存在しなければnullまたはOptional）
     */
	Optional<Admin> findByUser_Username(String username); // ← Userを検索し、その該当Userに紐づくAdminをGet
}

