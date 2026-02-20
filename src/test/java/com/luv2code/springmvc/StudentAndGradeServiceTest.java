package com.luv2code.springmvc;

import com.luv2code.springmvc.models.CollegeStudent;
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

    @BeforeEach
    public void setupDatabase() {
        jdbc.execute("INSERT INTO student(firstname, lastname, email_address) " +
                "VALUES ('Eric', 'Roby', 'eric.roby@luv2code_school.com')");
    }
    @AfterEach
    public void setupAfterTransaction() {
        jdbc.execute("DELETE FROM student");
        jdbc.execute("ALTER TABLE student ALTER COLUMN ID RESTART WITH 1");
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
}
