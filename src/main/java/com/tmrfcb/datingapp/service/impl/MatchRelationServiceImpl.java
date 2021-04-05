package com.tmrfcb.datingapp.service.impl;

import static org.elasticsearch.index.query.QueryBuilders.*;

import com.tmrfcb.datingapp.domain.MatchRelation;
import com.tmrfcb.datingapp.repository.MatchRelationRepository;
import com.tmrfcb.datingapp.repository.search.MatchRelationSearchRepository;
import com.tmrfcb.datingapp.service.MatchRelationService;
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
 * Service Implementation for managing {@link MatchRelation}.
 */
@Service
@Transactional
public class MatchRelationServiceImpl implements MatchRelationService {

    private final Logger log = LoggerFactory.getLogger(MatchRelationServiceImpl.class);

    private final MatchRelationRepository matchRelationRepository;

    private final MatchRelationSearchRepository matchRelationSearchRepository;

    public MatchRelationServiceImpl(
        MatchRelationRepository matchRelationRepository,
        MatchRelationSearchRepository matchRelationSearchRepository
    ) {
        this.matchRelationRepository = matchRelationRepository;
        this.matchRelationSearchRepository = matchRelationSearchRepository;
    }

    @Override
    public MatchRelation save(MatchRelation matchRelation) {
        log.debug("Request to save MatchRelation : {}", matchRelation);
        MatchRelation result = matchRelationRepository.save(matchRelation);
        matchRelationSearchRepository.save(result);
        return result;
    }

    @Override
    public Optional<MatchRelation> partialUpdate(MatchRelation matchRelation) {
        log.debug("Request to partially update MatchRelation : {}", matchRelation);

        return matchRelationRepository
            .findById(matchRelation.getId())
            .map(
                existingMatchRelation -> {
                    if (matchRelation.getMatchDate() != null) {
                        existingMatchRelation.setMatchDate(matchRelation.getMatchDate());
                    }

                    return existingMatchRelation;
                }
            )
            .map(matchRelationRepository::save)
            .map(
                savedMatchRelation -> {
                    matchRelationSearchRepository.save(savedMatchRelation);

                    return savedMatchRelation;
                }
            );
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MatchRelation> findAll(Pageable pageable) {
        log.debug("Request to get all MatchRelations");
        return matchRelationRepository.findAll(pageable);
    }

    /**
     *  Get all the matchRelations where Relation is {@code null}.
     *  @return the list of entities.
     */
    @Transactional(readOnly = true)
    public List<MatchRelation> findAllWhereRelationIsNull() {
        log.debug("Request to get all matchRelations where Relation is null");
        return StreamSupport
            .stream(matchRelationRepository.findAll().spliterator(), false)
            .filter(matchRelation -> matchRelation.getRelation() == null)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<MatchRelation> findOne(Long id) {
        log.debug("Request to get MatchRelation : {}", id);
        return matchRelationRepository.findById(id);
    }

    @Override
    public void delete(Long id) {
        log.debug("Request to delete MatchRelation : {}", id);
        matchRelationRepository.deleteById(id);
        matchRelationSearchRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MatchRelation> search(String query, Pageable pageable) {
        log.debug("Request to search for a page of MatchRelations for query {}", query);
        return matchRelationSearchRepository.search(queryStringQuery(query), pageable);
    }
}
