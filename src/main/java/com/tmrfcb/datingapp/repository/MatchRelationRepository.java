package com.tmrfcb.datingapp.repository;

import com.tmrfcb.datingapp.domain.MatchRelation;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data SQL repository for the MatchRelation entity.
 */
@SuppressWarnings("unused")
@Repository
public interface MatchRelationRepository extends JpaRepository<MatchRelation, Long> {}
