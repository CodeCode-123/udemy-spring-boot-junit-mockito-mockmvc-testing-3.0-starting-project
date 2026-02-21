package com.luv2code.springmvc;

import com.luv2code.springmvc.models.CollegeStudent;
import com.luv2code.springmvc.models.GradebookCollegeStudent;
import com.luv2code.springmvc.repository.StudentDao;
import com.luv2code.springmvc.service.StudentAndGradeService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.ModelAndViewAssert;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.servlet.ModelAndView;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestPropertySource("/application.properties")
@AutoConfigureMockMvc
@SpringBootTest
public class GradebookControllerTest {
    private static MockHttpServletRequest request;
    @Autowired
    private JdbcTemplate jdbc;
    @Autowired
    private MockMvc mockMvc;
    @Mock
    private StudentAndGradeService studentAndGradeServiceMock;
    @Autowired
    private StudentDao studentDao;
    @Value("${sql.script.create.student}")
    private String sqlAddStudent;
    @Value("${sql.script.create.math.grade}")
    private String sqlAddMathGrade;
    @Value("${sql.script.create.science.grade}")
    private String sqlAddScienceGrade;
    @Value("${sql.script.create.history.grade}")
    private String sqlAddHistoryGrade;
    @Value("${sql.script.delete.student}")
    private String sqlDeleteStudent;
    @Value("${sql.script.delete.math.grade}")
    private String sqlDeleteMathGrade;
    @Value("${sql.script.delete.science.grade}")
    private String sqlDeleteScienceGrade;
    @Value("${sql.script.delete.history.grade}")
    private String sqlDeleteHistoryGrade;
    @BeforeAll // @BeforeAll methods must be declared as static, also must be public and return void
    public static void setup() {
        request = new MockHttpServletRequest();
        request.setParameter("firstname", "Chad");
        request.setParameter("lastname", "Darby");
        request.setParameter("emailAddress", "chad.darby@luv2code_school.com");
    }
    @BeforeEach
    public void beforeEach() {
        jdbc.execute(sqlAddStudent);
        jdbc.execute(sqlAddMathGrade);
        jdbc.execute(sqlAddScienceGrade);
        jdbc.execute(sqlAddHistoryGrade);
    }
    @AfterEach
    public void setupAfterTransaction() {
        jdbc.execute(sqlDeleteStudent);
        jdbc.execute(sqlDeleteMathGrade);
        jdbc.execute(sqlDeleteScienceGrade);
        jdbc.execute(sqlDeleteHistoryGrade);
    }
    @Test
    public void getStudentsHttpRequest() throws Exception {
        CollegeStudent studentOne = new GradebookCollegeStudent("Eric", "Roby",
                "eric.roby@luv2code_school.com");
        CollegeStudent studentTwo = new GradebookCollegeStudent("Chad", "Darby",
                "chad.darby@luv2code_school.com");
        List<CollegeStudent> collegeStudentList = new ArrayList<>(Arrays.asList(studentOne, studentTwo));
        when(studentAndGradeServiceMock.getGradebook()).thenReturn(collegeStudentList);
        assertIterableEquals(collegeStudentList, studentAndGradeServiceMock.getGradebook());
        // mockMvc, the studentService is annotated by @Mock, and the result from the studentService is pre-defined
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/"))
                .andExpect(status().isOk()).andReturn();
        ModelAndView mav = mvcResult.getModelAndView();
        ModelAndViewAssert.assertViewName(mav, "index");
    }
    @Test
    public void createStudentHttpRequest() throws Exception {
        // test @PostMapping("/") createStudent method in the GradebookController class
        // request is preset to contain Chad's information in the @BeforeAll method,
        // here we create a student of Chad and retrieve him
        // studentAndGradeServiceMock is annotated by @Mock
        CollegeStudent studentOne = new CollegeStudent("Eric", "Roby",
                "eric_roby@luv2code_school.com");
        List<CollegeStudent> collegeStudentList = new ArrayList<>(Arrays.asList(studentOne));
        when(studentAndGradeServiceMock.getGradebook()).thenReturn(collegeStudentList);
        assertIterableEquals(collegeStudentList, studentAndGradeServiceMock.getGradebook());

        MvcResult mvcResult = mockMvc.perform(post("/")
                .contentType(MediaType.APPLICATION_JSON)
                .param("firstname", request.getParameterValues("firstname"))
                .param("lastname", request.getParameterValues("lastname"))
                .param("emailAddress", request.getParameterValues("emailAddress")))
                .andExpect(status().isOk()).andReturn();
        ModelAndView mav = mvcResult.getModelAndView();
        ModelAndViewAssert.assertViewName(mav, "index");
        // after insert or Post method to create the student, the CollegeStudent should be found by emailAddress
        CollegeStudent verifyStudent = studentDao
                .findByEmailAddress("chad.darby@luv2code_school.com");
        assertNotNull(verifyStudent, "Student should be found");
    }
    @Test
    public void deleteStudentHttpRequest() throws Exception {
        int id = 1;
        assertTrue(studentDao.findById(id).isPresent());
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
                .get("/delete/student/{id}", id))
                .andExpect(status().isOk()).andReturn();
        ModelAndView mav = mvcResult.getModelAndView();
        ModelAndViewAssert.assertViewName(mav, "index");
        assertFalse(studentDao.findById(id).isPresent());
    }
    @Test
    public void deleteStudentHttpRequestErrorPage() throws Exception {
        int id = 0;
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
                .get("/delete/student/{id}", id))
                .andExpect(status().isOk()).andReturn();
        ModelAndView mav = mvcResult.getModelAndView();
        ModelAndViewAssert.assertViewName(mav, "error");
    }
}
