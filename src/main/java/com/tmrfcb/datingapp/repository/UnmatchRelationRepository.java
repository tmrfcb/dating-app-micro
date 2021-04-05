package com.tmrfcb.datingapp.repository;

import com.tmrfcb.datingapp.domain.UnmatchRelation;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data SQL repository for the UnmatchRelation entity.
 */
@SuppressWarnings("unused")
@Repository
public interface UnmatchRelationRepository extends JpaRepository<UnmatchRelation, Long> {}
