package com.tmrfcb.datingapp.repository;

import com.tmrfcb.datingapp.domain.ReportUser;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data SQL repository for the ReportUser entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ReportUserRepository extends JpaRepository<ReportUser, Long> {}
