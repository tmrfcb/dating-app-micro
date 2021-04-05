package com.tmrfcb.datingapp.web.rest;

import static org.elasticsearch.index.query.QueryBuilders.*;

import com.tmrfcb.datingapp.domain.UserApp;
import com.tmrfcb.datingapp.repository.UserAppRepository;
import com.tmrfcb.datingapp.repository.search.UserAppSearchRepository;
import com.tmrfcb.datingapp.web.rest.errors.BadRequestAlertException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link com.tmrfcb.datingapp.domain.UserApp}.
 */
@RestController
@RequestMapping("/api")
@Transactional
public class UserAppResource {

    private final Logger log = LoggerFactory.getLogger(UserAppResource.class);

    private static final String ENTITY_NAME = "datingAppUserApp";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final UserAppRepository userAppRepository;

    private final UserAppSearchRepository userAppSearchRepository;

    public UserAppResource(UserAppRepository userAppRepository, UserAppSearchRepository userAppSearchRepository) {
        this.userAppRepository = userAppRepository;
        this.userAppSearchRepository = userAppSearchRepository;
    }

    /**
     * {@code POST  /user-apps} : Create a new userApp.
     *
     * @param userApp the userApp to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new userApp, or with status {@code 400 (Bad Request)} if the userApp has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/user-apps")
    public ResponseEntity<UserApp> createUserApp(@RequestBody UserApp userApp) throws URISyntaxException {
        log.debug("REST request to save UserApp : {}", userApp);
        if (userApp.getId() != null) {
            throw new BadRequestAlertException("A new userApp cannot already have an ID", ENTITY_NAME, "idexists");
        }
        UserApp result = userAppRepository.save(userApp);
        userAppSearchRepository.save(result);
        return ResponseEntity
            .created(new URI("/api/user-apps/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /user-apps/:id} : Updates an existing userApp.
     *
     * @param id the id of the userApp to save.
     * @param userApp the userApp to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated userApp,
     * or with status {@code 400 (Bad Request)} if the userApp is not valid,
     * or with status {@code 500 (Internal Server Error)} if the userApp couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/user-apps/{id}")
    public ResponseEntity<UserApp> updateUserApp(@PathVariable(value = "id", required = false) final Long id, @RequestBody UserApp userApp)
        throws URISyntaxException {
        log.debug("REST request to update UserApp : {}, {}", id, userApp);
        if (userApp.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, userApp.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!userAppRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        UserApp result = userAppRepository.save(userApp);
        userAppSearchRepository.save(result);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, userApp.getId().toString()))
            .body(result);
    }

    /**
     * {@code PATCH  /user-apps/:id} : Partial updates given fields of an existing userApp, field will ignore if it is null
     *
     * @param id the id of the userApp to save.
     * @param userApp the userApp to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated userApp,
     * or with status {@code 400 (Bad Request)} if the userApp is not valid,
     * or with status {@code 404 (Not Found)} if the userApp is not found,
     * or with status {@code 500 (Internal Server Error)} if the userApp couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/user-apps/{id}", consumes = "application/merge-patch+json")
    public ResponseEntity<UserApp> partialUpdateUserApp(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody UserApp userApp
    ) throws URISyntaxException {
        log.debug("REST request to partial update UserApp partially : {}, {}", id, userApp);
        if (userApp.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, userApp.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!userAppRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<UserApp> result = userAppRepository
            .findById(userApp.getId())
            .map(
                existingUserApp -> {
                    if (userApp.getFirstName() != null) {
                        existingUserApp.setFirstName(userApp.getFirstName());
                    }
                    if (userApp.getLastName() != null) {
                        existingUserApp.setLastName(userApp.getLastName());
                    }
                    if (userApp.getEmail() != null) {
                        existingUserApp.setEmail(userApp.getEmail());
                    }
                    if (userApp.getPhoneNumber() != null) {
                        existingUserApp.setPhoneNumber(userApp.getPhoneNumber());
                    }
                    if (userApp.getBirthDate() != null) {
                        existingUserApp.setBirthDate(userApp.getBirthDate());
                    }

                    return existingUserApp;
                }
            )
            .map(userAppRepository::save)
            .map(
                savedUserApp -> {
                    userAppSearchRepository.save(savedUserApp);

                    return savedUserApp;
                }
            );

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, userApp.getId().toString())
        );
    }

    /**
     * {@code GET  /user-apps} : get all the userApps.
     *
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of userApps in body.
     */
    @GetMapping("/user-apps")
    public ResponseEntity<List<UserApp>> getAllUserApps(Pageable pageable) {
        log.debug("REST request to get a page of UserApps");
        Page<UserApp> page = userAppRepository.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /user-apps/:id} : get the "id" userApp.
     *
     * @param id the id of the userApp to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the userApp, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/user-apps/{id}")
    public ResponseEntity<UserApp> getUserApp(@PathVariable Long id) {
        log.debug("REST request to get UserApp : {}", id);
        Optional<UserApp> userApp = userAppRepository.findById(id);
        return ResponseUtil.wrapOrNotFound(userApp);
    }

    /**
     * {@code DELETE  /user-apps/:id} : delete the "id" userApp.
     *
     * @param id the id of the userApp to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/user-apps/{id}")
    public ResponseEntity<Void> deleteUserApp(@PathVariable Long id) {
        log.debug("REST request to delete UserApp : {}", id);
        userAppRepository.deleteById(id);
        userAppSearchRepository.deleteById(id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }

    /**
     * {@code SEARCH  /_search/user-apps?query=:query} : search for the userApp corresponding
     * to the query.
     *
     * @param query the query of the userApp search.
     * @param pageable the pagination information.
     * @return the result of the search.
     */
    @GetMapping("/_search/user-apps")
    public ResponseEntity<List<UserApp>> searchUserApps(@RequestParam String query, Pageable pageable) {
        log.debug("REST request to search for a page of UserApps for query {}", query);
        Page<UserApp> page = userAppSearchRepository.search(queryStringQuery(query), pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }
}
