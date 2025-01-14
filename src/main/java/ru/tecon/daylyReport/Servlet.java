package ru.tecon.daylyReport;

import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ru.tecon.daylyReport.ejb.DaylyReportBean;


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
