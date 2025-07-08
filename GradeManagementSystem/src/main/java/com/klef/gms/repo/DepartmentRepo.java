package com.klef.gms.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.klef.gms.model.Department;

public interface DepartmentRepo extends JpaRepository<Department, Long> {
}