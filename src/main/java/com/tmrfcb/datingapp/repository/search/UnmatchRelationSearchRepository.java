package com.tmrfcb.datingapp.repository.search;

import com.tmrfcb.datingapp.domain.UnmatchRelation;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Spring Data Elasticsearch repository for the {@link UnmatchRelation} entity.
 */
public interface UnmatchRelationSearchRepository extends ElasticsearchRepository<UnmatchRelation, Long> {}
