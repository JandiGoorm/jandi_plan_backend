package com.jandi.plan_backend.openai.repository;

import com.jandi.plan_backend.openai.entity.AiInteraction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiInteractionRepository extends JpaRepository<AiInteraction, Long> {
}
