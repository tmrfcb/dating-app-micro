package com.tmrfcb.datingapp.repository.search;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Configuration;

/**
 * Configure a Mock version of {@link RelationSearchRepository} to test the
 * application without starting Elasticsearch.
 */
@Configuration
public class RelationSearchRepositoryMockConfiguration {

    @MockBean
    private RelationSearchRepository mockRelationSearchRepository;
}
