package ru.tecon.daylyReport;

import ru.tecon.daylyReport.ejb.DaylyReportBean;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


@WebServlet("/daylyReport")
public class Servlet extends HttpServlet {

    @EJB
    private DaylyReportBean daylyReportBean;
    @EJB

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String timestamp = req.getParameter("timestamp");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        LocalDateTime timestampLDT = LocalDateTime.parse(timestamp, formatter);

        daylyReportBean.createReport(timestampLDT);


        resp.setStatus(HttpServletResponse.SC_OK);
    }

}
