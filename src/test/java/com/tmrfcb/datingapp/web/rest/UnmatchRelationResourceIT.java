package com.tmrfcb.datingapp.web.rest;

import static com.tmrfcb.datingapp.web.rest.TestUtil.sameInstant;
import static org.assertj.core.api.Assertions.assertThat;
import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.tmrfcb.datingapp.IntegrationTest;
import com.tmrfcb.datingapp.domain.UnmatchRelation;
import com.tmrfcb.datingapp.repository.UnmatchRelationRepository;
import com.tmrfcb.datingapp.repository.search.UnmatchRelationSearchRepository;
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
 * Integration tests for the {@link UnmatchRelationResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class UnmatchRelationResourceIT {

    private static final ZonedDateTime DEFAULT_UN_MATCH_DATE = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneOffset.UTC);
    private static final ZonedDateTime UPDATED_UN_MATCH_DATE = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);

    private static final String ENTITY_API_URL = "/api/unmatch-relations";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";
    private static final String ENTITY_SEARCH_API_URL = "/api/_search/unmatch-relations";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private UnmatchRelationRepository unmatchRelationRepository;

    /**
     * This repository is mocked in the com.tmrfcb.datingapp.repository.search test package.
     *
     * @see com.tmrfcb.datingapp.repository.search.UnmatchRelationSearchRepositoryMockConfiguration
     */
    @Autowired
    private UnmatchRelationSearchRepository mockUnmatchRelationSearchRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restUnmatchRelationMockMvc;

    private UnmatchRelation unmatchRelation;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static UnmatchRelation createEntity(EntityManager em) {
        UnmatchRelation unmatchRelation = new UnmatchRelation().unMatchDate(DEFAULT_UN_MATCH_DATE);
        return unmatchRelation;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static UnmatchRelation createUpdatedEntity(EntityManager em) {
        UnmatchRelation unmatchRelation = new UnmatchRelation().unMatchDate(UPDATED_UN_MATCH_DATE);
        return unmatchRelation;
    }

    @BeforeEach
    public void initTest() {
        unmatchRelation = createEntity(em);
    }

    @Test
    @Transactional
    void createUnmatchRelation() throws Exception {
        int databaseSizeBeforeCreate = unmatchRelationRepository.findAll().size();
        // Create the UnmatchRelation
        restUnmatchRelationMockMvc
            .perform(
                post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(unmatchRelation))
            )
            .andExpect(status().isCreated());

        // Validate the UnmatchRelation in the database
        List<UnmatchRelation> unmatchRelationList = unmatchRelationRepository.findAll();
        assertThat(unmatchRelationList).hasSize(databaseSizeBeforeCreate + 1);
        UnmatchRelation testUnmatchRelation = unmatchRelationList.get(unmatchRelationList.size() - 1);
        assertThat(testUnmatchRelation.getUnMatchDate()).isEqualTo(DEFAULT_UN_MATCH_DATE);

        // Validate the UnmatchRelation in Elasticsearch
        verify(mockUnmatchRelationSearchRepository, times(1)).save(testUnmatchRelation);
    }

    @Test
    @Transactional
    void createUnmatchRelationWithExistingId() throws Exception {
        // Create the UnmatchRelation with an existing ID
        unmatchRelation.setId(1L);

        int databaseSizeBeforeCreate = unmatchRelationRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restUnmatchRelationMockMvc
            .perform(
                post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(unmatchRelation))
            )
            .andExpect(status().isBadRequest());

        // Validate the UnmatchRelation in the database
        List<UnmatchRelation> unmatchRelationList = unmatchRelationRepository.findAll();
        assertThat(unmatchRelationList).hasSize(databaseSizeBeforeCreate);

        // Validate the UnmatchRelation in Elasticsearch
        verify(mockUnmatchRelationSearchRepository, times(0)).save(unmatchRelation);
    }

    @Test
    @Transactional
    void getAllUnmatchRelations() throws Exception {
        // Initialize the database
        unmatchRelationRepository.saveAndFlush(unmatchRelation);

        // Get all the unmatchRelationList
        restUnmatchRelationMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(unmatchRelation.getId().intValue())))
            .andExpect(jsonPath("$.[*].unMatchDate").value(hasItem(sameInstant(DEFAULT_UN_MATCH_DATE))));
    }

    @Test
    @Transactional
    void getUnmatchRelation() throws Exception {
        // Initialize the database
        unmatchRelationRepository.saveAndFlush(unmatchRelation);

        // Get the unmatchRelation
        restUnmatchRelationMockMvc
            .perform(get(ENTITY_API_URL_ID, unmatchRelation.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(unmatchRelation.getId().intValue()))
            .andExpect(jsonPath("$.unMatchDate").value(sameInstant(DEFAULT_UN_MATCH_DATE)));
    }

    @Test
    @Transactional
    void getNonExistingUnmatchRelation() throws Exception {
        // Get the unmatchRelation
        restUnmatchRelationMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putNewUnmatchRelation() throws Exception {
        // Initialize the database
        unmatchRelationRepository.saveAndFlush(unmatchRelation);

        int databaseSizeBeforeUpdate = unmatchRelationRepository.findAll().size();

        // Update the unmatchRelation
        UnmatchRelation updatedUnmatchRelation = unmatchRelationRepository.findById(unmatchRelation.getId()).get();
        // Disconnect from session so that the updates on updatedUnmatchRelation are not directly saved in db
        em.detach(updatedUnmatchRelation);
        updatedUnmatchRelation.unMatchDate(UPDATED_UN_MATCH_DATE);

        restUnmatchRelationMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedUnmatchRelation.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(updatedUnmatchRelation))
            )
            .andExpect(status().isOk());

        // Validate the UnmatchRelation in the database
        List<UnmatchRelation> unmatchRelationList = unmatchRelationRepository.findAll();
        assertThat(unmatchRelationList).hasSize(databaseSizeBeforeUpdate);
        UnmatchRelation testUnmatchRelation = unmatchRelationList.get(unmatchRelationList.size() - 1);
        assertThat(testUnmatchRelation.getUnMatchDate()).isEqualTo(UPDATED_UN_MATCH_DATE);

        // Validate the UnmatchRelation in Elasticsearch
        verify(mockUnmatchRelationSearchRepository).save(testUnmatchRelation);
    }

    @Test
    @Transactional
    void putNonExistingUnmatchRelation() throws Exception {
        int databaseSizeBeforeUpdate = unmatchRelationRepository.findAll().size();
        unmatchRelation.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restUnmatchRelationMockMvc
            .perform(
                put(ENTITY_API_URL_ID, unmatchRelation.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(unmatchRelation))
            )
            .andExpect(status().isBadRequest());

        // Validate the UnmatchRelation in the database
        List<UnmatchRelation> unmatchRelationList = unmatchRelationRepository.findAll();
        assertThat(unmatchRelationList).hasSize(databaseSizeBeforeUpdate);

        // Validate the UnmatchRelation in Elasticsearch
        verify(mockUnmatchRelationSearchRepository, times(0)).save(unmatchRelation);
    }

    @Test
    @Transactional
    void putWithIdMismatchUnmatchRelation() throws Exception {
        int databaseSizeBeforeUpdate = unmatchRelationRepository.findAll().size();
        unmatchRelation.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restUnmatchRelationMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(unmatchRelation))
            )
            .andExpect(status().isBadRequest());

        // Validate the UnmatchRelation in the database
        List<UnmatchRelation> unmatchRelationList = unmatchRelationRepository.findAll();
        assertThat(unmatchRelationList).hasSize(databaseSizeBeforeUpdate);

        // Validate the UnmatchRelation in Elasticsearch
        verify(mockUnmatchRelationSearchRepository, times(0)).save(unmatchRelation);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamUnmatchRelation() throws Exception {
        int databaseSizeBeforeUpdate = unmatchRelationRepository.findAll().size();
        unmatchRelation.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restUnmatchRelationMockMvc
            .perform(
                put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(unmatchRelation))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the UnmatchRelation in the database
        List<UnmatchRelation> unmatchRelationList = unmatchRelationRepository.findAll();
        assertThat(unmatchRelationList).hasSize(databaseSizeBeforeUpdate);

        // Validate the UnmatchRelation in Elasticsearch
        verify(mockUnmatchRelationSearchRepository, times(0)).save(unmatchRelation);
    }

    @Test
    @Transactional
    void partialUpdateUnmatchRelationWithPatch() throws Exception {
        // Initialize the database
        unmatchRelationRepository.saveAndFlush(unmatchRelation);

        int databaseSizeBeforeUpdate = unmatchRelationRepository.findAll().size();

        // Update the unmatchRelation using partial update
        UnmatchRelation partialUpdatedUnmatchRelation = new UnmatchRelation();
        partialUpdatedUnmatchRelation.setId(unmatchRelation.getId());

        restUnmatchRelationMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedUnmatchRelation.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedUnmatchRelation))
            )
            .andExpect(status().isOk());

        // Validate the UnmatchRelation in the database
        List<UnmatchRelation> unmatchRelationList = unmatchRelationRepository.findAll();
        assertThat(unmatchRelationList).hasSize(databaseSizeBeforeUpdate);
        UnmatchRelation testUnmatchRelation = unmatchRelationList.get(unmatchRelationList.size() - 1);
        assertThat(testUnmatchRelation.getUnMatchDate()).isEqualTo(DEFAULT_UN_MATCH_DATE);
    }

    @Test
    @Transactional
    void fullUpdateUnmatchRelationWithPatch() throws Exception {
        // Initialize the database
        unmatchRelationRepository.saveAndFlush(unmatchRelation);

        int databaseSizeBeforeUpdate = unmatchRelationRepository.findAll().size();

        // Update the unmatchRelation using partial update
        UnmatchRelation partialUpdatedUnmatchRelation = new UnmatchRelation();
        partialUpdatedUnmatchRelation.setId(unmatchRelation.getId());

        partialUpdatedUnmatchRelation.unMatchDate(UPDATED_UN_MATCH_DATE);

        restUnmatchRelationMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedUnmatchRelation.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedUnmatchRelation))
            )
            .andExpect(status().isOk());

        // Validate the UnmatchRelation in the database
        List<UnmatchRelation> unmatchRelationList = unmatchRelationRepository.findAll();
        assertThat(unmatchRelationList).hasSize(databaseSizeBeforeUpdate);
        UnmatchRelation testUnmatchRelation = unmatchRelationList.get(unmatchRelationList.size() - 1);
        assertThat(testUnmatchRelation.getUnMatchDate()).isEqualTo(UPDATED_UN_MATCH_DATE);
    }

    @Test
    @Transactional
    void patchNonExistingUnmatchRelation() throws Exception {
        int databaseSizeBeforeUpdate = unmatchRelationRepository.findAll().size();
        unmatchRelation.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restUnmatchRelationMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, unmatchRelation.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(unmatchRelation))
            )
            .andExpect(status().isBadRequest());

        // Validate the UnmatchRelation in the database
        List<UnmatchRelation> unmatchRelationList = unmatchRelationRepository.findAll();
        assertThat(unmatchRelationList).hasSize(databaseSizeBeforeUpdate);

        // Validate the UnmatchRelation in Elasticsearch
        verify(mockUnmatchRelationSearchRepository, times(0)).save(unmatchRelation);
    }

    @Test
    @Transactional
    void patchWithIdMismatchUnmatchRelation() throws Exception {
        int databaseSizeBeforeUpdate = unmatchRelationRepository.findAll().size();
        unmatchRelation.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restUnmatchRelationMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(unmatchRelation))
            )
            .andExpect(status().isBadRequest());

        // Validate the UnmatchRelation in the database
        List<UnmatchRelation> unmatchRelationList = unmatchRelationRepository.findAll();
        assertThat(unmatchRelationList).hasSize(databaseSizeBeforeUpdate);

        // Validate the UnmatchRelation in Elasticsearch
        verify(mockUnmatchRelationSearchRepository, times(0)).save(unmatchRelation);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamUnmatchRelation() throws Exception {
        int databaseSizeBeforeUpdate = unmatchRelationRepository.findAll().size();
        unmatchRelation.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restUnmatchRelationMockMvc
            .perform(
                patch(ENTITY_API_URL)
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(unmatchRelation))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the UnmatchRelation in the database
        List<UnmatchRelation> unmatchRelationList = unmatchRelationRepository.findAll();
        assertThat(unmatchRelationList).hasSize(databaseSizeBeforeUpdate);

        // Validate the UnmatchRelation in Elasticsearch
        verify(mockUnmatchRelationSearchRepository, times(0)).save(unmatchRelation);
    }

    @Test
    @Transactional
    void deleteUnmatchRelation() throws Exception {
        // Initialize the database
        unmatchRelationRepository.saveAndFlush(unmatchRelation);

        int databaseSizeBeforeDelete = unmatchRelationRepository.findAll().size();

        // Delete the unmatchRelation
        restUnmatchRelationMockMvc
            .perform(delete(ENTITY_API_URL_ID, unmatchRelation.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<UnmatchRelation> unmatchRelationList = unmatchRelationRepository.findAll();
        assertThat(unmatchRelationList).hasSize(databaseSizeBeforeDelete - 1);

        // Validate the UnmatchRelation in Elasticsearch
        verify(mockUnmatchRelationSearchRepository, times(1)).deleteById(unmatchRelation.getId());
    }

    @Test
    @Transactional
    void searchUnmatchRelation() throws Exception {
        // Configure the mock search repository
        // Initialize the database
        unmatchRelationRepository.saveAndFlush(unmatchRelation);
        when(mockUnmatchRelationSearchRepository.search(queryStringQuery("id:" + unmatchRelation.getId()), PageRequest.of(0, 20)))
            .thenReturn(new PageImpl<>(Collections.singletonList(unmatchRelation), PageRequest.of(0, 1), 1));

        // Search the unmatchRelation
        restUnmatchRelationMockMvc
            .perform(get(ENTITY_SEARCH_API_URL + "?query=id:" + unmatchRelation.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(unmatchRelation.getId().intValue())))
            .andExpect(jsonPath("$.[*].unMatchDate").value(hasItem(sameInstant(DEFAULT_UN_MATCH_DATE))));
    }
}
