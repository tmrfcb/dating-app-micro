package com.tmrfcb.datingapp.repository;

import com.tmrfcb.datingapp.domain.Facebook;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data SQL repository for the Facebook entity.
 */
@SuppressWarnings("unused")
@Repository
public interface FacebookRepository extends JpaRepository<Facebook, Long> {}
