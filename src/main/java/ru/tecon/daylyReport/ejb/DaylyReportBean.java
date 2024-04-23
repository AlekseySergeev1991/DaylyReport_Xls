package ru.tecon.daylyReport.ejb;

import ru.tecon.daylyReport.DaylyReport;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Stateless
public class DaylyReportBean {

    @Resource(name = "jdbc/DataSourceR")
    private DataSource dsR;
    @Resource(name = "jdbc/DataSource")
    private DataSource dsRW;

    public void createReport(LocalDateTime timestamp) {

//        System.out.println(timestamp);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            DaylyReport.createTreads(timestamp, dsR, dsRW);
        });
    }
}
