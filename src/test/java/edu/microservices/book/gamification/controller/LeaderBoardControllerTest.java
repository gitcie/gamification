package edu.microservices.book.gamification.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.microservices.book.gamification.domain.LeaderBoardRow;
import edu.microservices.book.gamification.service.LeaderBoardService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 * class summary
 * <p>
 * class detail description
 *
 * @author Siyi Lu
 * @since 2020/7/9
 */
@RunWith(SpringRunner.class)
@WebMvcTest(LeaderBoardController.class)
public class LeaderBoardControllerTest {

    @MockBean
    private LeaderBoardService leaderBoardService;

    @Autowired
    private MockMvc mvc;

    private JacksonTester<List<LeaderBoardRow>> jsonTester;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        JacksonTester.initFields(this, new ObjectMapper());
    }

    @Test
    public void getLeaderBoard() throws Exception{
        Long userId = 0L;
        LeaderBoardRow leaderBoard = new LeaderBoardRow(userId, 5555L);
        given(leaderBoardService.getCurrentLeaderBoard())
                .willReturn(Collections.singletonList(leaderBoard));
        MockHttpServletResponse response = mvc.perform(
                get("/leaders")
                    .accept(MediaType.APPLICATION_JSON)
        ).andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo(
                jsonTester.write(Collections.singletonList(leaderBoard)).getJson()
        );
    }
}