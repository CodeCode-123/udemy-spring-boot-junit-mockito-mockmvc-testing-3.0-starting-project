package com.luv2code.springmvc.repository;

import com.luv2code.springmvc.models.ScienceGrade;
import org.springframework.data.repository.CrudRepository;
import org.springframework.web.bind.annotation.RestController;

@RestController
public interface ScienceGradesDao extends CrudRepository<ScienceGrade, Integer> {
    Iterable<ScienceGrade> findGradeByStudentId(int studentId);
    void deleteByStudentId(int studentId);
}
