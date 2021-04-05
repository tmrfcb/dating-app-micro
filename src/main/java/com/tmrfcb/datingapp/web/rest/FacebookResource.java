package com.tmrfcb.datingapp.web.rest;

import static org.elasticsearch.index.query.QueryBuilders.*;

import com.tmrfcb.datingapp.domain.Facebook;
import com.tmrfcb.datingapp.repository.FacebookRepository;
import com.tmrfcb.datingapp.service.FacebookService;
import com.tmrfcb.datingapp.web.rest.errors.BadRequestAlertException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.StreamSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link com.tmrfcb.datingapp.domain.Facebook}.
 */
@RestController
@RequestMapping("/api")
public class FacebookResource {

    private final Logger log = LoggerFactory.getLogger(FacebookResource.class);

    private static final String ENTITY_NAME = "datingAppFacebook";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final FacebookService facebookService;

    private final FacebookRepository facebookRepository;

    public FacebookResource(FacebookService facebookService, FacebookRepository facebookRepository) {
        this.facebookService = facebookService;
        this.facebookRepository = facebookRepository;
    }

    /**
     * {@code POST  /facebooks} : Create a new facebook.
     *
     * @param facebook the facebook to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new facebook, or with status {@code 400 (Bad Request)} if the facebook has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/facebooks")
    public ResponseEntity<Facebook> createFacebook(@RequestBody Facebook facebook) throws URISyntaxException {
        log.debug("REST request to save Facebook : {}", facebook);
        if (facebook.getId() != null) {
            throw new BadRequestAlertException("A new facebook cannot already have an ID", ENTITY_NAME, "idexists");
        }
        Facebook result = facebookService.save(facebook);
        return ResponseEntity
            .created(new URI("/api/facebooks/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /facebooks/:id} : Updates an existing facebook.
     *
     * @param id the id of the facebook to save.
     * @param facebook the facebook to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated facebook,
     * or with status {@code 400 (Bad Request)} if the facebook is not valid,
     * or with status {@code 500 (Internal Server Error)} if the facebook couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/facebooks/{id}")
    public ResponseEntity<Facebook> updateFacebook(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody Facebook facebook
    ) throws URISyntaxException {
        log.debug("REST request to update Facebook : {}, {}", id, facebook);
        if (facebook.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, facebook.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!facebookRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Facebook result = facebookService.save(facebook);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, facebook.getId().toString()))
            .body(result);
    }

    /**
     * {@code PATCH  /facebooks/:id} : Partial updates given fields of an existing facebook, field will ignore if it is null
     *
     * @param id the id of the facebook to save.
     * @param facebook the facebook to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated facebook,
     * or with status {@code 400 (Bad Request)} if the facebook is not valid,
     * or with status {@code 404 (Not Found)} if the facebook is not found,
     * or with status {@code 500 (Internal Server Error)} if the facebook couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/facebooks/{id}", consumes = "application/merge-patch+json")
    public ResponseEntity<Facebook> partialUpdateFacebook(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody Facebook facebook
    ) throws URISyntaxException {
        log.debug("REST request to partial update Facebook partially : {}, {}", id, facebook);
        if (facebook.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, facebook.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!facebookRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<Facebook> result = facebookService.partialUpdate(facebook);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, facebook.getId().toString())
        );
    }

    /**
     * {@code GET  /facebooks} : get all the facebooks.
     *
     * @param pageable the pagination information.
     * @param filter the filter of the request.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of facebooks in body.
     */
    @GetMapping("/facebooks")
    public ResponseEntity<List<Facebook>> getAllFacebooks(Pageable pageable, @RequestParam(required = false) String filter) {
        if ("userapp-is-null".equals(filter)) {
            log.debug("REST request to get all Facebooks where userApp is null");
            return new ResponseEntity<>(facebookService.findAllWhereUserAppIsNull(), HttpStatus.OK);
        }
        log.debug("REST request to get a page of Facebooks");
        Page<Facebook> page = facebookService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /facebooks/:id} : get the "id" facebook.
     *
     * @param id the id of the facebook to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the facebook, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/facebooks/{id}")
    public ResponseEntity<Facebook> getFacebook(@PathVariable Long id) {
        log.debug("REST request to get Facebook : {}", id);
        Optional<Facebook> facebook = facebookService.findOne(id);
        return ResponseUtil.wrapOrNotFound(facebook);
    }

    /**
     * {@code DELETE  /facebooks/:id} : delete the "id" facebook.
     *
     * @param id the id of the facebook to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/facebooks/{id}")
    public ResponseEntity<Void> deleteFacebook(@PathVariable Long id) {
        log.debug("REST request to delete Facebook : {}", id);
        facebookService.delete(id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }

    /**
     * {@code SEARCH  /_search/facebooks?query=:query} : search for the facebook corresponding
     * to the query.
     *
     * @param query the query of the facebook search.
     * @param pageable the pagination information.
     * @return the result of the search.
     */
    @GetMapping("/_search/facebooks")
    public ResponseEntity<List<Facebook>> searchFacebooks(@RequestParam String query, Pageable pageable) {
        log.debug("REST request to search for a page of Facebooks for query {}", query);
        Page<Facebook> page = facebookService.search(query, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }
}
