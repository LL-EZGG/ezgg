package com.matching.ezgg.global.jwt.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.matching.ezgg.global.jwt.entity.Refresh;

import jakarta.transaction.Transactional;

@Repository
public interface RefreshRepository extends JpaRepository<Refresh, Long> {
	@Transactional
	void deleteByRefresh(String refresh);

	boolean existsByRefresh(String refresh);
}
