package com.tmrfcb.datingapp.repository.search;

import com.tmrfcb.datingapp.domain.MatchRelation;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Spring Data Elasticsearch repository for the {@link MatchRelation} entity.
 */
public interface MatchRelationSearchRepository extends ElasticsearchRepository<MatchRelation, Long> {}
