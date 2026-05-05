package com.okcir.et.admin.accessright;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "access_rights")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccessRight {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 100)
  private String code;

  @Column(length = 255)
  private String description;

  @Column(name = "parent_code", length = 100)
  private String parentCode;

  @Column(name = "category", length = 50)
  private String category;
}
