package com.techacademy.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.techacademy.entity.Report;

public interface ReportRepository extends JpaRepository<Report, String> {
    public abstract List<Report> findByEmployeeCode(String employeeCode);

}