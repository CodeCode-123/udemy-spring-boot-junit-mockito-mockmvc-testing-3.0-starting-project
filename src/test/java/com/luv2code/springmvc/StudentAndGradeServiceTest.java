package com.luv2code.springmvc;

import com.luv2code.springmvc.models.CollegeStudent;
import com.luv2code.springmvc.models.HistoryGrade;
import com.luv2code.springmvc.models.MathGrade;
import com.luv2code.springmvc.models.ScienceGrade;
import com.luv2code.springmvc.repository.HistoryGradesDao;
import com.luv2code.springmvc.repository.MathGradesDao;
import com.luv2code.springmvc.repository.ScienceGradesDao;
import com.luv2code.springmvc.repository.StudentDao;
import com.luv2code.springmvc.service.StudentAndGradeService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;

import java.util.*;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@TestPropertySource("/application.properties")
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

    @BeforeEach
    public void setupDatabase() {
        jdbc.execute("INSERT INTO student(firstname, lastname, email_address) " +
                "VALUES ('Eric', 'Roby', 'eric.roby@luv2code_school.com')");
        jdbc.execute("INSERT INTO math_grade(student_id, grade) VALUES (1,100.00)");
        jdbc.execute("INSERT INTO science_grade(student_id, grade) VALUES (1,100.00)");
        jdbc.execute("INSERT INTO history_grade(student_id, grade) VALUES (1,100.00)");
    }
    @AfterEach
    public void setupAfterTransaction() {
        jdbc.execute("DELETE FROM student");
        jdbc.execute("DELETE FROM math_grade");
        jdbc.execute("DELETE FROM science_grade");
        jdbc.execute("DELETE FROM history_grade");
        jdbc.execute("ALTER TABLE student ALTER COLUMN ID RESTART WITH 1");
        jdbc.execute("ALTER TABLE math_grade ALTER COLUMN ID RESTART WITH 1");
        jdbc.execute("ALTER TABLE science_grade ALTER COLUMN ID RESTART WITH 1");
        jdbc.execute("ALTER TABLE history_grade ALTER COLUMN ID RESTART WITH 1");
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
        Optional<CollegeStudent> deletedColledgeStudent = studentDao.findById(id);
        assertTrue(deletedColledgeStudent.isPresent(), "Return True");
        studentService.deleteStudent(id);
        deletedColledgeStudent = studentDao.findById(id);
        assertFalse( deletedColledgeStudent.isPresent(), "Return False");
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
        assertTrue(mathGrades.iterator().hasNext(), "Student has math grades");
        assertTrue(scienceGrades.iterator().hasNext(), "Student has science grades");
        assertTrue(historyGrades.iterator().hasNext(), "Student has history grades");
    }
    @Test
    public void createGradeServiceReturnFalse() {
        assertFalse(studentService.createGrade(105, 1, "math"));
        assertFalse(studentService.createGrade(-5, 1, "math"));
        assertFalse(studentService.createGrade(80.50, 2, "math"));
        assertFalse(studentService.createGrade(80.50, 1, "literature"));
    }
}
