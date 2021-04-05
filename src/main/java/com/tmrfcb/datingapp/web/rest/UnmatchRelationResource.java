package com.tmrfcb.datingapp.web.rest;

import static org.elasticsearch.index.query.QueryBuilders.*;

import com.tmrfcb.datingapp.domain.UnmatchRelation;
import com.tmrfcb.datingapp.repository.UnmatchRelationRepository;
import com.tmrfcb.datingapp.service.UnmatchRelationService;
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
 * REST controller for managing {@link com.tmrfcb.datingapp.domain.UnmatchRelation}.
 */
@RestController
@RequestMapping("/api")
public class UnmatchRelationResource {

    private final Logger log = LoggerFactory.getLogger(UnmatchRelationResource.class);

    private static final String ENTITY_NAME = "datingAppUnmatchRelation";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final UnmatchRelationService unmatchRelationService;

    private final UnmatchRelationRepository unmatchRelationRepository;

    public UnmatchRelationResource(UnmatchRelationService unmatchRelationService, UnmatchRelationRepository unmatchRelationRepository) {
        this.unmatchRelationService = unmatchRelationService;
        this.unmatchRelationRepository = unmatchRelationRepository;
    }

    /**
     * {@code POST  /unmatch-relations} : Create a new unmatchRelation.
     *
     * @param unmatchRelation the unmatchRelation to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new unmatchRelation, or with status {@code 400 (Bad Request)} if the unmatchRelation has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/unmatch-relations")
    public ResponseEntity<UnmatchRelation> createUnmatchRelation(@RequestBody UnmatchRelation unmatchRelation) throws URISyntaxException {
        log.debug("REST request to save UnmatchRelation : {}", unmatchRelation);
        if (unmatchRelation.getId() != null) {
            throw new BadRequestAlertException("A new unmatchRelation cannot already have an ID", ENTITY_NAME, "idexists");
        }
        UnmatchRelation result = unmatchRelationService.save(unmatchRelation);
        return ResponseEntity
            .created(new URI("/api/unmatch-relations/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /unmatch-relations/:id} : Updates an existing unmatchRelation.
     *
     * @param id the id of the unmatchRelation to save.
     * @param unmatchRelation the unmatchRelation to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated unmatchRelation,
     * or with status {@code 400 (Bad Request)} if the unmatchRelation is not valid,
     * or with status {@code 500 (Internal Server Error)} if the unmatchRelation couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/unmatch-relations/{id}")
    public ResponseEntity<UnmatchRelation> updateUnmatchRelation(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody UnmatchRelation unmatchRelation
    ) throws URISyntaxException {
        log.debug("REST request to update UnmatchRelation : {}, {}", id, unmatchRelation);
        if (unmatchRelation.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, unmatchRelation.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!unmatchRelationRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        UnmatchRelation result = unmatchRelationService.save(unmatchRelation);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, unmatchRelation.getId().toString()))
            .body(result);
    }

    /**
     * {@code PATCH  /unmatch-relations/:id} : Partial updates given fields of an existing unmatchRelation, field will ignore if it is null
     *
     * @param id the id of the unmatchRelation to save.
     * @param unmatchRelation the unmatchRelation to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated unmatchRelation,
     * or with status {@code 400 (Bad Request)} if the unmatchRelation is not valid,
     * or with status {@code 404 (Not Found)} if the unmatchRelation is not found,
     * or with status {@code 500 (Internal Server Error)} if the unmatchRelation couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/unmatch-relations/{id}", consumes = "application/merge-patch+json")
    public ResponseEntity<UnmatchRelation> partialUpdateUnmatchRelation(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody UnmatchRelation unmatchRelation
    ) throws URISyntaxException {
        log.debug("REST request to partial update UnmatchRelation partially : {}, {}", id, unmatchRelation);
        if (unmatchRelation.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, unmatchRelation.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!unmatchRelationRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<UnmatchRelation> result = unmatchRelationService.partialUpdate(unmatchRelation);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, unmatchRelation.getId().toString())
        );
    }

    /**
     * {@code GET  /unmatch-relations} : get all the unmatchRelations.
     *
     * @param pageable the pagination information.
     * @param filter the filter of the request.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of unmatchRelations in body.
     */
    @GetMapping("/unmatch-relations")
    public ResponseEntity<List<UnmatchRelation>> getAllUnmatchRelations(Pageable pageable, @RequestParam(required = false) String filter) {
        if ("relation-is-null".equals(filter)) {
            log.debug("REST request to get all UnmatchRelations where relation is null");
            return new ResponseEntity<>(unmatchRelationService.findAllWhereRelationIsNull(), HttpStatus.OK);
        }
        log.debug("REST request to get a page of UnmatchRelations");
        Page<UnmatchRelation> page = unmatchRelationService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /unmatch-relations/:id} : get the "id" unmatchRelation.
     *
     * @param id the id of the unmatchRelation to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the unmatchRelation, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/unmatch-relations/{id}")
    public ResponseEntity<UnmatchRelation> getUnmatchRelation(@PathVariable Long id) {
        log.debug("REST request to get UnmatchRelation : {}", id);
        Optional<UnmatchRelation> unmatchRelation = unmatchRelationService.findOne(id);
        return ResponseUtil.wrapOrNotFound(unmatchRelation);
    }

    /**
     * {@code DELETE  /unmatch-relations/:id} : delete the "id" unmatchRelation.
     *
     * @param id the id of the unmatchRelation to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/unmatch-relations/{id}")
    public ResponseEntity<Void> deleteUnmatchRelation(@PathVariable Long id) {
        log.debug("REST request to delete UnmatchRelation : {}", id);
        unmatchRelationService.delete(id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }

    /**
     * {@code SEARCH  /_search/unmatch-relations?query=:query} : search for the unmatchRelation corresponding
     * to the query.
     *
     * @param query the query of the unmatchRelation search.
     * @param pageable the pagination information.
     * @return the result of the search.
     */
    @GetMapping("/_search/unmatch-relations")
    public ResponseEntity<List<UnmatchRelation>> searchUnmatchRelations(@RequestParam String query, Pageable pageable) {
        log.debug("REST request to search for a page of UnmatchRelations for query {}", query);
        Page<UnmatchRelation> page = unmatchRelationService.search(query, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }
}
