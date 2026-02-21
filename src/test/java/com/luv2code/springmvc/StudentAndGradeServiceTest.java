package com.luv2code.springmvc;

import com.luv2code.springmvc.models.*;
import com.luv2code.springmvc.repository.HistoryGradesDao;
import com.luv2code.springmvc.repository.MathGradesDao;
import com.luv2code.springmvc.repository.ScienceGradesDao;
import com.luv2code.springmvc.repository.StudentDao;
import com.luv2code.springmvc.service.StudentAndGradeService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;

import java.util.*;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@TestPropertySource("/application-test.properties")
@SpringBootTest
public class StudentAndGradeServiceTest {
    @Autowired
    private JdbcTemplate jdbc;
    @Autowired
    private StudentAndGradeService studentService;
    @Autowired
    private StudentDao studentDao;
    @Autowired
    private MathGradesDao mathGradesDao;
    @Autowired
    private ScienceGradesDao scienceGradesDao;
    @Autowired
    private HistoryGradesDao historyGradesDao;
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

    @BeforeEach
    public void setupDatabase() {
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
    public void createStudentAndGradeTest() {
        studentService.createStudent("Chad", "Darby", "chad.darby@luv2code_school.com");
        CollegeStudent student = studentDao.findByEmailAddress("chad.darby@luv2code_school.com");
        assertEquals("chad.darby@luv2code_school.com", student.getEmailAddress(), "find by email");
    }
    @Test
    public void isStudentNullCheck() {
        assertTrue(studentService.checkIfStudentPresent(1));
        assertFalse(studentService.checkIfStudentPresent(0));
    }
    @Test
    public void deleteStudentService() {
        int id = 1;
        int mathGradeId = 1;
        int scienceGradeId = 1;
        int historyGradeId = 1;
        // retrieve the college student
        Optional<CollegeStudent> deletedColledgeStudent = studentDao.findById(id);
        Optional<MathGrade> deletedMathGrade = mathGradesDao.findById(mathGradeId);
        Optional<ScienceGrade> deletedScienceGrade = scienceGradesDao.findById(scienceGradeId);
        Optional<HistoryGrade> deletedHistoryGrade = historyGradesDao.findById(historyGradeId);
        assertTrue(deletedColledgeStudent.isPresent(), "Return True");
        assertTrue(deletedMathGrade.isPresent(), "Return True");
        assertTrue(deletedScienceGrade.isPresent(), "Return True");
        assertTrue(deletedHistoryGrade.isPresent(), "Return True");
        // delete the student and math, science, and history grades
        studentService.deleteStudent(id);
        deletedColledgeStudent = studentDao.findById(id);
        deletedMathGrade = mathGradesDao.findById(mathGradeId);
        deletedScienceGrade = scienceGradesDao.findById(scienceGradeId);
        deletedHistoryGrade = historyGradesDao.findById(historyGradeId);
        assertFalse( deletedColledgeStudent.isPresent(), "Return False");
        assertFalse( deletedMathGrade.isPresent(), "Return False");
        assertFalse( deletedScienceGrade.isPresent(), "Return False");
        assertFalse( deletedHistoryGrade.isPresent(), "Return False");
    }
    // Overall sequence: @BeforeEach, then @Sql insertData.sql, and getGradebookService() test
    // new records, 1 from @BeforeEach, 4 from @Sql
    @Sql("/insertData.sql") // execute the SQL before the test method.
    @Test
    public void getGradebookService() {
        Iterable<CollegeStudent> iterableCollegeStudents = studentService.getGradebook();
        List<CollegeStudent> collegeStudents = new ArrayList<>();
        for (CollegeStudent collegeStudent : iterableCollegeStudents) {
            collegeStudents.add(collegeStudent);
        }
        assertEquals(5, collegeStudents.size());
    }
    @Test
    public void createGradeService() {
        int studentId = 1;
        // Create the grade
        assertTrue(studentService.createGrade(80.50, studentId, "math"));
        assertTrue(studentService.createGrade(80.50, studentId, "science"));
        assertTrue(studentService.createGrade(80.50, studentId, "history"));
        // Get all grades with studentId
        Iterable<MathGrade> mathGrades = mathGradesDao.findGradeByStudentId(studentId);
        Iterable<ScienceGrade> scienceGrades = scienceGradesDao.findGradeByStudentId(studentId);
        Iterable<HistoryGrade> historyGrades = historyGradesDao.findGradeByStudentId(studentId);
        // Verify there is grades
        assertEquals(2, ((Collection<MathGrade>) mathGrades).size(),
                "Student has 2 math grades");
        assertEquals(2, ((Collection<ScienceGrade>) scienceGrades).size(),
                "Student has 2 science grades");
        assertEquals(2, ((Collection<HistoryGrade>) historyGrades).size(),
                "Student has 2 history grades");
    }
    @Test
    public void createGradeServiceReturnFalse() {
        assertFalse(studentService.createGrade(105, 1, "math"));
        assertFalse(studentService.createGrade(-5, 1, "math"));
        assertFalse(studentService.createGrade(80.50, 2, "math"));
        assertFalse(studentService.createGrade(80.50, 1, "literature"));
    }
    @Test
    public void deleteGradeService() {
        int studentId = 1;
        int gradeId = 1;
        assertEquals(studentId, studentService.deleteGrade(gradeId, "math"),
                "Returns student id after delete");
        assertEquals(studentId, studentService.deleteGrade(gradeId, "science"),
                "Returns student id after delete");
        assertEquals(studentId, studentService.deleteGrade(gradeId, "history"),
                "Returns student id after delete");
    }
    @Test
    public void deleteGradeServiceReturnStudentIdOfZero() {
        int studentId = 0;
        assertEquals(studentId, studentService.deleteGrade(0, "science"),
                "No student should have 0 id");
        assertEquals(studentId, studentService.deleteGrade(1, "literature"),
                "No student should have a literature class");
    }
    @Test
    public void studentInformation() {
        GradebookCollegeStudent gradebookCollegeStudent = studentService.studentInformation(1);
        assertNotNull(gradebookCollegeStudent);
        assertEquals(1, gradebookCollegeStudent.getId());
        assertEquals("Eric", gradebookCollegeStudent.getFirstname());
        assertEquals("Roby", gradebookCollegeStudent.getLastname());
        assertEquals("eric.roby@luv2code_school.com", gradebookCollegeStudent.getEmailAddress());
        assertEquals(1, gradebookCollegeStudent.getStudentGrades().getMathGradeResults().size());
        assertEquals(1, gradebookCollegeStudent.getStudentGrades().getScienceGradeResults().size());
        assertEquals(1, gradebookCollegeStudent.getStudentGrades().getHistoryGradeResults().size());
    }
    @Test
    public void studentInformationServiceReturnNull() {
        GradebookCollegeStudent gradebookCollegeStudent = studentService.studentInformation(0);
        assertNull(gradebookCollegeStudent);
    }
}
