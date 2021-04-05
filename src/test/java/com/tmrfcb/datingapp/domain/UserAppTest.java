package com.tmrfcb.datingapp.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.tmrfcb.datingapp.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class UserAppTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(UserApp.class);
        UserApp userApp1 = new UserApp();
        userApp1.setId(1L);
        UserApp userApp2 = new UserApp();
        userApp2.setId(userApp1.getId());
        assertThat(userApp1).isEqualTo(userApp2);
        userApp2.setId(2L);
        assertThat(userApp1).isNotEqualTo(userApp2);
        userApp1.setId(null);
        assertThat(userApp1).isNotEqualTo(userApp2);
    }
}
