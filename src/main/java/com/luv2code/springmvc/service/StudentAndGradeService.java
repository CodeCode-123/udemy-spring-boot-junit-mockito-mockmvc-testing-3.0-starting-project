package com.luv2code.springmvc.service;

import com.luv2code.springmvc.models.CollegeStudent;
import com.luv2code.springmvc.models.HistoryGrade;
import com.luv2code.springmvc.models.MathGrade;
import com.luv2code.springmvc.models.ScienceGrade;
import com.luv2code.springmvc.repository.HistoryGradesDao;
import com.luv2code.springmvc.repository.MathGradesDao;
import com.luv2code.springmvc.repository.ScienceGradesDao;
import com.luv2code.springmvc.repository.StudentDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class StudentAndGradeService {
    @Autowired
    private StudentDao studentDao;
    @Autowired
    @Qualifier("mathGrades")
    private MathGrade mathGrade;
    @Autowired
    @Qualifier("scienceGrades")
    private ScienceGrade scienceGrade;
    @Autowired
    @Qualifier("historyGrades")
    private HistoryGrade historyGrade;
    @Autowired
    private MathGradesDao mathGradesDao;
    @Autowired
    private ScienceGradesDao scienceGradesDao;
    @Autowired
    private HistoryGradesDao historyGradesDao;

    public void createStudent(String firstname, String lastname, String emailAddress) {
        CollegeStudent student = new CollegeStudent(firstname, lastname, emailAddress);
        student.setId(0);
        studentDao.save(student);
    }

    public boolean checkIfStudentPresent(int id) {
        Optional<CollegeStudent> collegeStudent = studentDao.findById(id);
        if (collegeStudent.isPresent()) {
            return true;
        }
        return false;
    }

    public void deleteStudent(int id) {
        if (checkIfStudentPresent(id)) {
            studentDao.deleteById(id);
        }
    }

    public Iterable<CollegeStudent> getGradebook() {
        return studentDao.findAll();
    }

    public boolean createGrade(double grade, int studentId, String gradeType) {
        if (!checkIfStudentPresent(studentId)) {
            return false;
        }
        if (grade >= 0 && grade <= 100) {
            if (gradeType.equals("math")) {
                mathGrade.setId(0);
                mathGrade.setGrade(grade);
                mathGrade.setStudentId(studentId);
                mathGradesDao.save(mathGrade);
                return true;
            }
            if (gradeType.equals("science")) {
                scienceGrade.setId(0);
                scienceGrade.setGrade(grade);
                scienceGrade.setStudentId(studentId);
                scienceGradesDao.save(scienceGrade);
                return true;
            }
            if (gradeType.equals("history")) {
                historyGrade.setId(0);
                historyGrade.setGrade(grade);
                historyGrade.setStudentId(studentId);
                historyGradesDao.save(historyGrade);
                return true;
            }
        }
        return false;
    }
}
