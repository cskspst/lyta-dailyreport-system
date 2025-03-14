package com.techacademy.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.techacademy.constants.ErrorKinds;
import com.techacademy.entity.Employee;
import com.techacademy.entity.Report;
import com.techacademy.repository.EmployeeRepository;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    // 開始 : 追加 : Lesson 34 Chapter 7 課題
    // reportServiceのfindByEmployeeメソッドを使用するためここで定義する
    private final ReportService reportService;
    // 終了 : 追加 : Lesson 34 Chapter 7 課題

    @Autowired
    // 開始 : 変更 : Lesson 34 Chapter 7 課題
    // DIコンテナに登録するオブジェクトの追加に伴いコンストラクタを修正
    public EmployeeService(EmployeeRepository employeeRepository, PasswordEncoder passwordEncoder, ReportService reportService) {
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
        this.reportService = reportService;
    }
    //    public EmployeeService(EmployeeRepository employeeRepository, PasswordEncoder passwordEncoder) {
    //        this.employeeRepository = employeeRepository;
    //        this.passwordEncoder = passwordEncoder;
    //    }
    // 終了 : 変更 : Lesson 34 Chapter 7 課題

    // 従業員保存
    @Transactional
    public ErrorKinds save(Employee employee) {

        // パスワードチェック
        ErrorKinds result = employeePasswordCheck(employee);
        if (ErrorKinds.CHECK_OK != result) {
            return result;
        }

        // 従業員番号重複チェック
        if (findByCode(employee.getCode()) != null) {
            return ErrorKinds.DUPLICATE_ERROR;
        }

        employee.setDeleteFlg(false);

        LocalDateTime now = LocalDateTime.now();
        employee.setCreatedAt(now);
        employee.setUpdatedAt(now);

        employeeRepository.save(employee);
        return ErrorKinds.SUCCESS;
    }

    // 開始 : 追加 : Lesson 34 Chapter 6 課題
    // 従業員更新
    @Transactional
    public ErrorKinds update(Employee employee) {

        // 更新対象従業員のデータをリポジトリからtemporaryDataへコピーする
        // 更新せずに値を引き継ぐ項目はこの中からemployeeへセットする
        Employee temporaryData = findByCode(employee.getCode());

        if (employee.getPassword() == "") {
            // パスワード未入力なら元の値を引き継ぐ
            employee.setPassword(temporaryData.getPassword());
        } else {
            // パスワード入力済ならこの値に変更
            // パスワードチェック(新規登録と同じ処理)
            ErrorKinds result = employeePasswordCheck(employee);
            if (ErrorKinds.CHECK_OK != result) {
                return result;
            }
        }

        employee.setDeleteFlg(false);

        // CreateAt(登録日時)は変更しないため元の値を引き継ぐ
        employee.setCreatedAt(temporaryData.getCreatedAt());
        // UpdateAt(更新日時)はnowをセット
        LocalDateTime now = LocalDateTime.now();
        employee.setUpdatedAt(now);

        employeeRepository.save(employee);
        return ErrorKinds.SUCCESS;
    }
    // 終了 : 追加 : Lesson 34 Chapter 6 課題

    // 従業員削除
    @Transactional
    public ErrorKinds delete(String code, UserDetail userDetail) {

        // 自分を削除しようとした場合はエラーメッセージを表示
        if (code.equals(userDetail.getEmployee().getCode())) {
            return ErrorKinds.LOGINCHECK_ERROR;
        }
        Employee employee = findByCode(code);
        LocalDateTime now = LocalDateTime.now();
        employee.setUpdatedAt(now);
        employee.setDeleteFlg(true);

        // 開始 : 追加 : Lesson 34 Chapter 7 課題
        // 削除した従業員の日報も削除する
        List<Report> reportList = reportService.findByEmployee(code);
        for (Report report : reportList) {
            // 日報（report）のIDを指定して、日報情報を削除
            reportService.delete(report.getId());
        }
        // 終了 : 追加 : Lesson 34 Chapter 7 課題
        return ErrorKinds.SUCCESS;
    }

    // 従業員一覧表示処理
    public List<Employee> findAll() {
        return employeeRepository.findAll();
    }

    // 1件を検索
    public Employee findByCode(String code) {
        // findByIdで検索
        Optional<Employee> option = employeeRepository.findById(code);
        // 取得できなかった場合はnullを返す
        Employee employee = option.orElse(null);
        return employee;
    }

    // 従業員パスワードチェック
    private ErrorKinds employeePasswordCheck(Employee employee) {

        // 従業員パスワードの半角英数字チェック処理
        if (isHalfSizeCheckError(employee)) {

            return ErrorKinds.HALFSIZE_ERROR;
        }

        // 従業員パスワードの8文字～16文字チェック処理
        if (isOutOfRangePassword(employee)) {

            return ErrorKinds.RANGECHECK_ERROR;
        }

        employee.setPassword(passwordEncoder.encode(employee.getPassword()));

        return ErrorKinds.CHECK_OK;
    }

    // 従業員パスワードの半角英数字チェック処理
    private boolean isHalfSizeCheckError(Employee employee) {

        // 半角英数字チェック
        Pattern pattern = Pattern.compile("^[A-Za-z0-9]+$");
        Matcher matcher = pattern.matcher(employee.getPassword());
        return !matcher.matches();
    }

    // 従業員パスワードの8文字～16文字チェック処理
    public boolean isOutOfRangePassword(Employee employee) {

        // 桁数チェック
        int passwordLength = employee.getPassword().length();
        return passwordLength < 8 || 16 < passwordLength;
    }

}
