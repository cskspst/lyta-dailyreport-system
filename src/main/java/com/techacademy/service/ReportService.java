package com.techacademy.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.techacademy.constants.ErrorKinds;
import com.techacademy.entity.Report;
import com.techacademy.repository.ReportRepository;

@Service
public class ReportService {

    private final ReportRepository reportRepository;

    @Autowired
    public ReportService(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    // 日報保存
    @Transactional
    public ErrorKinds save(Report report, UserDetail userDetail) {

        // 開始 : 業務チェック部分
        // チェック内容 : "画面で表示中の従業員" かつ "入力した日付"の日報データが存在する場合エラー
        boolean logicCheck = true;
        // 作成者の社員番号と日付を取得する
        String code = report.getEmployeeCode();
        LocalDate date = report.getReportDate();
        // リポジトリから全件のリストを取得
        List<Report> reportList = findAll();
        // リストを１件ずつ走査し、その中のEmployeeCodeがcodeと等しく、かつ、
        // reportDateがdateと等しい場合に "DATECHECK_ERROR" を返す
        for ( Report i : reportList ) {
            if ( ( i.getEmployeeCode().equals(code) ) && ( i.getReportDate().equals(date) )) {
                logicCheck = false;
            }
        }
        if ( !logicCheck ) {
            return ErrorKinds.DATECHECK_ERROR;
        }
        // 終了 : 業務チェック部分

        report.setDeleteFlg(false);

        LocalDateTime now = LocalDateTime.now();
        report.setCreatedAt(now);
        report.setUpdatedAt(now);

        reportRepository.save(report);
        return ErrorKinds.SUCCESS;
    }

    // 日報更新
    @Transactional
    public ErrorKinds update(Report report) {

        // 開始 : 業務チェック部分
        // チェック内容 : 更新する日報データ以外に"画面で表示中の従業員" かつ "入力した日付"の日報データが存在する場合エラー
        boolean logicCheck = true;
        String code = report.getEmployeeCode();
        LocalDate date = report.getReportDate();
        // リポジトリから全件のリストを取得
        List<Report> reportList = findAll();
        // リストを１件ずつ走査し、その中のEmployeeCodeがcodeと等しく、かつ、
        // reportDateがdateと等しい場合に "DATECHECK_ERROR" を返す
        for ( Report i : reportList ) {
            if ( i.getId().equals(report.getId()) ) {
                // 走査中の日報IDと今回更新するIDが同じならスキップ(作成者・日付が存在してもfalseを返さない)
                continue;
            } else {
                if ( ( i.getEmployeeCode().equals(code) ) && ( i.getReportDate().equals(date) )) {
                    logicCheck = false;
                }
            }
        }
        if ( !logicCheck ) {
            return ErrorKinds.DATECHECK_ERROR;
        }
        // 終了 : 業務チェック部分

        // 更新対象日報のデータをリポジトリからtemporaryDataへコピーし、
        // 更新せず値を引き継ぐ項目はこの中からreportへセットする
        Report temporaryData = findByCode((int)report.getId());

        report.setDeleteFlg(false);

        // CreateAt(登録日時)は変更しないため元の値を引き継ぐ
        report.setCreatedAt(temporaryData.getCreatedAt());
        // UpdateAt(更新日時)はnowをセット
        LocalDateTime now = LocalDateTime.now();
        report.setUpdatedAt(now);

        reportRepository.save(report);
        return ErrorKinds.SUCCESS;
    }

    // 従業員削除
    @Transactional
    public ErrorKinds delete(Integer id) {

        Report report = findByCode(id);
        LocalDateTime now = LocalDateTime.now();
        report.setUpdatedAt(now);
        report.setDeleteFlg(true);

        return ErrorKinds.SUCCESS;
    }

    // 日報一覧表示処理
    public List<Report> generateListAtRole(UserDetail userDetail) {

        // ログイン中の従業員の社員番号と権限を取得
        String code = userDetail.getUsername();
        String role = userDetail.getEmployee().getRole().toString();
        List<Report> reportList;

        if ( role.equals("ADMIN") ) {
            // 権限が"ADMIN"なら全件を取得
            reportList = findAll();
        } else {
            // 権限が一般ならその従業員のリストを取得
            reportList = findByEmployee(code);
        }
        return reportList;
    }

    // 全件を検索
    public List<Report> findAll() {
        return reportRepository.findAll();
    }

    // 1件を検索
    public Report findByCode(Integer id) {
        // findByIdで検索
        Optional<Report> option = reportRepository.findById(id.toString());
        // 取得できなかった場合はnullを返す
        Report report = option.orElse(null);
        return report;
    }

    // 従業員を指定して検索
    public List<Report> findByEmployee(String code){
        return reportRepository.findByEmployeeCode(code);
    }
}
