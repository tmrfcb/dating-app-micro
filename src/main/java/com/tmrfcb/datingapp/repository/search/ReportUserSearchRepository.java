package com.tmrfcb.datingapp.repository.search;

import com.tmrfcb.datingapp.domain.ReportUser;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Spring Data Elasticsearch repository for the {@link ReportUser} entity.
 */
public interface ReportUserSearchRepository extends ElasticsearchRepository<ReportUser, Long> {}
