package com.tmrfcb.datingapp.service.impl;

import static org.elasticsearch.index.query.QueryBuilders.*;

import com.tmrfcb.datingapp.domain.UnmatchRelation;
import com.tmrfcb.datingapp.repository.UnmatchRelationRepository;
import com.tmrfcb.datingapp.repository.search.UnmatchRelationSearchRepository;
import com.tmrfcb.datingapp.service.UnmatchRelationService;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link UnmatchRelation}.
 */
@Service
@Transactional
public class UnmatchRelationServiceImpl implements UnmatchRelationService {

    private final Logger log = LoggerFactory.getLogger(UnmatchRelationServiceImpl.class);

    private final UnmatchRelationRepository unmatchRelationRepository;

    private final UnmatchRelationSearchRepository unmatchRelationSearchRepository;

    public UnmatchRelationServiceImpl(
        UnmatchRelationRepository unmatchRelationRepository,
        UnmatchRelationSearchRepository unmatchRelationSearchRepository
    ) {
        this.unmatchRelationRepository = unmatchRelationRepository;
        this.unmatchRelationSearchRepository = unmatchRelationSearchRepository;
    }

    @Override
    public UnmatchRelation save(UnmatchRelation unmatchRelation) {
        log.debug("Request to save UnmatchRelation : {}", unmatchRelation);
        UnmatchRelation result = unmatchRelationRepository.save(unmatchRelation);
        unmatchRelationSearchRepository.save(result);
        return result;
    }

    @Override
    public Optional<UnmatchRelation> partialUpdate(UnmatchRelation unmatchRelation) {
        log.debug("Request to partially update UnmatchRelation : {}", unmatchRelation);

        return unmatchRelationRepository
            .findById(unmatchRelation.getId())
            .map(
                existingUnmatchRelation -> {
                    if (unmatchRelation.getUnMatchDate() != null) {
                        existingUnmatchRelation.setUnMatchDate(unmatchRelation.getUnMatchDate());
                    }

                    return existingUnmatchRelation;
                }
            )
            .map(unmatchRelationRepository::save)
            .map(
                savedUnmatchRelation -> {
                    unmatchRelationSearchRepository.save(savedUnmatchRelation);

                    return savedUnmatchRelation;
                }
            );
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UnmatchRelation> findAll(Pageable pageable) {
        log.debug("Request to get all UnmatchRelations");
        return unmatchRelationRepository.findAll(pageable);
    }

    /**
     *  Get all the unmatchRelations where Relation is {@code null}.
     *  @return the list of entities.
     */
    @Transactional(readOnly = true)
    public List<UnmatchRelation> findAllWhereRelationIsNull() {
        log.debug("Request to get all unmatchRelations where Relation is null");
        return StreamSupport
            .stream(unmatchRelationRepository.findAll().spliterator(), false)
            .filter(unmatchRelation -> unmatchRelation.getRelation() == null)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UnmatchRelation> findOne(Long id) {
        log.debug("Request to get UnmatchRelation : {}", id);
        return unmatchRelationRepository.findById(id);
    }

    @Override
    public void delete(Long id) {
        log.debug("Request to delete UnmatchRelation : {}", id);
        unmatchRelationRepository.deleteById(id);
        unmatchRelationSearchRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UnmatchRelation> search(String query, Pageable pageable) {
        log.debug("Request to search for a page of UnmatchRelations for query {}", query);
        return unmatchRelationSearchRepository.search(queryStringQuery(query), pageable);
    }
}
