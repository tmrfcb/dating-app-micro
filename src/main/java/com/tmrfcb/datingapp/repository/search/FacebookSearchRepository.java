package com.tmrfcb.datingapp.repository.search;

import com.tmrfcb.datingapp.domain.Facebook;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Spring Data Elasticsearch repository for the {@link Facebook} entity.
 */
public interface FacebookSearchRepository extends ElasticsearchRepository<Facebook, Long> {}
