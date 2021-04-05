package com.tmrfcb.datingapp.repository.search;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Configuration;

/**
 * Configure a Mock version of {@link FacebookSearchRepository} to test the
 * application without starting Elasticsearch.
 */
@Configuration
public class FacebookSearchRepositoryMockConfiguration {

    @MockBean
    private FacebookSearchRepository mockFacebookSearchRepository;
}
