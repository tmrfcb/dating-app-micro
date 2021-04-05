package com.tmrfcb.datingapp.repository;

import com.tmrfcb.datingapp.domain.Relation;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data SQL repository for the Relation entity.
 */
@SuppressWarnings("unused")
@Repository
public interface RelationRepository extends JpaRepository<Relation, Long> {}
