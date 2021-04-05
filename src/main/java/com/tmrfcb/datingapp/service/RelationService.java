package com.tmrfcb.datingapp.service;

import com.tmrfcb.datingapp.domain.Relation;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link Relation}.
 */
public interface RelationService {
    /**
     * Save a relation.
     *
     * @param relation the entity to save.
     * @return the persisted entity.
     */
    Relation save(Relation relation);

    /**
     * Partially updates a relation.
     *
     * @param relation the entity to update partially.
     * @return the persisted entity.
     */
    Optional<Relation> partialUpdate(Relation relation);

    /**
     * Get all the relations.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<Relation> findAll(Pageable pageable);

    /**
     * Get the "id" relation.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<Relation> findOne(Long id);

    /**
     * Delete the "id" relation.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);

    /**
     * Search for the relation corresponding to the query.
     *
     * @param query the query of the search.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<Relation> search(String query, Pageable pageable);
}
