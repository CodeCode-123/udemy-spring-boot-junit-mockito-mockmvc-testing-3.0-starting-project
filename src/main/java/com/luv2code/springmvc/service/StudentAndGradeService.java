package com.luv2code.springmvc.service;

import com.luv2code.springmvc.models.*;
import com.luv2code.springmvc.repository.HistoryGradesDao;
import com.luv2code.springmvc.repository.MathGradesDao;
import com.luv2code.springmvc.repository.ScienceGradesDao;
import com.luv2code.springmvc.repository.StudentDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;

import java.util.ArrayList;
import java.util.List;
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
    @Autowired
    StudentGrades studentGrades;

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
            mathGradesDao.deleteByStudentId(id);
            scienceGradesDao.deleteByStudentId(id);
            historyGradesDao.deleteByStudentId(id);
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
    public int deleteGrade(int id, String gradeType) {
        int studentId = 0;
        if (gradeType.equals("math")) {
            Optional<MathGrade> mathGrade = mathGradesDao.findById(id);
            if (!mathGrade.isPresent()) {
                return studentId;
            }
            studentId = mathGrade.get().getStudentId();
            mathGradesDao.deleteById(id);
        }
        if (gradeType.equals("science")) {
            Optional<ScienceGrade> scienceGrade = scienceGradesDao.findById(id);
            if (!scienceGrade.isPresent()) {
                return studentId;
            }
            studentId = scienceGrade.get().getStudentId();
            scienceGradesDao.deleteById(id);
        }
        if (gradeType.equals("history")) {
            Optional<HistoryGrade> historyGrade = historyGradesDao.findById(id);
            if (!historyGrade.isPresent()) {
                return studentId;
            }
            studentId = historyGrade.get().getStudentId();
            historyGradesDao.deleteById(id);
        }
        return studentId;
    }
    public GradebookCollegeStudent studentInformation(int id) {
        Optional<CollegeStudent> student = studentDao.findById(id);
        if (!student.isPresent()) {
            return null;
        }
        Iterable<MathGrade> mathGrades = mathGradesDao.findGradeByStudentId(id);
        Iterable<ScienceGrade> scienceGrades = scienceGradesDao.findGradeByStudentId(id);
        Iterable<HistoryGrade> historyGrades = historyGradesDao.findGradeByStudentId(id);
        // convert to List<Grade> and add each grade to the list
        List<Grade> mathGradesList = new ArrayList<>();
        mathGrades.forEach(mathGradesList::add);
        List<Grade> scienceGradesList = new ArrayList<>();
        scienceGrades.forEach(scienceGradesList::add);
        List<Grade> historyGradesList = new ArrayList<>();
        historyGrades.forEach(historyGradesList::add);
        // set math, science, and history grades
        studentGrades.setMathGradeResults(mathGradesList);
        studentGrades.setScienceGradeResults(scienceGradesList);
        studentGrades.setHistoryGradeResults(historyGradesList);
        // create the GradebookCollegeStudent object
        GradebookCollegeStudent gradebookCollegeStudent = new GradebookCollegeStudent(
                student.get().getId(), student.get().getFirstname(), student.get().getLastname(),
                student.get().getEmailAddress(), studentGrades);
        return gradebookCollegeStudent;
    }
    public void configureStudentInformationModel(int id, Model m) {
        GradebookCollegeStudent studentEntity = studentInformation(id);
        m.addAttribute("student", studentEntity);
        // model set attribute of math average grade
        if (!studentEntity.getStudentGrades().getMathGradeResults().isEmpty()) {
            m.addAttribute("mathAverage", studentEntity.getStudentGrades().findGradePointAverage(
                    studentEntity.getStudentGrades().getMathGradeResults()
            ));
        } else {
            m.addAttribute("mathAverage", "N/A");
        }
        // model set attribute of science average grade
        if (!studentEntity.getStudentGrades().getScienceGradeResults().isEmpty()) {
            m.addAttribute("scienceAverage", studentEntity.getStudentGrades().findGradePointAverage(
                    studentEntity.getStudentGrades().getScienceGradeResults()
            ));
        } else {
            m.addAttribute("scienceAverage", "N/A");
        }
        // model set attribute of history average grade
        if (!studentEntity.getStudentGrades().getHistoryGradeResults().isEmpty()) {
            m.addAttribute("historyAverage", studentEntity.getStudentGrades().findGradePointAverage(
                    studentEntity.getStudentGrades().getHistoryGradeResults()
            ));
        } else {
            m.addAttribute("historyAverage", "N/A");
        }
    }
}
