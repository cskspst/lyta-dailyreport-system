package com.techacademy.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.SQLRestriction;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Entity
@Table(name = "reports")
@SQLRestriction("delete_flg = false")
public class Report {

    // ID
    @Id
    @Column(length = 10)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // 日付
    @Column(nullable = false)
    @NotNull
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate reportDate;

    // タイトル
    @Column(length = 100, nullable = false)
    @NotEmpty
    @Length(max = 100)
    private String title;

    // 内容
    @Column(columnDefinition="LONGTEXT", nullable = false)
    @NotEmpty
    @Length(max = 600)
    // @Length(max = 600)はテーブル定義書ではサイズ未定義だが画面設計書の入力チェックで600文字以下の項目がある
    private String content;

    // 社員番号(FK)
    @Column(name = "employee_code", length = 10, nullable = false)
    @NotEmpty
    @Length(max = 10)
    private String employeeCode;

    // 削除フラグ(論理削除を行うため)
    @Column(columnDefinition="TINYINT", nullable = false)
    private boolean deleteFlg;

    // 登録日時
    @Column(nullable = false)
    private LocalDateTime createdAt;

    // 更新日時
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // リレーションの設定
    @ManyToOne
    // カリキュラム内の記述ではエラーが出るため修正
    // @JoinColumn(name = "employee_code", referencedColumnName = "code", nullable = false)
    @JoinColumn(name = "employee_code", referencedColumnName = "code", insertable = false, updatable = false, nullable = false)
    private Employee employee;
}