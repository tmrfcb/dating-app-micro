package com.tmrfcb.datingapp.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.tmrfcb.datingapp.IntegrationTest;
import com.tmrfcb.datingapp.domain.UserApp;
import com.tmrfcb.datingapp.repository.UserAppRepository;
import com.tmrfcb.datingapp.repository.search.UserAppSearchRepository;
import java.time.LocalDate;
import java.time.ZoneId;
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
 * Integration tests for the {@link UserAppResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class UserAppResourceIT {

    private static final String DEFAULT_FIRST_NAME = "AAAAAAAAAA";
    private static final String UPDATED_FIRST_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_LAST_NAME = "AAAAAAAAAA";
    private static final String UPDATED_LAST_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_EMAIL = "AAAAAAAAAA";
    private static final String UPDATED_EMAIL = "BBBBBBBBBB";

    private static final String DEFAULT_PHONE_NUMBER = "AAAAAAAAAA";
    private static final String UPDATED_PHONE_NUMBER = "BBBBBBBBBB";

    private static final LocalDate DEFAULT_BIRTH_DATE = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_BIRTH_DATE = LocalDate.now(ZoneId.systemDefault());

    private static final String ENTITY_API_URL = "/api/user-apps";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";
    private static final String ENTITY_SEARCH_API_URL = "/api/_search/user-apps";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private UserAppRepository userAppRepository;

    /**
     * This repository is mocked in the com.tmrfcb.datingapp.repository.search test package.
     *
     * @see com.tmrfcb.datingapp.repository.search.UserAppSearchRepositoryMockConfiguration
     */
    @Autowired
    private UserAppSearchRepository mockUserAppSearchRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restUserAppMockMvc;

    private UserApp userApp;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static UserApp createEntity(EntityManager em) {
        UserApp userApp = new UserApp()
            .firstName(DEFAULT_FIRST_NAME)
            .lastName(DEFAULT_LAST_NAME)
            .email(DEFAULT_EMAIL)
            .phoneNumber(DEFAULT_PHONE_NUMBER)
            .birthDate(DEFAULT_BIRTH_DATE);
        return userApp;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static UserApp createUpdatedEntity(EntityManager em) {
        UserApp userApp = new UserApp()
            .firstName(UPDATED_FIRST_NAME)
            .lastName(UPDATED_LAST_NAME)
            .email(UPDATED_EMAIL)
            .phoneNumber(UPDATED_PHONE_NUMBER)
            .birthDate(UPDATED_BIRTH_DATE);
        return userApp;
    }

    @BeforeEach
    public void initTest() {
        userApp = createEntity(em);
    }

    @Test
    @Transactional
    void createUserApp() throws Exception {
        int databaseSizeBeforeCreate = userAppRepository.findAll().size();
        // Create the UserApp
        restUserAppMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(userApp)))
            .andExpect(status().isCreated());

        // Validate the UserApp in the database
        List<UserApp> userAppList = userAppRepository.findAll();
        assertThat(userAppList).hasSize(databaseSizeBeforeCreate + 1);
        UserApp testUserApp = userAppList.get(userAppList.size() - 1);
        assertThat(testUserApp.getFirstName()).isEqualTo(DEFAULT_FIRST_NAME);
        assertThat(testUserApp.getLastName()).isEqualTo(DEFAULT_LAST_NAME);
        assertThat(testUserApp.getEmail()).isEqualTo(DEFAULT_EMAIL);
        assertThat(testUserApp.getPhoneNumber()).isEqualTo(DEFAULT_PHONE_NUMBER);
        assertThat(testUserApp.getBirthDate()).isEqualTo(DEFAULT_BIRTH_DATE);

        // Validate the UserApp in Elasticsearch
        verify(mockUserAppSearchRepository, times(1)).save(testUserApp);
    }

    @Test
    @Transactional
    void createUserAppWithExistingId() throws Exception {
        // Create the UserApp with an existing ID
        userApp.setId(1L);

        int databaseSizeBeforeCreate = userAppRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restUserAppMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(userApp)))
            .andExpect(status().isBadRequest());

        // Validate the UserApp in the database
        List<UserApp> userAppList = userAppRepository.findAll();
        assertThat(userAppList).hasSize(databaseSizeBeforeCreate);

        // Validate the UserApp in Elasticsearch
        verify(mockUserAppSearchRepository, times(0)).save(userApp);
    }

    @Test
    @Transactional
    void getAllUserApps() throws Exception {
        // Initialize the database
        userAppRepository.saveAndFlush(userApp);

        // Get all the userAppList
        restUserAppMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(userApp.getId().intValue())))
            .andExpect(jsonPath("$.[*].firstName").value(hasItem(DEFAULT_FIRST_NAME)))
            .andExpect(jsonPath("$.[*].lastName").value(hasItem(DEFAULT_LAST_NAME)))
            .andExpect(jsonPath("$.[*].email").value(hasItem(DEFAULT_EMAIL)))
            .andExpect(jsonPath("$.[*].phoneNumber").value(hasItem(DEFAULT_PHONE_NUMBER)))
            .andExpect(jsonPath("$.[*].birthDate").value(hasItem(DEFAULT_BIRTH_DATE.toString())));
    }

    @Test
    @Transactional
    void getUserApp() throws Exception {
        // Initialize the database
        userAppRepository.saveAndFlush(userApp);

        // Get the userApp
        restUserAppMockMvc
            .perform(get(ENTITY_API_URL_ID, userApp.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(userApp.getId().intValue()))
            .andExpect(jsonPath("$.firstName").value(DEFAULT_FIRST_NAME))
            .andExpect(jsonPath("$.lastName").value(DEFAULT_LAST_NAME))
            .andExpect(jsonPath("$.email").value(DEFAULT_EMAIL))
            .andExpect(jsonPath("$.phoneNumber").value(DEFAULT_PHONE_NUMBER))
            .andExpect(jsonPath("$.birthDate").value(DEFAULT_BIRTH_DATE.toString()));
    }

    @Test
    @Transactional
    void getNonExistingUserApp() throws Exception {
        // Get the userApp
        restUserAppMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putNewUserApp() throws Exception {
        // Initialize the database
        userAppRepository.saveAndFlush(userApp);

        int databaseSizeBeforeUpdate = userAppRepository.findAll().size();

        // Update the userApp
        UserApp updatedUserApp = userAppRepository.findById(userApp.getId()).get();
        // Disconnect from session so that the updates on updatedUserApp are not directly saved in db
        em.detach(updatedUserApp);
        updatedUserApp
            .firstName(UPDATED_FIRST_NAME)
            .lastName(UPDATED_LAST_NAME)
            .email(UPDATED_EMAIL)
            .phoneNumber(UPDATED_PHONE_NUMBER)
            .birthDate(UPDATED_BIRTH_DATE);

        restUserAppMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedUserApp.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(updatedUserApp))
            )
            .andExpect(status().isOk());

        // Validate the UserApp in the database
        List<UserApp> userAppList = userAppRepository.findAll();
        assertThat(userAppList).hasSize(databaseSizeBeforeUpdate);
        UserApp testUserApp = userAppList.get(userAppList.size() - 1);
        assertThat(testUserApp.getFirstName()).isEqualTo(UPDATED_FIRST_NAME);
        assertThat(testUserApp.getLastName()).isEqualTo(UPDATED_LAST_NAME);
        assertThat(testUserApp.getEmail()).isEqualTo(UPDATED_EMAIL);
        assertThat(testUserApp.getPhoneNumber()).isEqualTo(UPDATED_PHONE_NUMBER);
        assertThat(testUserApp.getBirthDate()).isEqualTo(UPDATED_BIRTH_DATE);

        // Validate the UserApp in Elasticsearch
        verify(mockUserAppSearchRepository).save(testUserApp);
    }

    @Test
    @Transactional
    void putNonExistingUserApp() throws Exception {
        int databaseSizeBeforeUpdate = userAppRepository.findAll().size();
        userApp.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restUserAppMockMvc
            .perform(
                put(ENTITY_API_URL_ID, userApp.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(userApp))
            )
            .andExpect(status().isBadRequest());

        // Validate the UserApp in the database
        List<UserApp> userAppList = userAppRepository.findAll();
        assertThat(userAppList).hasSize(databaseSizeBeforeUpdate);

        // Validate the UserApp in Elasticsearch
        verify(mockUserAppSearchRepository, times(0)).save(userApp);
    }

    @Test
    @Transactional
    void putWithIdMismatchUserApp() throws Exception {
        int databaseSizeBeforeUpdate = userAppRepository.findAll().size();
        userApp.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restUserAppMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(userApp))
            )
            .andExpect(status().isBadRequest());

        // Validate the UserApp in the database
        List<UserApp> userAppList = userAppRepository.findAll();
        assertThat(userAppList).hasSize(databaseSizeBeforeUpdate);

        // Validate the UserApp in Elasticsearch
        verify(mockUserAppSearchRepository, times(0)).save(userApp);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamUserApp() throws Exception {
        int databaseSizeBeforeUpdate = userAppRepository.findAll().size();
        userApp.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restUserAppMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(userApp)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the UserApp in the database
        List<UserApp> userAppList = userAppRepository.findAll();
        assertThat(userAppList).hasSize(databaseSizeBeforeUpdate);

        // Validate the UserApp in Elasticsearch
        verify(mockUserAppSearchRepository, times(0)).save(userApp);
    }

    @Test
    @Transactional
    void partialUpdateUserAppWithPatch() throws Exception {
        // Initialize the database
        userAppRepository.saveAndFlush(userApp);

        int databaseSizeBeforeUpdate = userAppRepository.findAll().size();

        // Update the userApp using partial update
        UserApp partialUpdatedUserApp = new UserApp();
        partialUpdatedUserApp.setId(userApp.getId());

        partialUpdatedUserApp.email(UPDATED_EMAIL).phoneNumber(UPDATED_PHONE_NUMBER);

        restUserAppMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedUserApp.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedUserApp))
            )
            .andExpect(status().isOk());

        // Validate the UserApp in the database
        List<UserApp> userAppList = userAppRepository.findAll();
        assertThat(userAppList).hasSize(databaseSizeBeforeUpdate);
        UserApp testUserApp = userAppList.get(userAppList.size() - 1);
        assertThat(testUserApp.getFirstName()).isEqualTo(DEFAULT_FIRST_NAME);
        assertThat(testUserApp.getLastName()).isEqualTo(DEFAULT_LAST_NAME);
        assertThat(testUserApp.getEmail()).isEqualTo(UPDATED_EMAIL);
        assertThat(testUserApp.getPhoneNumber()).isEqualTo(UPDATED_PHONE_NUMBER);
        assertThat(testUserApp.getBirthDate()).isEqualTo(DEFAULT_BIRTH_DATE);
    }

    @Test
    @Transactional
    void fullUpdateUserAppWithPatch() throws Exception {
        // Initialize the database
        userAppRepository.saveAndFlush(userApp);

        int databaseSizeBeforeUpdate = userAppRepository.findAll().size();

        // Update the userApp using partial update
        UserApp partialUpdatedUserApp = new UserApp();
        partialUpdatedUserApp.setId(userApp.getId());

        partialUpdatedUserApp
            .firstName(UPDATED_FIRST_NAME)
            .lastName(UPDATED_LAST_NAME)
            .email(UPDATED_EMAIL)
            .phoneNumber(UPDATED_PHONE_NUMBER)
            .birthDate(UPDATED_BIRTH_DATE);

        restUserAppMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedUserApp.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedUserApp))
            )
            .andExpect(status().isOk());

        // Validate the UserApp in the database
        List<UserApp> userAppList = userAppRepository.findAll();
        assertThat(userAppList).hasSize(databaseSizeBeforeUpdate);
        UserApp testUserApp = userAppList.get(userAppList.size() - 1);
        assertThat(testUserApp.getFirstName()).isEqualTo(UPDATED_FIRST_NAME);
        assertThat(testUserApp.getLastName()).isEqualTo(UPDATED_LAST_NAME);
        assertThat(testUserApp.getEmail()).isEqualTo(UPDATED_EMAIL);
        assertThat(testUserApp.getPhoneNumber()).isEqualTo(UPDATED_PHONE_NUMBER);
        assertThat(testUserApp.getBirthDate()).isEqualTo(UPDATED_BIRTH_DATE);
    }

    @Test
    @Transactional
    void patchNonExistingUserApp() throws Exception {
        int databaseSizeBeforeUpdate = userAppRepository.findAll().size();
        userApp.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restUserAppMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, userApp.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(userApp))
            )
            .andExpect(status().isBadRequest());

        // Validate the UserApp in the database
        List<UserApp> userAppList = userAppRepository.findAll();
        assertThat(userAppList).hasSize(databaseSizeBeforeUpdate);

        // Validate the UserApp in Elasticsearch
        verify(mockUserAppSearchRepository, times(0)).save(userApp);
    }

    @Test
    @Transactional
    void patchWithIdMismatchUserApp() throws Exception {
        int databaseSizeBeforeUpdate = userAppRepository.findAll().size();
        userApp.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restUserAppMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(userApp))
            )
            .andExpect(status().isBadRequest());

        // Validate the UserApp in the database
        List<UserApp> userAppList = userAppRepository.findAll();
        assertThat(userAppList).hasSize(databaseSizeBeforeUpdate);

        // Validate the UserApp in Elasticsearch
        verify(mockUserAppSearchRepository, times(0)).save(userApp);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamUserApp() throws Exception {
        int databaseSizeBeforeUpdate = userAppRepository.findAll().size();
        userApp.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restUserAppMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(TestUtil.convertObjectToJsonBytes(userApp)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the UserApp in the database
        List<UserApp> userAppList = userAppRepository.findAll();
        assertThat(userAppList).hasSize(databaseSizeBeforeUpdate);

        // Validate the UserApp in Elasticsearch
        verify(mockUserAppSearchRepository, times(0)).save(userApp);
    }

    @Test
    @Transactional
    void deleteUserApp() throws Exception {
        // Initialize the database
        userAppRepository.saveAndFlush(userApp);

        int databaseSizeBeforeDelete = userAppRepository.findAll().size();

        // Delete the userApp
        restUserAppMockMvc
            .perform(delete(ENTITY_API_URL_ID, userApp.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<UserApp> userAppList = userAppRepository.findAll();
        assertThat(userAppList).hasSize(databaseSizeBeforeDelete - 1);

        // Validate the UserApp in Elasticsearch
        verify(mockUserAppSearchRepository, times(1)).deleteById(userApp.getId());
    }

    @Test
    @Transactional
    void searchUserApp() throws Exception {
        // Configure the mock search repository
        // Initialize the database
        userAppRepository.saveAndFlush(userApp);
        when(mockUserAppSearchRepository.search(queryStringQuery("id:" + userApp.getId()), PageRequest.of(0, 20)))
            .thenReturn(new PageImpl<>(Collections.singletonList(userApp), PageRequest.of(0, 1), 1));

        // Search the userApp
        restUserAppMockMvc
            .perform(get(ENTITY_SEARCH_API_URL + "?query=id:" + userApp.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(userApp.getId().intValue())))
            .andExpect(jsonPath("$.[*].firstName").value(hasItem(DEFAULT_FIRST_NAME)))
            .andExpect(jsonPath("$.[*].lastName").value(hasItem(DEFAULT_LAST_NAME)))
            .andExpect(jsonPath("$.[*].email").value(hasItem(DEFAULT_EMAIL)))
            .andExpect(jsonPath("$.[*].phoneNumber").value(hasItem(DEFAULT_PHONE_NUMBER)))
            .andExpect(jsonPath("$.[*].birthDate").value(hasItem(DEFAULT_BIRTH_DATE.toString())));
    }
}
