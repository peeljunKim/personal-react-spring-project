package org.personal.project.connectionPoolOptimization;

import jakarta.persistence.*;

@Entity
@Table(name = "noop") // ddl-auto:update면 자동 생성될 수 있음
public class Dummy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    // 필드 없음: "존재" 자체가 목적
}