package com.tmrfcb.datingapp.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.tmrfcb.datingapp.IntegrationTest;
import com.tmrfcb.datingapp.domain.Facebook;
import com.tmrfcb.datingapp.repository.FacebookRepository;
import com.tmrfcb.datingapp.repository.search.FacebookSearchRepository;
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
 * Integration tests for the {@link FacebookResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class FacebookResourceIT {

    private static final String ENTITY_API_URL = "/api/facebooks";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";
    private static final String ENTITY_SEARCH_API_URL = "/api/_search/facebooks";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private FacebookRepository facebookRepository;

    /**
     * This repository is mocked in the com.tmrfcb.datingapp.repository.search test package.
     *
     * @see com.tmrfcb.datingapp.repository.search.FacebookSearchRepositoryMockConfiguration
     */
    @Autowired
    private FacebookSearchRepository mockFacebookSearchRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restFacebookMockMvc;

    private Facebook facebook;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Facebook createEntity(EntityManager em) {
        Facebook facebook = new Facebook();
        return facebook;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Facebook createUpdatedEntity(EntityManager em) {
        Facebook facebook = new Facebook();
        return facebook;
    }

    @BeforeEach
    public void initTest() {
        facebook = createEntity(em);
    }

    @Test
    @Transactional
    void createFacebook() throws Exception {
        int databaseSizeBeforeCreate = facebookRepository.findAll().size();
        // Create the Facebook
        restFacebookMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(facebook)))
            .andExpect(status().isCreated());

        // Validate the Facebook in the database
        List<Facebook> facebookList = facebookRepository.findAll();
        assertThat(facebookList).hasSize(databaseSizeBeforeCreate + 1);
        Facebook testFacebook = facebookList.get(facebookList.size() - 1);

        // Validate the Facebook in Elasticsearch
        verify(mockFacebookSearchRepository, times(1)).save(testFacebook);
    }

    @Test
    @Transactional
    void createFacebookWithExistingId() throws Exception {
        // Create the Facebook with an existing ID
        facebook.setId(1L);

        int databaseSizeBeforeCreate = facebookRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restFacebookMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(facebook)))
            .andExpect(status().isBadRequest());

        // Validate the Facebook in the database
        List<Facebook> facebookList = facebookRepository.findAll();
        assertThat(facebookList).hasSize(databaseSizeBeforeCreate);

        // Validate the Facebook in Elasticsearch
        verify(mockFacebookSearchRepository, times(0)).save(facebook);
    }

    @Test
    @Transactional
    void getAllFacebooks() throws Exception {
        // Initialize the database
        facebookRepository.saveAndFlush(facebook);

        // Get all the facebookList
        restFacebookMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(facebook.getId().intValue())));
    }

    @Test
    @Transactional
    void getFacebook() throws Exception {
        // Initialize the database
        facebookRepository.saveAndFlush(facebook);

        // Get the facebook
        restFacebookMockMvc
            .perform(get(ENTITY_API_URL_ID, facebook.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(facebook.getId().intValue()));
    }

    @Test
    @Transactional
    void getNonExistingFacebook() throws Exception {
        // Get the facebook
        restFacebookMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putNewFacebook() throws Exception {
        // Initialize the database
        facebookRepository.saveAndFlush(facebook);

        int databaseSizeBeforeUpdate = facebookRepository.findAll().size();

        // Update the facebook
        Facebook updatedFacebook = facebookRepository.findById(facebook.getId()).get();
        // Disconnect from session so that the updates on updatedFacebook are not directly saved in db
        em.detach(updatedFacebook);

        restFacebookMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedFacebook.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(updatedFacebook))
            )
            .andExpect(status().isOk());

        // Validate the Facebook in the database
        List<Facebook> facebookList = facebookRepository.findAll();
        assertThat(facebookList).hasSize(databaseSizeBeforeUpdate);
        Facebook testFacebook = facebookList.get(facebookList.size() - 1);

        // Validate the Facebook in Elasticsearch
        verify(mockFacebookSearchRepository).save(testFacebook);
    }

    @Test
    @Transactional
    void putNonExistingFacebook() throws Exception {
        int databaseSizeBeforeUpdate = facebookRepository.findAll().size();
        facebook.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restFacebookMockMvc
            .perform(
                put(ENTITY_API_URL_ID, facebook.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(facebook))
            )
            .andExpect(status().isBadRequest());

        // Validate the Facebook in the database
        List<Facebook> facebookList = facebookRepository.findAll();
        assertThat(facebookList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Facebook in Elasticsearch
        verify(mockFacebookSearchRepository, times(0)).save(facebook);
    }

    @Test
    @Transactional
    void putWithIdMismatchFacebook() throws Exception {
        int databaseSizeBeforeUpdate = facebookRepository.findAll().size();
        facebook.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restFacebookMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(facebook))
            )
            .andExpect(status().isBadRequest());

        // Validate the Facebook in the database
        List<Facebook> facebookList = facebookRepository.findAll();
        assertThat(facebookList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Facebook in Elasticsearch
        verify(mockFacebookSearchRepository, times(0)).save(facebook);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamFacebook() throws Exception {
        int databaseSizeBeforeUpdate = facebookRepository.findAll().size();
        facebook.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restFacebookMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(facebook)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Facebook in the database
        List<Facebook> facebookList = facebookRepository.findAll();
        assertThat(facebookList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Facebook in Elasticsearch
        verify(mockFacebookSearchRepository, times(0)).save(facebook);
    }

    @Test
    @Transactional
    void partialUpdateFacebookWithPatch() throws Exception {
        // Initialize the database
        facebookRepository.saveAndFlush(facebook);

        int databaseSizeBeforeUpdate = facebookRepository.findAll().size();

        // Update the facebook using partial update
        Facebook partialUpdatedFacebook = new Facebook();
        partialUpdatedFacebook.setId(facebook.getId());

        restFacebookMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedFacebook.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedFacebook))
            )
            .andExpect(status().isOk());

        // Validate the Facebook in the database
        List<Facebook> facebookList = facebookRepository.findAll();
        assertThat(facebookList).hasSize(databaseSizeBeforeUpdate);
        Facebook testFacebook = facebookList.get(facebookList.size() - 1);
    }

    @Test
    @Transactional
    void fullUpdateFacebookWithPatch() throws Exception {
        // Initialize the database
        facebookRepository.saveAndFlush(facebook);

        int databaseSizeBeforeUpdate = facebookRepository.findAll().size();

        // Update the facebook using partial update
        Facebook partialUpdatedFacebook = new Facebook();
        partialUpdatedFacebook.setId(facebook.getId());

        restFacebookMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedFacebook.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedFacebook))
            )
            .andExpect(status().isOk());

        // Validate the Facebook in the database
        List<Facebook> facebookList = facebookRepository.findAll();
        assertThat(facebookList).hasSize(databaseSizeBeforeUpdate);
        Facebook testFacebook = facebookList.get(facebookList.size() - 1);
    }

    @Test
    @Transactional
    void patchNonExistingFacebook() throws Exception {
        int databaseSizeBeforeUpdate = facebookRepository.findAll().size();
        facebook.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restFacebookMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, facebook.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(facebook))
            )
            .andExpect(status().isBadRequest());

        // Validate the Facebook in the database
        List<Facebook> facebookList = facebookRepository.findAll();
        assertThat(facebookList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Facebook in Elasticsearch
        verify(mockFacebookSearchRepository, times(0)).save(facebook);
    }

    @Test
    @Transactional
    void patchWithIdMismatchFacebook() throws Exception {
        int databaseSizeBeforeUpdate = facebookRepository.findAll().size();
        facebook.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restFacebookMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(facebook))
            )
            .andExpect(status().isBadRequest());

        // Validate the Facebook in the database
        List<Facebook> facebookList = facebookRepository.findAll();
        assertThat(facebookList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Facebook in Elasticsearch
        verify(mockFacebookSearchRepository, times(0)).save(facebook);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamFacebook() throws Exception {
        int databaseSizeBeforeUpdate = facebookRepository.findAll().size();
        facebook.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restFacebookMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(TestUtil.convertObjectToJsonBytes(facebook)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Facebook in the database
        List<Facebook> facebookList = facebookRepository.findAll();
        assertThat(facebookList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Facebook in Elasticsearch
        verify(mockFacebookSearchRepository, times(0)).save(facebook);
    }

    @Test
    @Transactional
    void deleteFacebook() throws Exception {
        // Initialize the database
        facebookRepository.saveAndFlush(facebook);

        int databaseSizeBeforeDelete = facebookRepository.findAll().size();

        // Delete the facebook
        restFacebookMockMvc
            .perform(delete(ENTITY_API_URL_ID, facebook.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Facebook> facebookList = facebookRepository.findAll();
        assertThat(facebookList).hasSize(databaseSizeBeforeDelete - 1);

        // Validate the Facebook in Elasticsearch
        verify(mockFacebookSearchRepository, times(1)).deleteById(facebook.getId());
    }

    @Test
    @Transactional
    void searchFacebook() throws Exception {
        // Configure the mock search repository
        // Initialize the database
        facebookRepository.saveAndFlush(facebook);
        when(mockFacebookSearchRepository.search(queryStringQuery("id:" + facebook.getId()), PageRequest.of(0, 20)))
            .thenReturn(new PageImpl<>(Collections.singletonList(facebook), PageRequest.of(0, 1), 1));

        // Search the facebook
        restFacebookMockMvc
            .perform(get(ENTITY_SEARCH_API_URL + "?query=id:" + facebook.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(facebook.getId().intValue())));
    }
}
