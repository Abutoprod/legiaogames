package com.lanhouse.repository;

import com.lanhouse.model.VapidKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VapidKeyRepository extends JpaRepository<VapidKey, Long> {
}
