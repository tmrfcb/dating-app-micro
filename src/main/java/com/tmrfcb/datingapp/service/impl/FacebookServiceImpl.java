package com.tmrfcb.datingapp.service.impl;

import static org.elasticsearch.index.query.QueryBuilders.*;

import com.tmrfcb.datingapp.domain.Facebook;
import com.tmrfcb.datingapp.repository.FacebookRepository;
import com.tmrfcb.datingapp.repository.search.FacebookSearchRepository;
import com.tmrfcb.datingapp.service.FacebookService;
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
 * Service Implementation for managing {@link Facebook}.
 */
@Service
@Transactional
public class FacebookServiceImpl implements FacebookService {

    private final Logger log = LoggerFactory.getLogger(FacebookServiceImpl.class);

    private final FacebookRepository facebookRepository;

    private final FacebookSearchRepository facebookSearchRepository;

    public FacebookServiceImpl(FacebookRepository facebookRepository, FacebookSearchRepository facebookSearchRepository) {
        this.facebookRepository = facebookRepository;
        this.facebookSearchRepository = facebookSearchRepository;
    }

    @Override
    public Facebook save(Facebook facebook) {
        log.debug("Request to save Facebook : {}", facebook);
        Facebook result = facebookRepository.save(facebook);
        facebookSearchRepository.save(result);
        return result;
    }

    @Override
    public Optional<Facebook> partialUpdate(Facebook facebook) {
        log.debug("Request to partially update Facebook : {}", facebook);

        return facebookRepository
            .findById(facebook.getId())
            .map(
                existingFacebook -> {
                    return existingFacebook;
                }
            )
            .map(facebookRepository::save)
            .map(
                savedFacebook -> {
                    facebookSearchRepository.save(savedFacebook);

                    return savedFacebook;
                }
            );
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Facebook> findAll(Pageable pageable) {
        log.debug("Request to get all Facebooks");
        return facebookRepository.findAll(pageable);
    }

    /**
     *  Get all the facebooks where UserApp is {@code null}.
     *  @return the list of entities.
     */
    @Transactional(readOnly = true)
    public List<Facebook> findAllWhereUserAppIsNull() {
        log.debug("Request to get all facebooks where UserApp is null");
        return StreamSupport
            .stream(facebookRepository.findAll().spliterator(), false)
            .filter(facebook -> facebook.getUserApp() == null)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Facebook> findOne(Long id) {
        log.debug("Request to get Facebook : {}", id);
        return facebookRepository.findById(id);
    }

    @Override
    public void delete(Long id) {
        log.debug("Request to delete Facebook : {}", id);
        facebookRepository.deleteById(id);
        facebookSearchRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Facebook> search(String query, Pageable pageable) {
        log.debug("Request to search for a page of Facebooks for query {}", query);
        return facebookSearchRepository.search(queryStringQuery(query), pageable);
    }
}
