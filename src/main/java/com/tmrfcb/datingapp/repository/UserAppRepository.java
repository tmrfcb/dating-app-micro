package com.tmrfcb.datingapp.repository;

import com.tmrfcb.datingapp.domain.UserApp;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data SQL repository for the UserApp entity.
 */
@SuppressWarnings("unused")
@Repository
public interface UserAppRepository extends JpaRepository<UserApp, Long> {}
