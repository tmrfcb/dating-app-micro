package com.tmrfcb.datingapp.repository.search;

import com.tmrfcb.datingapp.domain.UserApp;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Spring Data Elasticsearch repository for the {@link UserApp} entity.
 */
public interface UserAppSearchRepository extends ElasticsearchRepository<UserApp, Long> {}
