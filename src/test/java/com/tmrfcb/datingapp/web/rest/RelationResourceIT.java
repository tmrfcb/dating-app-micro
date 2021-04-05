package com.tmrfcb.datingapp.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.tmrfcb.datingapp.IntegrationTest;
import com.tmrfcb.datingapp.domain.Relation;
import com.tmrfcb.datingapp.domain.enumeration.RelationType;
import com.tmrfcb.datingapp.repository.RelationRepository;
import com.tmrfcb.datingapp.repository.search.RelationSearchRepository;
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
 * Integration tests for the {@link RelationResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class RelationResourceIT {

    private static final String DEFAULT_USER_ID_OF_OTHER = "AAAAAAAAAA";
    private static final String UPDATED_USER_ID_OF_OTHER = "BBBBBBBBBB";

    private static final RelationType DEFAULT_RELATION_TYPE = RelationType.LIKE;
    private static final RelationType UPDATED_RELATION_TYPE = RelationType.DISLIKE;

    private static final String ENTITY_API_URL = "/api/relations";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";
    private static final String ENTITY_SEARCH_API_URL = "/api/_search/relations";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private RelationRepository relationRepository;

    /**
     * This repository is mocked in the com.tmrfcb.datingapp.repository.search test package.
     *
     * @see com.tmrfcb.datingapp.repository.search.RelationSearchRepositoryMockConfiguration
     */
    @Autowired
    private RelationSearchRepository mockRelationSearchRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restRelationMockMvc;

    private Relation relation;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Relation createEntity(EntityManager em) {
        Relation relation = new Relation().userIdOfOther(DEFAULT_USER_ID_OF_OTHER).relationType(DEFAULT_RELATION_TYPE);
        return relation;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Relation createUpdatedEntity(EntityManager em) {
        Relation relation = new Relation().userIdOfOther(UPDATED_USER_ID_OF_OTHER).relationType(UPDATED_RELATION_TYPE);
        return relation;
    }

    @BeforeEach
    public void initTest() {
        relation = createEntity(em);
    }

    @Test
    @Transactional
    void createRelation() throws Exception {
        int databaseSizeBeforeCreate = relationRepository.findAll().size();
        // Create the Relation
        restRelationMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(relation)))
            .andExpect(status().isCreated());

        // Validate the Relation in the database
        List<Relation> relationList = relationRepository.findAll();
        assertThat(relationList).hasSize(databaseSizeBeforeCreate + 1);
        Relation testRelation = relationList.get(relationList.size() - 1);
        assertThat(testRelation.getUserIdOfOther()).isEqualTo(DEFAULT_USER_ID_OF_OTHER);
        assertThat(testRelation.getRelationType()).isEqualTo(DEFAULT_RELATION_TYPE);

        // Validate the Relation in Elasticsearch
        verify(mockRelationSearchRepository, times(1)).save(testRelation);
    }

    @Test
    @Transactional
    void createRelationWithExistingId() throws Exception {
        // Create the Relation with an existing ID
        relation.setId(1L);

        int databaseSizeBeforeCreate = relationRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restRelationMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(relation)))
            .andExpect(status().isBadRequest());

        // Validate the Relation in the database
        List<Relation> relationList = relationRepository.findAll();
        assertThat(relationList).hasSize(databaseSizeBeforeCreate);

        // Validate the Relation in Elasticsearch
        verify(mockRelationSearchRepository, times(0)).save(relation);
    }

    @Test
    @Transactional
    void getAllRelations() throws Exception {
        // Initialize the database
        relationRepository.saveAndFlush(relation);

        // Get all the relationList
        restRelationMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(relation.getId().intValue())))
            .andExpect(jsonPath("$.[*].userIdOfOther").value(hasItem(DEFAULT_USER_ID_OF_OTHER)))
            .andExpect(jsonPath("$.[*].relationType").value(hasItem(DEFAULT_RELATION_TYPE.toString())));
    }

    @Test
    @Transactional
    void getRelation() throws Exception {
        // Initialize the database
        relationRepository.saveAndFlush(relation);

        // Get the relation
        restRelationMockMvc
            .perform(get(ENTITY_API_URL_ID, relation.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(relation.getId().intValue()))
            .andExpect(jsonPath("$.userIdOfOther").value(DEFAULT_USER_ID_OF_OTHER))
            .andExpect(jsonPath("$.relationType").value(DEFAULT_RELATION_TYPE.toString()));
    }

    @Test
    @Transactional
    void getNonExistingRelation() throws Exception {
        // Get the relation
        restRelationMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putNewRelation() throws Exception {
        // Initialize the database
        relationRepository.saveAndFlush(relation);

        int databaseSizeBeforeUpdate = relationRepository.findAll().size();

        // Update the relation
        Relation updatedRelation = relationRepository.findById(relation.getId()).get();
        // Disconnect from session so that the updates on updatedRelation are not directly saved in db
        em.detach(updatedRelation);
        updatedRelation.userIdOfOther(UPDATED_USER_ID_OF_OTHER).relationType(UPDATED_RELATION_TYPE);

        restRelationMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedRelation.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(updatedRelation))
            )
            .andExpect(status().isOk());

        // Validate the Relation in the database
        List<Relation> relationList = relationRepository.findAll();
        assertThat(relationList).hasSize(databaseSizeBeforeUpdate);
        Relation testRelation = relationList.get(relationList.size() - 1);
        assertThat(testRelation.getUserIdOfOther()).isEqualTo(UPDATED_USER_ID_OF_OTHER);
        assertThat(testRelation.getRelationType()).isEqualTo(UPDATED_RELATION_TYPE);

        // Validate the Relation in Elasticsearch
        verify(mockRelationSearchRepository).save(testRelation);
    }

    @Test
    @Transactional
    void putNonExistingRelation() throws Exception {
        int databaseSizeBeforeUpdate = relationRepository.findAll().size();
        relation.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restRelationMockMvc
            .perform(
                put(ENTITY_API_URL_ID, relation.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(relation))
            )
            .andExpect(status().isBadRequest());

        // Validate the Relation in the database
        List<Relation> relationList = relationRepository.findAll();
        assertThat(relationList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Relation in Elasticsearch
        verify(mockRelationSearchRepository, times(0)).save(relation);
    }

    @Test
    @Transactional
    void putWithIdMismatchRelation() throws Exception {
        int databaseSizeBeforeUpdate = relationRepository.findAll().size();
        relation.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restRelationMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(relation))
            )
            .andExpect(status().isBadRequest());

        // Validate the Relation in the database
        List<Relation> relationList = relationRepository.findAll();
        assertThat(relationList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Relation in Elasticsearch
        verify(mockRelationSearchRepository, times(0)).save(relation);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamRelation() throws Exception {
        int databaseSizeBeforeUpdate = relationRepository.findAll().size();
        relation.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restRelationMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(relation)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Relation in the database
        List<Relation> relationList = relationRepository.findAll();
        assertThat(relationList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Relation in Elasticsearch
        verify(mockRelationSearchRepository, times(0)).save(relation);
    }

    @Test
    @Transactional
    void partialUpdateRelationWithPatch() throws Exception {
        // Initialize the database
        relationRepository.saveAndFlush(relation);

        int databaseSizeBeforeUpdate = relationRepository.findAll().size();

        // Update the relation using partial update
        Relation partialUpdatedRelation = new Relation();
        partialUpdatedRelation.setId(relation.getId());

        partialUpdatedRelation.userIdOfOther(UPDATED_USER_ID_OF_OTHER);

        restRelationMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedRelation.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedRelation))
            )
            .andExpect(status().isOk());

        // Validate the Relation in the database
        List<Relation> relationList = relationRepository.findAll();
        assertThat(relationList).hasSize(databaseSizeBeforeUpdate);
        Relation testRelation = relationList.get(relationList.size() - 1);
        assertThat(testRelation.getUserIdOfOther()).isEqualTo(UPDATED_USER_ID_OF_OTHER);
        assertThat(testRelation.getRelationType()).isEqualTo(DEFAULT_RELATION_TYPE);
    }

    @Test
    @Transactional
    void fullUpdateRelationWithPatch() throws Exception {
        // Initialize the database
        relationRepository.saveAndFlush(relation);

        int databaseSizeBeforeUpdate = relationRepository.findAll().size();

        // Update the relation using partial update
        Relation partialUpdatedRelation = new Relation();
        partialUpdatedRelation.setId(relation.getId());

        partialUpdatedRelation.userIdOfOther(UPDATED_USER_ID_OF_OTHER).relationType(UPDATED_RELATION_TYPE);

        restRelationMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedRelation.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedRelation))
            )
            .andExpect(status().isOk());

        // Validate the Relation in the database
        List<Relation> relationList = relationRepository.findAll();
        assertThat(relationList).hasSize(databaseSizeBeforeUpdate);
        Relation testRelation = relationList.get(relationList.size() - 1);
        assertThat(testRelation.getUserIdOfOther()).isEqualTo(UPDATED_USER_ID_OF_OTHER);
        assertThat(testRelation.getRelationType()).isEqualTo(UPDATED_RELATION_TYPE);
    }

    @Test
    @Transactional
    void patchNonExistingRelation() throws Exception {
        int databaseSizeBeforeUpdate = relationRepository.findAll().size();
        relation.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restRelationMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, relation.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(relation))
            )
            .andExpect(status().isBadRequest());

        // Validate the Relation in the database
        List<Relation> relationList = relationRepository.findAll();
        assertThat(relationList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Relation in Elasticsearch
        verify(mockRelationSearchRepository, times(0)).save(relation);
    }

    @Test
    @Transactional
    void patchWithIdMismatchRelation() throws Exception {
        int databaseSizeBeforeUpdate = relationRepository.findAll().size();
        relation.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restRelationMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(relation))
            )
            .andExpect(status().isBadRequest());

        // Validate the Relation in the database
        List<Relation> relationList = relationRepository.findAll();
        assertThat(relationList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Relation in Elasticsearch
        verify(mockRelationSearchRepository, times(0)).save(relation);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamRelation() throws Exception {
        int databaseSizeBeforeUpdate = relationRepository.findAll().size();
        relation.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restRelationMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(TestUtil.convertObjectToJsonBytes(relation)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Relation in the database
        List<Relation> relationList = relationRepository.findAll();
        assertThat(relationList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Relation in Elasticsearch
        verify(mockRelationSearchRepository, times(0)).save(relation);
    }

    @Test
    @Transactional
    void deleteRelation() throws Exception {
        // Initialize the database
        relationRepository.saveAndFlush(relation);

        int databaseSizeBeforeDelete = relationRepository.findAll().size();

        // Delete the relation
        restRelationMockMvc
            .perform(delete(ENTITY_API_URL_ID, relation.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Relation> relationList = relationRepository.findAll();
        assertThat(relationList).hasSize(databaseSizeBeforeDelete - 1);

        // Validate the Relation in Elasticsearch
        verify(mockRelationSearchRepository, times(1)).deleteById(relation.getId());
    }

    @Test
    @Transactional
    void searchRelation() throws Exception {
        // Configure the mock search repository
        // Initialize the database
        relationRepository.saveAndFlush(relation);
        when(mockRelationSearchRepository.search(queryStringQuery("id:" + relation.getId()), PageRequest.of(0, 20)))
            .thenReturn(new PageImpl<>(Collections.singletonList(relation), PageRequest.of(0, 1), 1));

        // Search the relation
        restRelationMockMvc
            .perform(get(ENTITY_SEARCH_API_URL + "?query=id:" + relation.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(relation.getId().intValue())))
            .andExpect(jsonPath("$.[*].userIdOfOther").value(hasItem(DEFAULT_USER_ID_OF_OTHER)))
            .andExpect(jsonPath("$.[*].relationType").value(hasItem(DEFAULT_RELATION_TYPE.toString())));
    }
}
