package com.tmrfcb.datingapp.service;

import com.tmrfcb.datingapp.domain.Location;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link Location}.
 */
public interface LocationService {
    /**
     * Save a location.
     *
     * @param location the entity to save.
     * @return the persisted entity.
     */
    Location save(Location location);

    /**
     * Partially updates a location.
     *
     * @param location the entity to update partially.
     * @return the persisted entity.
     */
    Optional<Location> partialUpdate(Location location);

    /**
     * Get all the locations.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<Location> findAll(Pageable pageable);
    /**
     * Get all the Location where Country is {@code null}.
     *
     * @return the {@link List} of entities.
     */
    List<Location> findAllWhereCountryIsNull();

    /**
     * Get the "id" location.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<Location> findOne(Long id);

    /**
     * Delete the "id" location.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);

    /**
     * Search for the location corresponding to the query.
     *
     * @param query the query of the search.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<Location> search(String query, Pageable pageable);
}
