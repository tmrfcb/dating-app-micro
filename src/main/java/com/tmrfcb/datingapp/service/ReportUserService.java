package com.tmrfcb.datingapp.service;

import com.tmrfcb.datingapp.domain.ReportUser;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link ReportUser}.
 */
public interface ReportUserService {
    /**
     * Save a reportUser.
     *
     * @param reportUser the entity to save.
     * @return the persisted entity.
     */
    ReportUser save(ReportUser reportUser);

    /**
     * Partially updates a reportUser.
     *
     * @param reportUser the entity to update partially.
     * @return the persisted entity.
     */
    Optional<ReportUser> partialUpdate(ReportUser reportUser);

    /**
     * Get all the reportUsers.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<ReportUser> findAll(Pageable pageable);

    /**
     * Get the "id" reportUser.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<ReportUser> findOne(Long id);

    /**
     * Delete the "id" reportUser.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);

    /**
     * Search for the reportUser corresponding to the query.
     *
     * @param query the query of the search.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<ReportUser> search(String query, Pageable pageable);
}
