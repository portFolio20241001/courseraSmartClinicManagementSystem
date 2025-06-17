package com.project.back_end.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.project.back_end.Entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // username で検索したいとき用
    Optional<User> findByUsername(String username);

    // username が既に存在しているかチェックしたいとき用
    boolean existsByUsername(String username);
}
