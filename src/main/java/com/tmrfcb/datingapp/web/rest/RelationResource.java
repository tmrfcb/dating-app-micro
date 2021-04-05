package com.tmrfcb.datingapp.web.rest;

import static org.elasticsearch.index.query.QueryBuilders.*;

import com.tmrfcb.datingapp.domain.Relation;
import com.tmrfcb.datingapp.repository.RelationRepository;
import com.tmrfcb.datingapp.service.RelationService;
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
 * REST controller for managing {@link com.tmrfcb.datingapp.domain.Relation}.
 */
@RestController
@RequestMapping("/api")
public class RelationResource {

    private final Logger log = LoggerFactory.getLogger(RelationResource.class);

    private static final String ENTITY_NAME = "datingAppRelation";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final RelationService relationService;

    private final RelationRepository relationRepository;

    public RelationResource(RelationService relationService, RelationRepository relationRepository) {
        this.relationService = relationService;
        this.relationRepository = relationRepository;
    }

    /**
     * {@code POST  /relations} : Create a new relation.
     *
     * @param relation the relation to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new relation, or with status {@code 400 (Bad Request)} if the relation has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/relations")
    public ResponseEntity<Relation> createRelation(@RequestBody Relation relation) throws URISyntaxException {
        log.debug("REST request to save Relation : {}", relation);
        if (relation.getId() != null) {
            throw new BadRequestAlertException("A new relation cannot already have an ID", ENTITY_NAME, "idexists");
        }
        Relation result = relationService.save(relation);
        return ResponseEntity
            .created(new URI("/api/relations/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /relations/:id} : Updates an existing relation.
     *
     * @param id the id of the relation to save.
     * @param relation the relation to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated relation,
     * or with status {@code 400 (Bad Request)} if the relation is not valid,
     * or with status {@code 500 (Internal Server Error)} if the relation couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/relations/{id}")
    public ResponseEntity<Relation> updateRelation(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody Relation relation
    ) throws URISyntaxException {
        log.debug("REST request to update Relation : {}, {}", id, relation);
        if (relation.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, relation.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!relationRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Relation result = relationService.save(relation);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, relation.getId().toString()))
            .body(result);
    }

    /**
     * {@code PATCH  /relations/:id} : Partial updates given fields of an existing relation, field will ignore if it is null
     *
     * @param id the id of the relation to save.
     * @param relation the relation to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated relation,
     * or with status {@code 400 (Bad Request)} if the relation is not valid,
     * or with status {@code 404 (Not Found)} if the relation is not found,
     * or with status {@code 500 (Internal Server Error)} if the relation couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/relations/{id}", consumes = "application/merge-patch+json")
    public ResponseEntity<Relation> partialUpdateRelation(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody Relation relation
    ) throws URISyntaxException {
        log.debug("REST request to partial update Relation partially : {}, {}", id, relation);
        if (relation.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, relation.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!relationRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<Relation> result = relationService.partialUpdate(relation);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, relation.getId().toString())
        );
    }

    /**
     * {@code GET  /relations} : get all the relations.
     *
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of relations in body.
     */
    @GetMapping("/relations")
    public ResponseEntity<List<Relation>> getAllRelations(Pageable pageable) {
        log.debug("REST request to get a page of Relations");
        Page<Relation> page = relationService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /relations/:id} : get the "id" relation.
     *
     * @param id the id of the relation to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the relation, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/relations/{id}")
    public ResponseEntity<Relation> getRelation(@PathVariable Long id) {
        log.debug("REST request to get Relation : {}", id);
        Optional<Relation> relation = relationService.findOne(id);
        return ResponseUtil.wrapOrNotFound(relation);
    }

    /**
     * {@code DELETE  /relations/:id} : delete the "id" relation.
     *
     * @param id the id of the relation to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/relations/{id}")
    public ResponseEntity<Void> deleteRelation(@PathVariable Long id) {
        log.debug("REST request to delete Relation : {}", id);
        relationService.delete(id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }

    /**
     * {@code SEARCH  /_search/relations?query=:query} : search for the relation corresponding
     * to the query.
     *
     * @param query the query of the relation search.
     * @param pageable the pagination information.
     * @return the result of the search.
     */
    @GetMapping("/_search/relations")
    public ResponseEntity<List<Relation>> searchRelations(@RequestParam String query, Pageable pageable) {
        log.debug("REST request to search for a page of Relations for query {}", query);
        Page<Relation> page = relationService.search(query, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }
}
