package com.tmrfcb.datingapp.service;

import com.tmrfcb.datingapp.domain.MatchRelation;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link MatchRelation}.
 */
public interface MatchRelationService {
    /**
     * Save a matchRelation.
     *
     * @param matchRelation the entity to save.
     * @return the persisted entity.
     */
    MatchRelation save(MatchRelation matchRelation);

    /**
     * Partially updates a matchRelation.
     *
     * @param matchRelation the entity to update partially.
     * @return the persisted entity.
     */
    Optional<MatchRelation> partialUpdate(MatchRelation matchRelation);

    /**
     * Get all the matchRelations.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<MatchRelation> findAll(Pageable pageable);
    /**
     * Get all the MatchRelation where Relation is {@code null}.
     *
     * @return the {@link List} of entities.
     */
    List<MatchRelation> findAllWhereRelationIsNull();

    /**
     * Get the "id" matchRelation.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<MatchRelation> findOne(Long id);

    /**
     * Delete the "id" matchRelation.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);

    /**
     * Search for the matchRelation corresponding to the query.
     *
     * @param query the query of the search.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<MatchRelation> search(String query, Pageable pageable);
}
