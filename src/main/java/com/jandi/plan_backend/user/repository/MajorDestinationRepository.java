package com.jandi.plan_backend.user.repository;

import com.jandi.plan_backend.user.entity.MajorDestination;
import com.jandi.plan_backend.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MajorDestinationRepository extends JpaRepository<MajorDestination, Long> {
    Optional<MajorDestination> findByName(String name);
    List<MajorDestination> findByNameIn(List<String> filter);
}
