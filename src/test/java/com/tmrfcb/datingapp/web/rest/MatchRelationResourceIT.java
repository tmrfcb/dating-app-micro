package com.tmrfcb.datingapp.web.rest;

import static com.tmrfcb.datingapp.web.rest.TestUtil.sameInstant;
import static org.assertj.core.api.Assertions.assertThat;
import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.tmrfcb.datingapp.IntegrationTest;
import com.tmrfcb.datingapp.domain.MatchRelation;
import com.tmrfcb.datingapp.repository.MatchRelationRepository;
import com.tmrfcb.datingapp.repository.search.MatchRelationSearchRepository;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import javax.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link MatchRelationResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class MatchRelationResourceIT {

    private static final ZonedDateTime DEFAULT_MATCH_DATE = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneOffset.UTC);
    private static final ZonedDateTime UPDATED_MATCH_DATE = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);

    private static final String ENTITY_API_URL = "/api/match-relations";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";
    private static final String ENTITY_SEARCH_API_URL = "/api/_search/match-relations";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private MatchRelationRepository matchRelationRepository;

    /**
     * This repository is mocked in the com.tmrfcb.datingapp.repository.search test package.
     *
     * @see com.tmrfcb.datingapp.repository.search.MatchRelationSearchRepositoryMockConfiguration
     */
    @Autowired
    private MatchRelationSearchRepository mockMatchRelationSearchRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restMatchRelationMockMvc;

    private MatchRelation matchRelation;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static MatchRelation createEntity(EntityManager em) {
        MatchRelation matchRelation = new MatchRelation().matchDate(DEFAULT_MATCH_DATE);
        return matchRelation;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static MatchRelation createUpdatedEntity(EntityManager em) {
        MatchRelation matchRelation = new MatchRelation().matchDate(UPDATED_MATCH_DATE);
        return matchRelation;
    }

    @BeforeEach
    public void initTest() {
        matchRelation = createEntity(em);
    }

    @Test
    @Transactional
    void createMatchRelation() throws Exception {
        int databaseSizeBeforeCreate = matchRelationRepository.findAll().size();
        // Create the MatchRelation
        restMatchRelationMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(matchRelation)))
            .andExpect(status().isCreated());

        // Validate the MatchRelation in the database
        List<MatchRelation> matchRelationList = matchRelationRepository.findAll();
        assertThat(matchRelationList).hasSize(databaseSizeBeforeCreate + 1);
        MatchRelation testMatchRelation = matchRelationList.get(matchRelationList.size() - 1);
        assertThat(testMatchRelation.getMatchDate()).isEqualTo(DEFAULT_MATCH_DATE);

        // Validate the MatchRelation in Elasticsearch
        verify(mockMatchRelationSearchRepository, times(1)).save(testMatchRelation);
    }

    @Test
    @Transactional
    void createMatchRelationWithExistingId() throws Exception {
        // Create the MatchRelation with an existing ID
        matchRelation.setId(1L);

        int databaseSizeBeforeCreate = matchRelationRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restMatchRelationMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(matchRelation)))
            .andExpect(status().isBadRequest());

        // Validate the MatchRelation in the database
        List<MatchRelation> matchRelationList = matchRelationRepository.findAll();
        assertThat(matchRelationList).hasSize(databaseSizeBeforeCreate);

        // Validate the MatchRelation in Elasticsearch
        verify(mockMatchRelationSearchRepository, times(0)).save(matchRelation);
    }

    @Test
    @Transactional
    void getAllMatchRelations() throws Exception {
        // Initialize the database
        matchRelationRepository.saveAndFlush(matchRelation);

        // Get all the matchRelationList
        restMatchRelationMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(matchRelation.getId().intValue())))
            .andExpect(jsonPath("$.[*].matchDate").value(hasItem(sameInstant(DEFAULT_MATCH_DATE))));
    }

    @Test
    @Transactional
    void getMatchRelation() throws Exception {
        // Initialize the database
        matchRelationRepository.saveAndFlush(matchRelation);

        // Get the matchRelation
        restMatchRelationMockMvc
            .perform(get(ENTITY_API_URL_ID, matchRelation.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(matchRelation.getId().intValue()))
            .andExpect(jsonPath("$.matchDate").value(sameInstant(DEFAULT_MATCH_DATE)));
    }

    @Test
    @Transactional
    void getNonExistingMatchRelation() throws Exception {
        // Get the matchRelation
        restMatchRelationMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putNewMatchRelation() throws Exception {
        // Initialize the database
        matchRelationRepository.saveAndFlush(matchRelation);

        int databaseSizeBeforeUpdate = matchRelationRepository.findAll().size();

        // Update the matchRelation
        MatchRelation updatedMatchRelation = matchRelationRepository.findById(matchRelation.getId()).get();
        // Disconnect from session so that the updates on updatedMatchRelation are not directly saved in db
        em.detach(updatedMatchRelation);
        updatedMatchRelation.matchDate(UPDATED_MATCH_DATE);

        restMatchRelationMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedMatchRelation.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(updatedMatchRelation))
            )
            .andExpect(status().isOk());

        // Validate the MatchRelation in the database
        List<MatchRelation> matchRelationList = matchRelationRepository.findAll();
        assertThat(matchRelationList).hasSize(databaseSizeBeforeUpdate);
        MatchRelation testMatchRelation = matchRelationList.get(matchRelationList.size() - 1);
        assertThat(testMatchRelation.getMatchDate()).isEqualTo(UPDATED_MATCH_DATE);

        // Validate the MatchRelation in Elasticsearch
        verify(mockMatchRelationSearchRepository).save(testMatchRelation);
    }

    @Test
    @Transactional
    void putNonExistingMatchRelation() throws Exception {
        int databaseSizeBeforeUpdate = matchRelationRepository.findAll().size();
        matchRelation.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restMatchRelationMockMvc
            .perform(
                put(ENTITY_API_URL_ID, matchRelation.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(matchRelation))
            )
            .andExpect(status().isBadRequest());

        // Validate the MatchRelation in the database
        List<MatchRelation> matchRelationList = matchRelationRepository.findAll();
        assertThat(matchRelationList).hasSize(databaseSizeBeforeUpdate);

        // Validate the MatchRelation in Elasticsearch
        verify(mockMatchRelationSearchRepository, times(0)).save(matchRelation);
    }

    @Test
    @Transactional
    void putWithIdMismatchMatchRelation() throws Exception {
        int databaseSizeBeforeUpdate = matchRelationRepository.findAll().size();
        matchRelation.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restMatchRelationMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(matchRelation))
            )
            .andExpect(status().isBadRequest());

        // Validate the MatchRelation in the database
        List<MatchRelation> matchRelationList = matchRelationRepository.findAll();
        assertThat(matchRelationList).hasSize(databaseSizeBeforeUpdate);

        // Validate the MatchRelation in Elasticsearch
        verify(mockMatchRelationSearchRepository, times(0)).save(matchRelation);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamMatchRelation() throws Exception {
        int databaseSizeBeforeUpdate = matchRelationRepository.findAll().size();
        matchRelation.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restMatchRelationMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(matchRelation)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the MatchRelation in the database
        List<MatchRelation> matchRelationList = matchRelationRepository.findAll();
        assertThat(matchRelationList).hasSize(databaseSizeBeforeUpdate);

        // Validate the MatchRelation in Elasticsearch
        verify(mockMatchRelationSearchRepository, times(0)).save(matchRelation);
    }

    @Test
    @Transactional
    void partialUpdateMatchRelationWithPatch() throws Exception {
        // Initialize the database
        matchRelationRepository.saveAndFlush(matchRelation);

        int databaseSizeBeforeUpdate = matchRelationRepository.findAll().size();

        // Update the matchRelation using partial update
        MatchRelation partialUpdatedMatchRelation = new MatchRelation();
        partialUpdatedMatchRelation.setId(matchRelation.getId());

        restMatchRelationMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedMatchRelation.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedMatchRelation))
            )
            .andExpect(status().isOk());

        // Validate the MatchRelation in the database
        List<MatchRelation> matchRelationList = matchRelationRepository.findAll();
        assertThat(matchRelationList).hasSize(databaseSizeBeforeUpdate);
        MatchRelation testMatchRelation = matchRelationList.get(matchRelationList.size() - 1);
        assertThat(testMatchRelation.getMatchDate()).isEqualTo(DEFAULT_MATCH_DATE);
    }

    @Test
    @Transactional
    void fullUpdateMatchRelationWithPatch() throws Exception {
        // Initialize the database
        matchRelationRepository.saveAndFlush(matchRelation);

        int databaseSizeBeforeUpdate = matchRelationRepository.findAll().size();

        // Update the matchRelation using partial update
        MatchRelation partialUpdatedMatchRelation = new MatchRelation();
        partialUpdatedMatchRelation.setId(matchRelation.getId());

        partialUpdatedMatchRelation.matchDate(UPDATED_MATCH_DATE);

        restMatchRelationMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedMatchRelation.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedMatchRelation))
            )
            .andExpect(status().isOk());

        // Validate the MatchRelation in the database
        List<MatchRelation> matchRelationList = matchRelationRepository.findAll();
        assertThat(matchRelationList).hasSize(databaseSizeBeforeUpdate);
        MatchRelation testMatchRelation = matchRelationList.get(matchRelationList.size() - 1);
        assertThat(testMatchRelation.getMatchDate()).isEqualTo(UPDATED_MATCH_DATE);
    }

    @Test
    @Transactional
    void patchNonExistingMatchRelation() throws Exception {
        int databaseSizeBeforeUpdate = matchRelationRepository.findAll().size();
        matchRelation.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restMatchRelationMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, matchRelation.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(matchRelation))
            )
            .andExpect(status().isBadRequest());

        // Validate the MatchRelation in the database
        List<MatchRelation> matchRelationList = matchRelationRepository.findAll();
        assertThat(matchRelationList).hasSize(databaseSizeBeforeUpdate);

        // Validate the MatchRelation in Elasticsearch
        verify(mockMatchRelationSearchRepository, times(0)).save(matchRelation);
    }

    @Test
    @Transactional
    void patchWithIdMismatchMatchRelation() throws Exception {
        int databaseSizeBeforeUpdate = matchRelationRepository.findAll().size();
        matchRelation.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restMatchRelationMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(matchRelation))
            )
            .andExpect(status().isBadRequest());

        // Validate the MatchRelation in the database
        List<MatchRelation> matchRelationList = matchRelationRepository.findAll();
        assertThat(matchRelationList).hasSize(databaseSizeBeforeUpdate);

        // Validate the MatchRelation in Elasticsearch
        verify(mockMatchRelationSearchRepository, times(0)).save(matchRelation);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamMatchRelation() throws Exception {
        int databaseSizeBeforeUpdate = matchRelationRepository.findAll().size();
        matchRelation.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restMatchRelationMockMvc
            .perform(
                patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(TestUtil.convertObjectToJsonBytes(matchRelation))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the MatchRelation in the database
        List<MatchRelation> matchRelationList = matchRelationRepository.findAll();
        assertThat(matchRelationList).hasSize(databaseSizeBeforeUpdate);

        // Validate the MatchRelation in Elasticsearch
        verify(mockMatchRelationSearchRepository, times(0)).save(matchRelation);
    }

    @Test
    @Transactional
    void deleteMatchRelation() throws Exception {
        // Initialize the database
        matchRelationRepository.saveAndFlush(matchRelation);

        int databaseSizeBeforeDelete = matchRelationRepository.findAll().size();

        // Delete the matchRelation
        restMatchRelationMockMvc
            .perform(delete(ENTITY_API_URL_ID, matchRelation.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<MatchRelation> matchRelationList = matchRelationRepository.findAll();
        assertThat(matchRelationList).hasSize(databaseSizeBeforeDelete - 1);

        // Validate the MatchRelation in Elasticsearch
        verify(mockMatchRelationSearchRepository, times(1)).deleteById(matchRelation.getId());
    }

    @Test
    @Transactional
    void searchMatchRelation() throws Exception {
        // Configure the mock search repository
        // Initialize the database
        matchRelationRepository.saveAndFlush(matchRelation);
        when(mockMatchRelationSearchRepository.search(queryStringQuery("id:" + matchRelation.getId()), PageRequest.of(0, 20)))
            .thenReturn(new PageImpl<>(Collections.singletonList(matchRelation), PageRequest.of(0, 1), 1));

        // Search the matchRelation
        restMatchRelationMockMvc
            .perform(get(ENTITY_SEARCH_API_URL + "?query=id:" + matchRelation.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(matchRelation.getId().intValue())))
            .andExpect(jsonPath("$.[*].matchDate").value(hasItem(sameInstant(DEFAULT_MATCH_DATE))));
    }
}
