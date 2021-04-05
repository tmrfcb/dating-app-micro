package com.tmrfcb.datingapp.web.rest;

import static org.elasticsearch.index.query.QueryBuilders.*;

import com.tmrfcb.datingapp.domain.MatchRelation;
import com.tmrfcb.datingapp.repository.MatchRelationRepository;
import com.tmrfcb.datingapp.service.MatchRelationService;
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
 * REST controller for managing {@link com.tmrfcb.datingapp.domain.MatchRelation}.
 */
@RestController
@RequestMapping("/api")
public class MatchRelationResource {

    private final Logger log = LoggerFactory.getLogger(MatchRelationResource.class);

    private static final String ENTITY_NAME = "datingAppMatchRelation";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final MatchRelationService matchRelationService;

    private final MatchRelationRepository matchRelationRepository;

    public MatchRelationResource(MatchRelationService matchRelationService, MatchRelationRepository matchRelationRepository) {
        this.matchRelationService = matchRelationService;
        this.matchRelationRepository = matchRelationRepository;
    }

    /**
     * {@code POST  /match-relations} : Create a new matchRelation.
     *
     * @param matchRelation the matchRelation to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new matchRelation, or with status {@code 400 (Bad Request)} if the matchRelation has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/match-relations")
    public ResponseEntity<MatchRelation> createMatchRelation(@RequestBody MatchRelation matchRelation) throws URISyntaxException {
        log.debug("REST request to save MatchRelation : {}", matchRelation);
        if (matchRelation.getId() != null) {
            throw new BadRequestAlertException("A new matchRelation cannot already have an ID", ENTITY_NAME, "idexists");
        }
        MatchRelation result = matchRelationService.save(matchRelation);
        return ResponseEntity
            .created(new URI("/api/match-relations/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /match-relations/:id} : Updates an existing matchRelation.
     *
     * @param id the id of the matchRelation to save.
     * @param matchRelation the matchRelation to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated matchRelation,
     * or with status {@code 400 (Bad Request)} if the matchRelation is not valid,
     * or with status {@code 500 (Internal Server Error)} if the matchRelation couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/match-relations/{id}")
    public ResponseEntity<MatchRelation> updateMatchRelation(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody MatchRelation matchRelation
    ) throws URISyntaxException {
        log.debug("REST request to update MatchRelation : {}, {}", id, matchRelation);
        if (matchRelation.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, matchRelation.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!matchRelationRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        MatchRelation result = matchRelationService.save(matchRelation);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, matchRelation.getId().toString()))
            .body(result);
    }

    /**
     * {@code PATCH  /match-relations/:id} : Partial updates given fields of an existing matchRelation, field will ignore if it is null
     *
     * @param id the id of the matchRelation to save.
     * @param matchRelation the matchRelation to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated matchRelation,
     * or with status {@code 400 (Bad Request)} if the matchRelation is not valid,
     * or with status {@code 404 (Not Found)} if the matchRelation is not found,
     * or with status {@code 500 (Internal Server Error)} if the matchRelation couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/match-relations/{id}", consumes = "application/merge-patch+json")
    public ResponseEntity<MatchRelation> partialUpdateMatchRelation(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody MatchRelation matchRelation
    ) throws URISyntaxException {
        log.debug("REST request to partial update MatchRelation partially : {}, {}", id, matchRelation);
        if (matchRelation.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, matchRelation.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!matchRelationRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<MatchRelation> result = matchRelationService.partialUpdate(matchRelation);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, matchRelation.getId().toString())
        );
    }

    /**
     * {@code GET  /match-relations} : get all the matchRelations.
     *
     * @param pageable the pagination information.
     * @param filter the filter of the request.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of matchRelations in body.
     */
    @GetMapping("/match-relations")
    public ResponseEntity<List<MatchRelation>> getAllMatchRelations(Pageable pageable, @RequestParam(required = false) String filter) {
        if ("relation-is-null".equals(filter)) {
            log.debug("REST request to get all MatchRelations where relation is null");
            return new ResponseEntity<>(matchRelationService.findAllWhereRelationIsNull(), HttpStatus.OK);
        }
        log.debug("REST request to get a page of MatchRelations");
        Page<MatchRelation> page = matchRelationService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /match-relations/:id} : get the "id" matchRelation.
     *
     * @param id the id of the matchRelation to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the matchRelation, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/match-relations/{id}")
    public ResponseEntity<MatchRelation> getMatchRelation(@PathVariable Long id) {
        log.debug("REST request to get MatchRelation : {}", id);
        Optional<MatchRelation> matchRelation = matchRelationService.findOne(id);
        return ResponseUtil.wrapOrNotFound(matchRelation);
    }

    /**
     * {@code DELETE  /match-relations/:id} : delete the "id" matchRelation.
     *
     * @param id the id of the matchRelation to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/match-relations/{id}")
    public ResponseEntity<Void> deleteMatchRelation(@PathVariable Long id) {
        log.debug("REST request to delete MatchRelation : {}", id);
        matchRelationService.delete(id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }

    /**
     * {@code SEARCH  /_search/match-relations?query=:query} : search for the matchRelation corresponding
     * to the query.
     *
     * @param query the query of the matchRelation search.
     * @param pageable the pagination information.
     * @return the result of the search.
     */
    @GetMapping("/_search/match-relations")
    public ResponseEntity<List<MatchRelation>> searchMatchRelations(@RequestParam String query, Pageable pageable) {
        log.debug("REST request to search for a page of MatchRelations for query {}", query);
        Page<MatchRelation> page = matchRelationService.search(query, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }
}
