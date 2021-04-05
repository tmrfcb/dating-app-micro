package com.tmrfcb.datingapp.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.tmrfcb.datingapp.IntegrationTest;
import com.tmrfcb.datingapp.domain.ReportUser;
import com.tmrfcb.datingapp.repository.ReportUserRepository;
import com.tmrfcb.datingapp.repository.search.ReportUserSearchRepository;
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
import org.springframework.util.Base64Utils;

/**
 * Integration tests for the {@link ReportUserResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class ReportUserResourceIT {

    private static final String DEFAULT_REPORT_ID = "AAAAAAAAAA";
    private static final String UPDATED_REPORT_ID = "BBBBBBBBBB";

    private static final String DEFAULT_USER_ID = "AAAAAAAAAA";
    private static final String UPDATED_USER_ID = "BBBBBBBBBB";

    private static final String DEFAULT_CAUSE = "AAAAAAAAAA";
    private static final String UPDATED_CAUSE = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/report-users";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";
    private static final String ENTITY_SEARCH_API_URL = "/api/_search/report-users";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ReportUserRepository reportUserRepository;

    /**
     * This repository is mocked in the com.tmrfcb.datingapp.repository.search test package.
     *
     * @see com.tmrfcb.datingapp.repository.search.ReportUserSearchRepositoryMockConfiguration
     */
    @Autowired
    private ReportUserSearchRepository mockReportUserSearchRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restReportUserMockMvc;

    private ReportUser reportUser;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static ReportUser createEntity(EntityManager em) {
        ReportUser reportUser = new ReportUser().reportId(DEFAULT_REPORT_ID).userId(DEFAULT_USER_ID).cause(DEFAULT_CAUSE);
        return reportUser;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static ReportUser createUpdatedEntity(EntityManager em) {
        ReportUser reportUser = new ReportUser().reportId(UPDATED_REPORT_ID).userId(UPDATED_USER_ID).cause(UPDATED_CAUSE);
        return reportUser;
    }

    @BeforeEach
    public void initTest() {
        reportUser = createEntity(em);
    }

    @Test
    @Transactional
    void createReportUser() throws Exception {
        int databaseSizeBeforeCreate = reportUserRepository.findAll().size();
        // Create the ReportUser
        restReportUserMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(reportUser)))
            .andExpect(status().isCreated());

        // Validate the ReportUser in the database
        List<ReportUser> reportUserList = reportUserRepository.findAll();
        assertThat(reportUserList).hasSize(databaseSizeBeforeCreate + 1);
        ReportUser testReportUser = reportUserList.get(reportUserList.size() - 1);
        assertThat(testReportUser.getReportId()).isEqualTo(DEFAULT_REPORT_ID);
        assertThat(testReportUser.getUserId()).isEqualTo(DEFAULT_USER_ID);
        assertThat(testReportUser.getCause()).isEqualTo(DEFAULT_CAUSE);

        // Validate the ReportUser in Elasticsearch
        verify(mockReportUserSearchRepository, times(1)).save(testReportUser);
    }

    @Test
    @Transactional
    void createReportUserWithExistingId() throws Exception {
        // Create the ReportUser with an existing ID
        reportUser.setId(1L);

        int databaseSizeBeforeCreate = reportUserRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restReportUserMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(reportUser)))
            .andExpect(status().isBadRequest());

        // Validate the ReportUser in the database
        List<ReportUser> reportUserList = reportUserRepository.findAll();
        assertThat(reportUserList).hasSize(databaseSizeBeforeCreate);

        // Validate the ReportUser in Elasticsearch
        verify(mockReportUserSearchRepository, times(0)).save(reportUser);
    }

    @Test
    @Transactional
    void getAllReportUsers() throws Exception {
        // Initialize the database
        reportUserRepository.saveAndFlush(reportUser);

        // Get all the reportUserList
        restReportUserMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(reportUser.getId().intValue())))
            .andExpect(jsonPath("$.[*].reportId").value(hasItem(DEFAULT_REPORT_ID)))
            .andExpect(jsonPath("$.[*].userId").value(hasItem(DEFAULT_USER_ID)))
            .andExpect(jsonPath("$.[*].cause").value(hasItem(DEFAULT_CAUSE.toString())));
    }

    @Test
    @Transactional
    void getReportUser() throws Exception {
        // Initialize the database
        reportUserRepository.saveAndFlush(reportUser);

        // Get the reportUser
        restReportUserMockMvc
            .perform(get(ENTITY_API_URL_ID, reportUser.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(reportUser.getId().intValue()))
            .andExpect(jsonPath("$.reportId").value(DEFAULT_REPORT_ID))
            .andExpect(jsonPath("$.userId").value(DEFAULT_USER_ID))
            .andExpect(jsonPath("$.cause").value(DEFAULT_CAUSE.toString()));
    }

    @Test
    @Transactional
    void getNonExistingReportUser() throws Exception {
        // Get the reportUser
        restReportUserMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putNewReportUser() throws Exception {
        // Initialize the database
        reportUserRepository.saveAndFlush(reportUser);

        int databaseSizeBeforeUpdate = reportUserRepository.findAll().size();

        // Update the reportUser
        ReportUser updatedReportUser = reportUserRepository.findById(reportUser.getId()).get();
        // Disconnect from session so that the updates on updatedReportUser are not directly saved in db
        em.detach(updatedReportUser);
        updatedReportUser.reportId(UPDATED_REPORT_ID).userId(UPDATED_USER_ID).cause(UPDATED_CAUSE);

        restReportUserMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedReportUser.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(updatedReportUser))
            )
            .andExpect(status().isOk());

        // Validate the ReportUser in the database
        List<ReportUser> reportUserList = reportUserRepository.findAll();
        assertThat(reportUserList).hasSize(databaseSizeBeforeUpdate);
        ReportUser testReportUser = reportUserList.get(reportUserList.size() - 1);
        assertThat(testReportUser.getReportId()).isEqualTo(UPDATED_REPORT_ID);
        assertThat(testReportUser.getUserId()).isEqualTo(UPDATED_USER_ID);
        assertThat(testReportUser.getCause()).isEqualTo(UPDATED_CAUSE);

        // Validate the ReportUser in Elasticsearch
        verify(mockReportUserSearchRepository).save(testReportUser);
    }

    @Test
    @Transactional
    void putNonExistingReportUser() throws Exception {
        int databaseSizeBeforeUpdate = reportUserRepository.findAll().size();
        reportUser.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restReportUserMockMvc
            .perform(
                put(ENTITY_API_URL_ID, reportUser.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(reportUser))
            )
            .andExpect(status().isBadRequest());

        // Validate the ReportUser in the database
        List<ReportUser> reportUserList = reportUserRepository.findAll();
        assertThat(reportUserList).hasSize(databaseSizeBeforeUpdate);

        // Validate the ReportUser in Elasticsearch
        verify(mockReportUserSearchRepository, times(0)).save(reportUser);
    }

    @Test
    @Transactional
    void putWithIdMismatchReportUser() throws Exception {
        int databaseSizeBeforeUpdate = reportUserRepository.findAll().size();
        reportUser.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restReportUserMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(reportUser))
            )
            .andExpect(status().isBadRequest());

        // Validate the ReportUser in the database
        List<ReportUser> reportUserList = reportUserRepository.findAll();
        assertThat(reportUserList).hasSize(databaseSizeBeforeUpdate);

        // Validate the ReportUser in Elasticsearch
        verify(mockReportUserSearchRepository, times(0)).save(reportUser);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamReportUser() throws Exception {
        int databaseSizeBeforeUpdate = reportUserRepository.findAll().size();
        reportUser.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restReportUserMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(reportUser)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the ReportUser in the database
        List<ReportUser> reportUserList = reportUserRepository.findAll();
        assertThat(reportUserList).hasSize(databaseSizeBeforeUpdate);

        // Validate the ReportUser in Elasticsearch
        verify(mockReportUserSearchRepository, times(0)).save(reportUser);
    }

    @Test
    @Transactional
    void partialUpdateReportUserWithPatch() throws Exception {
        // Initialize the database
        reportUserRepository.saveAndFlush(reportUser);

        int databaseSizeBeforeUpdate = reportUserRepository.findAll().size();

        // Update the reportUser using partial update
        ReportUser partialUpdatedReportUser = new ReportUser();
        partialUpdatedReportUser.setId(reportUser.getId());

        partialUpdatedReportUser.reportId(UPDATED_REPORT_ID).userId(UPDATED_USER_ID);

        restReportUserMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedReportUser.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedReportUser))
            )
            .andExpect(status().isOk());

        // Validate the ReportUser in the database
        List<ReportUser> reportUserList = reportUserRepository.findAll();
        assertThat(reportUserList).hasSize(databaseSizeBeforeUpdate);
        ReportUser testReportUser = reportUserList.get(reportUserList.size() - 1);
        assertThat(testReportUser.getReportId()).isEqualTo(UPDATED_REPORT_ID);
        assertThat(testReportUser.getUserId()).isEqualTo(UPDATED_USER_ID);
        assertThat(testReportUser.getCause()).isEqualTo(DEFAULT_CAUSE);
    }

    @Test
    @Transactional
    void fullUpdateReportUserWithPatch() throws Exception {
        // Initialize the database
        reportUserRepository.saveAndFlush(reportUser);

        int databaseSizeBeforeUpdate = reportUserRepository.findAll().size();

        // Update the reportUser using partial update
        ReportUser partialUpdatedReportUser = new ReportUser();
        partialUpdatedReportUser.setId(reportUser.getId());

        partialUpdatedReportUser.reportId(UPDATED_REPORT_ID).userId(UPDATED_USER_ID).cause(UPDATED_CAUSE);

        restReportUserMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedReportUser.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedReportUser))
            )
            .andExpect(status().isOk());

        // Validate the ReportUser in the database
        List<ReportUser> reportUserList = reportUserRepository.findAll();
        assertThat(reportUserList).hasSize(databaseSizeBeforeUpdate);
        ReportUser testReportUser = reportUserList.get(reportUserList.size() - 1);
        assertThat(testReportUser.getReportId()).isEqualTo(UPDATED_REPORT_ID);
        assertThat(testReportUser.getUserId()).isEqualTo(UPDATED_USER_ID);
        assertThat(testReportUser.getCause()).isEqualTo(UPDATED_CAUSE);
    }

    @Test
    @Transactional
    void patchNonExistingReportUser() throws Exception {
        int databaseSizeBeforeUpdate = reportUserRepository.findAll().size();
        reportUser.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restReportUserMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, reportUser.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(reportUser))
            )
            .andExpect(status().isBadRequest());

        // Validate the ReportUser in the database
        List<ReportUser> reportUserList = reportUserRepository.findAll();
        assertThat(reportUserList).hasSize(databaseSizeBeforeUpdate);

        // Validate the ReportUser in Elasticsearch
        verify(mockReportUserSearchRepository, times(0)).save(reportUser);
    }

    @Test
    @Transactional
    void patchWithIdMismatchReportUser() throws Exception {
        int databaseSizeBeforeUpdate = reportUserRepository.findAll().size();
        reportUser.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restReportUserMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(reportUser))
            )
            .andExpect(status().isBadRequest());

        // Validate the ReportUser in the database
        List<ReportUser> reportUserList = reportUserRepository.findAll();
        assertThat(reportUserList).hasSize(databaseSizeBeforeUpdate);

        // Validate the ReportUser in Elasticsearch
        verify(mockReportUserSearchRepository, times(0)).save(reportUser);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamReportUser() throws Exception {
        int databaseSizeBeforeUpdate = reportUserRepository.findAll().size();
        reportUser.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restReportUserMockMvc
            .perform(
                patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(TestUtil.convertObjectToJsonBytes(reportUser))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the ReportUser in the database
        List<ReportUser> reportUserList = reportUserRepository.findAll();
        assertThat(reportUserList).hasSize(databaseSizeBeforeUpdate);

        // Validate the ReportUser in Elasticsearch
        verify(mockReportUserSearchRepository, times(0)).save(reportUser);
    }

    @Test
    @Transactional
    void deleteReportUser() throws Exception {
        // Initialize the database
        reportUserRepository.saveAndFlush(reportUser);

        int databaseSizeBeforeDelete = reportUserRepository.findAll().size();

        // Delete the reportUser
        restReportUserMockMvc
            .perform(delete(ENTITY_API_URL_ID, reportUser.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<ReportUser> reportUserList = reportUserRepository.findAll();
        assertThat(reportUserList).hasSize(databaseSizeBeforeDelete - 1);

        // Validate the ReportUser in Elasticsearch
        verify(mockReportUserSearchRepository, times(1)).deleteById(reportUser.getId());
    }

    @Test
    @Transactional
    void searchReportUser() throws Exception {
        // Configure the mock search repository
        // Initialize the database
        reportUserRepository.saveAndFlush(reportUser);
        when(mockReportUserSearchRepository.search(queryStringQuery("id:" + reportUser.getId()), PageRequest.of(0, 20)))
            .thenReturn(new PageImpl<>(Collections.singletonList(reportUser), PageRequest.of(0, 1), 1));

        // Search the reportUser
        restReportUserMockMvc
            .perform(get(ENTITY_SEARCH_API_URL + "?query=id:" + reportUser.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(reportUser.getId().intValue())))
            .andExpect(jsonPath("$.[*].reportId").value(hasItem(DEFAULT_REPORT_ID)))
            .andExpect(jsonPath("$.[*].userId").value(hasItem(DEFAULT_USER_ID)))
            .andExpect(jsonPath("$.[*].cause").value(hasItem(DEFAULT_CAUSE.toString())));
    }
}
