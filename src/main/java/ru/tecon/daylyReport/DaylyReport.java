package ru.tecon.daylyReport;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.RegionUtil;
import org.apache.poi.xssf.streaming.SXSSFCell;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFColor;
import ru.tecon.daylyReport.model.ObjectParams;
import ru.tecon.daylyReport.model.User;
import ru.tecon.daylyReport.model.WbForSave;

import javax.sql.DataSource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Date;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DaylyReport {
    private static final Logger LOGGER = Logger.getLogger(DaylyReport.class.getName());

    private static final String GET_USERS = "select * from make_dayly_e_report.sel_user_list()";
    private static final String GET_STRUCT = "select * from make_dayly_e_report.sel_struct_list()";
    private static final String GET_OBJECTS = "select * from make_dayly_e_report.get_user_object(?)";
    private static final String GET_STRUCT_OBJECTS = "select * from make_dayly_report.getobjectsbystruct(?)";
    private static final String GET_PARAMS = "select * from make_dayly_report.get_obj_all_param_value1(?, ?, ?)";
    private static final String SAVE_BLOB_USER = "select * from make_dayly_e_report.createdaylyreport_sys_user(?, ?, ?, ?)";
    private static final String SAVE_BLOB_STRUCT = "select * from make_dayly_e_report.createdaylyreport_sys_struct(?, ?, ?, ?)";


    private DataSource dsR;
    public void setDsR(DataSource dsR) {
        this.dsR = dsR;
    }

    private DataSource dsRW;

    public void setDsRW(DataSource dsRW) {
        this.dsRW = dsRW;
    }

    public static void createTreads (LocalDateTime timestamp, DataSource dsR, DataSource dsRW) {

        //запрашиваем список пользователей журналов
        List<User> users = getUsers(dsR);

        //запрашиваем список пользователей журналов
        List<User> struct = getStruct(dsR);

        int treadsCount = 10;

        final BlockingQueue<User> queue = new ArrayBlockingQueue<>(users.size() +struct.size() );
        queue.addAll(users);
        queue.addAll(struct);
        ExecutorService pool = Executors.newFixedThreadPool(treadsCount);
        for (int i = 0; i < treadsCount; i++) {
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    User user;
                    while ((user = queue.poll()) != null) {
                        makeReports(timestamp, user, dsR, dsRW);
                    }
                }
            };
            pool.execute(r);
        }
    }


    public static void makeReports (LocalDateTime timestamp, User user, DataSource dsR, DataSource dsRW) {
        long currentTime = System.nanoTime();

        DaylyReport dr = new DaylyReport();
        dr.setDsR(dsR);
        dr.setDsRW(dsRW);
        WbForSave w;

        if (user.getTag().equals("User")) {
            LOGGER.log(Level.INFO, "start make report for user {0}", user.getUserId());
        } else {
            LOGGER.log(Level.INFO, "start make report for struct {0}", user.getUserName());
        }

        try {
            w = dr.printReport(user, timestamp, dsR, dsRW);
                dr.saveReportIntoTable (w, user, timestamp, dsRW);
        } catch (IOException | SQLException | ParseException | DecoderException e) {
            LOGGER.log(Level.WARNING, "makeReport error", e);
        }
        if (user.getTag().equals("User")) {
            LOGGER.log(Level.INFO, "report created for user {0} created time {1}", new java.lang.Object[]{user.getUserId(), (System.nanoTime() - currentTime)});
        } else {
            LOGGER.log(Level.INFO, "report created for struct {0} created time {1}", new java.lang.Object[]{user.getUserName(), (System.nanoTime() - currentTime)});
        }
    }

    /*
      Метод создает нужный воркбук. Параметры:
      Rep_Id - идентификатор отчета
      Rep_Type - тип репорта. Принимает на вход один символ, малая латинская буква: h - часовой, d - дневной, m - месячный
      Beg_Date - Начальная дата в ткстовом формате (тут пишу в ораклиной нотации) dd-mm-yyyy hh24:mi
      End_Date - Конечная дата в ткстовом формате (тут пишу в ораклиной нотации) dd-mm-yyyy hh24:mi. Прекрасно понимаю, что ее можно рассчитать
                   с помощью количества колонок, типа отчета и начальной даты
      Rows - количество строк в отчете
      Data_Cols - количество колонок - значений параметров. В шапке на 4 колонки больше

    */
    public WbForSave printReport (User user, LocalDateTime timestamp, DataSource dsR, DataSource dsRW) throws IOException, SQLException, ParseException, DecoderException {
        SXSSFWorkbook wb = new SXSSFWorkbook();
        String crash = "N";
        SXSSFSheet sh = wb.createSheet("Отчет");
        CellStyle headerStyle = setHeaderStyle(wb);
        CellStyle headerStyleNoBold = setHeaderStyleNoBold(wb);
        CellStyle nowStyle = setCellNow (wb);
        CellStyle tableHeaderStyle = setTableHeaderStyle(wb);
        CellStyle cellNoBoldStyle = setCellNoBoldStyle(wb);

        // Устанавливаем ширины колонок. В конце мероприятия
        sh.setColumnWidth(0, 7 * 256);
        sh.setColumnWidth(1, 32 * 256);
        sh.setColumnWidth(2, 45 * 256);
        sh.setColumnWidth(3, 19 * 256);
        sh.setColumnWidth(4, 10 * 256);
        sh.setColumnWidth(5, 16 * 256);
        sh.setColumnWidth(6, 10 * 256);
        sh.setColumnWidth(8, 12 * 256);
        sh.setColumnWidth(10, 10 * 256);
        sh.setColumnWidth(12, 12 * 256);
        sh.setColumnWidth(14, 10 * 256);
        sh.setColumnWidth(16, 12 * 256);
        sh.setColumnWidth(18, 10 * 256);
        sh.setColumnWidth(20, 12 * 256);
        sh.setColumnWidth(22, 13 * 256);
        sh.setColumnWidth(24, 13 * 256);
        sh.setColumnWidth(26, 13 * 256);
        sh.setColumnWidth(28, 13 * 256);
        sh.setColumnWidth(30, 17 * 256);
        sh.setColumnWidth(32, 17 * 256);
        sh.setColumnWidth(34, 17 * 256);
        sh.setColumnWidth(36, 17 * 256);
        sh.setColumnWidth(38, 17 * 256);
        sh.setColumnWidth(40, 17 * 256);
        sh.setColumnWidth(42, 17 * 256);
        sh.setColumnWidth(44, 8 * 256);
        sh.setColumnWidth(46, 15 * 256);
        sh.setColumnWidth(48, 17 * 256);
        sh.setColumnWidth(50, 20 * 256);
        sh.setColumnWidth(52, 20 * 256);
        sh.setColumnWidth(54, 40 * 256);
        sh.setColumnWidth(55, 40 * 256);

        sh.createFreezePane(4, 10);

        SXSSFRow row_1 = sh.createRow(0);
        row_1.setHeight((short) 435);
        SXSSFCell cell_1_1 = row_1.createCell(0);
        cell_1_1.setCellValue("ПАО \"МОЭК\": АС \"ТЕКОН - Диспетчеризация\"");

        CellRangeAddress title = new CellRangeAddress(0, 0, 0, 3);
        sh.addMergedRegion(title);
        cell_1_1.setCellStyle(headerStyle);

        SXSSFRow row_2 = sh.createRow(1);
        row_2.setHeight((short) 435);
        SXSSFCell cell_2_1 = row_2.createCell(0);
        cell_2_1.setCellValue("Журнал регистрации параметров и показаний в тепловых пунктах");
        CellRangeAddress formName = new CellRangeAddress(1, 1, 0, 3);
        sh.addMergedRegion(formName);
        cell_2_1.setCellStyle(headerStyle);

        SXSSFRow row_3 = sh.createRow(2);
        row_3.setHeight((short) 435);
        SXSSFCell cell_3_1 = row_3.createCell(0);
        cell_3_1.setCellValue("Техпроцесс: Все");
        CellRangeAddress techproName = new CellRangeAddress(2, 2, 0, 3);
        sh.addMergedRegion(techproName);
        cell_3_1.setCellStyle(headerStyle);

        SXSSFRow row_4 = sh.createRow(3);
        row_4.setHeight((short) 435);
        SXSSFCell cell_4_1 = row_4.createCell(0);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        String stringTimestamp = timestamp.format(formatter);
        cell_4_1.setCellValue("Дата: " + stringTimestamp);
        cell_4_1.setCellStyle(headerStyleNoBold);
        CellRangeAddress period = new CellRangeAddress(3, 3, 0, 3);
        sh.addMergedRegion(period);
        cell_4_1.setCellStyle(headerStyleNoBold);

        SXSSFRow row_5 = sh.createRow(4);
        row_5.setHeight((short) 435);
        SXSSFCell cell_5_1 = row_5.createCell(0);
        if (user.getTag().equals("User")) {
            cell_5_1.setCellValue("Пользователь: " + user.getUserName());
        } else {
            cell_5_1.setCellValue("Подразделение: " + user.getUserName());
        }
        CellRangeAddress userName = new CellRangeAddress(4, 4, 0, 3);
        sh.addMergedRegion(userName);
        cell_5_1.setCellStyle(headerStyleNoBold);

        // Печатаем отчетов зад общий для всех отчетов
        String now = new SimpleDateFormat("dd.MM.yyyy HH:mm").format(new Date());
        SXSSFRow row_6 = sh.createRow(5);
        row_6.setHeight((short) 435);
        SXSSFCell cell_6_1 = row_6.createCell(0);
        cell_6_1.setCellStyle(nowStyle);
        cell_6_1.setCellValue("Отчет сформирован  " + now);
        CellRangeAddress nowDone = new CellRangeAddress(5, 5, 0, 3);
        sh.addMergedRegion(nowDone);

        // готовим шапку таблицы
        SXSSFRow row_8 = sh.createRow(7);
        row_8.setHeight((short) 350);
        SXSSFRow row_9 = sh.createRow(8);
        row_9.setHeight((short) 350);
        SXSSFRow row_10 = sh.createRow(9);
        row_10.setHeight((short) 1400);

        SXSSFCell cell_8_1 = row_8.createCell(0);
        cell_8_1.setCellStyle(tableHeaderStyle);
        cell_8_1.setCellValue("№ п/п");
        CellRangeAddress headerNum = new CellRangeAddress(7, 9, 0, 0);
        sh.addMergedRegion(headerNum);
        CellRangeAddress borderForNum = new CellRangeAddress(7, 9, 0, 0);
        RegionUtil.setBorderBottom(BorderStyle.THICK, borderForNum, sh);
        RegionUtil.setBorderTop(BorderStyle.THICK, borderForNum, sh);
        RegionUtil.setBorderLeft(BorderStyle.THICK, borderForNum, sh);
        RegionUtil.setBorderRight(BorderStyle.THICK, borderForNum, sh);

        SXSSFCell cell_8_2 = row_8.createCell(1);
        cell_8_2.setCellStyle(tableHeaderStyle);
        cell_8_2.setCellValue("Наименование объекта");
        CellRangeAddress headerObj = new CellRangeAddress(7, 9, 1, 1);
        sh.addMergedRegion(headerObj);
        CellRangeAddress borderForObj = new CellRangeAddress(7, 9, 1, 1);
        RegionUtil.setBorderBottom(BorderStyle.THICK, borderForObj, sh);
        RegionUtil.setBorderTop(BorderStyle.THICK, borderForObj, sh);
        RegionUtil.setBorderLeft(BorderStyle.THICK, borderForObj, sh);
        RegionUtil.setBorderRight(BorderStyle.THICK, borderForObj, sh);

        SXSSFCell cell_8_3 = row_8.createCell(2);
        cell_8_3.setCellStyle(tableHeaderStyle);
        cell_8_3.setCellValue("Адрес объекта");
        CellRangeAddress headerBranch = new CellRangeAddress(7, 9, 2, 2);
        sh.addMergedRegion(headerBranch);
        CellRangeAddress borderForBranch = new CellRangeAddress(7, 9, 2, 2);
        RegionUtil.setBorderBottom(BorderStyle.THICK, borderForBranch, sh);
        RegionUtil.setBorderTop(BorderStyle.THICK, borderForBranch, sh);
        RegionUtil.setBorderLeft(BorderStyle.THICK, borderForBranch, sh);
        RegionUtil.setBorderRight(BorderStyle.THICK, borderForBranch, sh);

        SXSSFCell cell_8_4 = row_8.createCell(3);
        cell_8_4.setCellStyle(tableHeaderStyle);
        cell_8_4.setCellValue("Дата и время входа в состояние");
        CellRangeAddress headerFacility = new CellRangeAddress(7, 9, 3, 3);
        sh.addMergedRegion(headerFacility);
        CellRangeAddress borderForFacility = new CellRangeAddress(7, 9, 3, 3);
        RegionUtil.setBorderBottom(BorderStyle.THICK, borderForFacility, sh);
        RegionUtil.setBorderTop(BorderStyle.THICK, borderForFacility, sh);
        RegionUtil.setBorderLeft(BorderStyle.THICK, borderForFacility, sh);
        RegionUtil.setBorderRight(BorderStyle.THICK, borderForFacility, sh);

        SXSSFCell cell_8_5 = row_8.createCell(4);
        cell_8_5.setCellStyle(tableHeaderStyle);
        cell_8_5.setCellValue("Тгмц");
        CellRangeAddress headerAddress = new CellRangeAddress(7, 9, 4, 4);
        sh.addMergedRegion(headerAddress);
        CellRangeAddress borderForAddress = new CellRangeAddress(7, 9, 4, 4);
        RegionUtil.setBorderBottom(BorderStyle.THICK, borderForAddress, sh);
        RegionUtil.setBorderTop(BorderStyle.THICK, borderForAddress, sh);
        RegionUtil.setBorderLeft(BorderStyle.THICK, borderForAddress, sh);
        RegionUtil.setBorderRight(BorderStyle.THICK, borderForAddress, sh);

        SXSSFCell cell_8_6 = row_8.createCell(5);
        cell_8_6.setCellStyle(tableHeaderStyle);
        cell_8_6.setCellValue("Время измерения Тнв");
        CellRangeAddress headerZone = new CellRangeAddress(7, 9, 5, 5);
        sh.addMergedRegion(headerZone);
        CellRangeAddress borderForZone = new CellRangeAddress(7, 9, 5, 5);
        RegionUtil.setBorderBottom(BorderStyle.THICK, borderForZone, sh);
        RegionUtil.setBorderTop(BorderStyle.THICK, borderForZone, sh);
        RegionUtil.setBorderLeft(BorderStyle.THICK, borderForZone, sh);
        RegionUtil.setBorderRight(BorderStyle.THICK, borderForZone, sh);

        SXSSFCell cell_8_7 = row_8.createCell(6);
        cell_8_7.setCellStyle(tableHeaderStyle);
        cell_8_7.setCellValue("Параметры теплосети");
        CellRangeAddress headerHeatNet = new CellRangeAddress(7, 7, 6, 13);
        sh.addMergedRegion(headerHeatNet);
        CellRangeAddress borderForHeatNet = new CellRangeAddress(7, 7, 6, 13);
        RegionUtil.setBorderBottom(BorderStyle.THICK, borderForHeatNet, sh);
        RegionUtil.setBorderTop(BorderStyle.THICK, borderForHeatNet, sh);
        RegionUtil.setBorderLeft(BorderStyle.THICK, borderForHeatNet, sh);
        RegionUtil.setBorderRight(BorderStyle.THICK, borderForHeatNet, sh);

        SXSSFCell cell_9_7 = row_9.createCell(6);
        cell_9_7.setCellStyle(tableHeaderStyle);
        cell_9_7.setCellValue("Т1");
        SXSSFCell cell_10_7 = row_10.createCell(6);
        cell_10_7.setCellStyle(tableHeaderStyle);
        cell_10_7.setCellValue("Прямая");

        SXSSFCell cell_9_8 = row_9.createCell(7);
        cell_9_8.setCellStyle(tableHeaderStyle);
        cell_9_8.setCellValue("Т1");
        SXSSFCell cell_10_8 = row_10.createCell(7);
        cell_10_8.setCellStyle(tableHeaderStyle);
        cell_10_8.setCellValue("Время измерения параметра");
        sh.setColumnHidden(7, true);

        SXSSFCell cell_9_9 = row_9.createCell(8);
        cell_9_9.setCellStyle(tableHeaderStyle);
        cell_9_9.setCellValue("Т2");
        SXSSFCell cell_10_9 = row_10.createCell(8);
        cell_10_9.setCellStyle(tableHeaderStyle);
        cell_10_9.setCellValue("Обратная");

        SXSSFCell cell_9_10 = row_9.createCell(9);
        cell_9_10.setCellStyle(tableHeaderStyle);
        cell_9_10.setCellValue("Т2");
        SXSSFCell cell_10_10 = row_10.createCell(9);
        cell_10_10.setCellStyle(tableHeaderStyle);
        cell_10_10.setCellValue("Время измерения параметра");
        sh.setColumnHidden(9, true);

        SXSSFCell cell_9_11 = row_9.createCell(10);
        cell_9_11.setCellStyle(tableHeaderStyle);
        cell_9_11.setCellValue("P1");
        SXSSFCell cell_10_11 = row_10.createCell(10);
        cell_10_11.setCellStyle(tableHeaderStyle);
        cell_10_11.setCellValue("Прямая");

        SXSSFCell cell_9_12 = row_9.createCell(11);
        cell_9_12.setCellStyle(tableHeaderStyle);
        cell_9_12.setCellValue("P1");
        SXSSFCell cell_10_12 = row_10.createCell(11);
        cell_10_12.setCellStyle(tableHeaderStyle);
        cell_10_12.setCellValue("Время измерения параметра");
        sh.setColumnHidden(11, true);

        SXSSFCell cell_9_13 = row_9.createCell(12);
        cell_9_13.setCellStyle(tableHeaderStyle);
        cell_9_13.setCellValue("P2");
        SXSSFCell cell_10_13 = row_10.createCell(12);
        cell_10_13.setCellStyle(tableHeaderStyle);
        cell_10_13.setCellValue("Обратная");

        SXSSFCell cell_9_14 = row_9.createCell(13);
        cell_9_14.setCellStyle(tableHeaderStyle);
        cell_9_14.setCellValue("P2");
        SXSSFCell cell_10_14 = row_10.createCell(13);
        cell_10_14.setCellStyle(tableHeaderStyle);
        cell_10_14.setCellValue("Время измерения параметра");
        sh.setColumnHidden(13, true);

        SXSSFCell cell_8_15 = row_8.createCell(14);
        cell_8_15.setCellStyle(tableHeaderStyle);
        cell_8_15.setCellValue("Параметры отопления");
        CellRangeAddress headerHeatParam = new CellRangeAddress(7, 7, 14, 21);
        sh.addMergedRegion(headerHeatParam);
        CellRangeAddress borderForHeatParam = new CellRangeAddress(7, 7, 14, 21);
        RegionUtil.setBorderBottom(BorderStyle.THICK, borderForHeatParam, sh);
        RegionUtil.setBorderTop(BorderStyle.THICK, borderForHeatParam, sh);
        RegionUtil.setBorderLeft(BorderStyle.THICK, borderForHeatParam, sh);
        RegionUtil.setBorderRight(BorderStyle.THICK, borderForHeatParam, sh);

        SXSSFCell cell_9_15 = row_9.createCell(14);
        cell_9_15.setCellStyle(tableHeaderStyle);
        cell_9_15.setCellValue("Т3");
        SXSSFCell cell_10_15 = row_10.createCell(14);
        cell_10_15.setCellStyle(tableHeaderStyle);
        cell_10_15.setCellValue("Прямая");

        SXSSFCell cell_9_16 = row_9.createCell(15);
        cell_9_16.setCellStyle(tableHeaderStyle);
        cell_9_16.setCellValue("Т3");
        SXSSFCell cell_10_16 = row_10.createCell(15);
        cell_10_16.setCellStyle(tableHeaderStyle);
        cell_10_16.setCellValue("Время измерения параметра");
        sh.setColumnHidden(15, true);

        SXSSFCell cell_9_17 = row_9.createCell(16);
        cell_9_17.setCellStyle(tableHeaderStyle);
        cell_9_17.setCellValue("Т4");
        SXSSFCell cell_10_17 = row_10.createCell(16);
        cell_10_17.setCellStyle(tableHeaderStyle);
        cell_10_17.setCellValue("Обратная");

        SXSSFCell cell_9_18 = row_9.createCell(17);
        cell_9_18.setCellStyle(tableHeaderStyle);
        cell_9_18.setCellValue("Т4");
        SXSSFCell cell_10_18 = row_10.createCell(17);
        cell_10_18.setCellStyle(tableHeaderStyle);
        cell_10_18.setCellValue("Время измерения параметра");
        sh.setColumnHidden(17, true);

        SXSSFCell cell_9_19 = row_9.createCell(18);
        cell_9_19.setCellStyle(tableHeaderStyle);
        cell_9_19.setCellValue("P3");
        SXSSFCell cell_10_19 = row_10.createCell(18);
        cell_10_19.setCellStyle(tableHeaderStyle);
        cell_10_19.setCellValue("Прямая");

        SXSSFCell cell_9_20 = row_9.createCell(19);
        cell_9_20.setCellStyle(tableHeaderStyle);
        cell_9_20.setCellValue("P3");
        SXSSFCell cell_10_20 = row_10.createCell(19);
        cell_10_20.setCellStyle(tableHeaderStyle);
        cell_10_20.setCellValue("Время измерения параметра");
        sh.setColumnHidden(19, true);

        SXSSFCell cell_9_21 = row_9.createCell(20);
        cell_9_21.setCellStyle(tableHeaderStyle);
        cell_9_21.setCellValue("P4");
        SXSSFCell cell_10_21 = row_10.createCell(20);
        cell_10_21.setCellStyle(tableHeaderStyle);
        cell_10_21.setCellValue("Обратная");

        SXSSFCell cell_9_22 = row_9.createCell(21);
        cell_9_22.setCellStyle(tableHeaderStyle);
        cell_9_22.setCellValue("P4");
        SXSSFCell cell_10_22 = row_10.createCell(21);
        cell_10_22.setCellStyle(tableHeaderStyle);
        cell_10_22.setCellValue("Время измерения параметра");
        sh.setColumnHidden(21, true);

        SXSSFCell cell_8_23 = row_8.createCell(22);
        cell_8_23.setCellStyle(tableHeaderStyle);
        cell_8_23.setCellValue("Параметры ГВС");
        CellRangeAddress headerGVSParam = new CellRangeAddress(7, 7, 22, 37);
        sh.addMergedRegion(headerGVSParam);
        CellRangeAddress borderForGVSParam = new CellRangeAddress(7, 7, 22, 37);
        RegionUtil.setBorderBottom(BorderStyle.THICK, borderForGVSParam, sh);
        RegionUtil.setBorderTop(BorderStyle.THICK, borderForGVSParam, sh);
        RegionUtil.setBorderLeft(BorderStyle.THICK, borderForGVSParam, sh);
        RegionUtil.setBorderRight(BorderStyle.THICK, borderForGVSParam, sh);

        SXSSFCell cell_9_23 = row_9.createCell(22);
        cell_9_23.setCellStyle(tableHeaderStyle);
        cell_9_23.setCellValue("Т7");
        SXSSFCell cell_10_23 = row_10.createCell(22);
        cell_10_23.setCellStyle(tableHeaderStyle);
        cell_10_23.setCellValue("после ВВП I зона");

        SXSSFCell cell_9_24 = row_9.createCell(23);
        cell_9_24.setCellStyle(tableHeaderStyle);
        cell_9_24.setCellValue("Т7");
        SXSSFCell cell_10_24 = row_10.createCell(23);
        cell_10_24.setCellStyle(tableHeaderStyle);
        cell_10_24.setCellValue("Время измерения параметра");
        sh.setColumnHidden(23, true);

        SXSSFCell cell_9_25 = row_9.createCell(24);
        cell_9_25.setCellStyle(tableHeaderStyle);
        cell_9_25.setCellValue("T7`");
        SXSSFCell cell_10_25 = row_10.createCell(24);
        cell_10_25.setCellStyle(tableHeaderStyle);
        cell_10_25.setCellValue("после ВВП II зона");

        SXSSFCell cell_9_26 = row_9.createCell(25);
        cell_9_26.setCellStyle(tableHeaderStyle);
        cell_9_26.setCellValue("T7`");
        SXSSFCell cell_10_26 = row_10.createCell(25);
        cell_10_26.setCellStyle(tableHeaderStyle);
        cell_10_26.setCellValue("Время измерения параметра");
        sh.setColumnHidden(25, true);

        SXSSFCell cell_9_27 = row_9.createCell(26);
        cell_9_27.setCellStyle(tableHeaderStyle);
        cell_9_27.setCellValue("P7");
        SXSSFCell cell_10_27 = row_10.createCell(26);
        cell_10_27.setCellStyle(tableHeaderStyle);
        cell_10_27.setCellValue("ГВС на I зону");

        SXSSFCell cell_9_28 = row_9.createCell(27);
        cell_9_28.setCellStyle(tableHeaderStyle);
        cell_9_28.setCellValue("P7");
        SXSSFCell cell_10_28 = row_10.createCell(27);
        cell_10_28.setCellStyle(tableHeaderStyle);
        cell_10_28.setCellValue("Время измерения параметра");
        sh.setColumnHidden(27, true);

        SXSSFCell cell_9_29 = row_9.createCell(28);
        cell_9_29.setCellStyle(tableHeaderStyle);
        cell_9_29.setCellValue("P7`");
        SXSSFCell cell_10_29 = row_10.createCell(28);
        cell_10_29.setCellStyle(tableHeaderStyle);
        cell_10_29.setCellValue("ГВС на II зону");

        SXSSFCell cell_9_30 = row_9.createCell(29);
        cell_9_30.setCellStyle(tableHeaderStyle);
        cell_9_30.setCellValue("P7`");
        SXSSFCell cell_10_30 = row_10.createCell(29);
        cell_10_30.setCellStyle(tableHeaderStyle);
        cell_10_30.setCellValue("Время измерения параметра");
        sh.setColumnHidden(29, true);

        SXSSFCell cell_9_31 = row_9.createCell(30);
        cell_9_31.setCellStyle(tableHeaderStyle);
        cell_9_31.setCellValue("Т13");
        SXSSFCell cell_10_31 = row_10.createCell(30);
        cell_10_31.setCellStyle(tableHeaderStyle);
        cell_10_31.setCellValue("В циркуляц. линии I зоны");

        SXSSFCell cell_9_32 = row_9.createCell(31);
        cell_9_32.setCellStyle(tableHeaderStyle);
        cell_9_32.setCellValue("Т13");
        SXSSFCell cell_10_32 = row_10.createCell(31);
        cell_10_32.setCellStyle(tableHeaderStyle);
        cell_10_32.setCellValue("Время измерения параметра");
        sh.setColumnHidden(31, true);

        SXSSFCell cell_9_33 = row_9.createCell(32);
        cell_9_33.setCellStyle(tableHeaderStyle);
        cell_9_33.setCellValue("T13`");
        SXSSFCell cell_10_33 = row_10.createCell(32);
        cell_10_33.setCellStyle(tableHeaderStyle);
        cell_10_33.setCellValue("В циркуляц. линии II зоны");

        SXSSFCell cell_9_34 = row_9.createCell(33);
        cell_9_34.setCellStyle(tableHeaderStyle);
        cell_9_34.setCellValue("T13`");
        SXSSFCell cell_10_34 = row_10.createCell(33);
        cell_10_34.setCellStyle(tableHeaderStyle);
        cell_10_34.setCellValue("Время измерения параметра");
        sh.setColumnHidden(33, true);

        SXSSFCell cell_9_35 = row_9.createCell(34);
        cell_9_35.setCellStyle(tableHeaderStyle);
        cell_9_35.setCellValue("P13");
        SXSSFCell cell_10_35 = row_10.createCell(34);
        cell_10_35.setCellStyle(tableHeaderStyle);
        cell_10_35.setCellValue("В циркуляц. линии I зоны");

        SXSSFCell cell_9_36 = row_9.createCell(35);
        cell_9_36.setCellStyle(tableHeaderStyle);
        cell_9_36.setCellValue("P13");
        SXSSFCell cell_10_36 = row_10.createCell(35);
        cell_10_36.setCellStyle(tableHeaderStyle);
        cell_10_36.setCellValue("Время измерения параметра");
        sh.setColumnHidden(35, true);

        SXSSFCell cell_9_37 = row_9.createCell(36);
        cell_9_37.setCellStyle(tableHeaderStyle);
        cell_9_37.setCellValue("P13`");
        SXSSFCell cell_10_37 = row_10.createCell(36);
        cell_10_37.setCellStyle(tableHeaderStyle);
        cell_10_37.setCellValue("В циркуляц. линии II зоны");

        SXSSFCell cell_9_38 = row_9.createCell(37);
        cell_9_38.setCellStyle(tableHeaderStyle);
        cell_9_38.setCellValue("P13`");
        SXSSFCell cell_10_38 = row_10.createCell(37);
        cell_10_38.setCellStyle(tableHeaderStyle);
        cell_10_38.setCellValue("Время измерения параметра");
        sh.setColumnHidden(37, true);

        SXSSFCell cell_8_39 = row_8.createCell(38);
        cell_8_39.setCellStyle(tableHeaderStyle);
        cell_8_39.setCellValue("Параметры ХВС");
        CellRangeAddress headerHVSParam = new CellRangeAddress(7, 7, 38, 43);
        sh.addMergedRegion(headerHVSParam);
        CellRangeAddress borderForHVSParam = new CellRangeAddress(7, 7, 38, 43);
        RegionUtil.setBorderBottom(BorderStyle.THICK, borderForHVSParam, sh);
        RegionUtil.setBorderTop(BorderStyle.THICK, borderForHVSParam, sh);
        RegionUtil.setBorderLeft(BorderStyle.THICK, borderForHVSParam, sh);
        RegionUtil.setBorderRight(BorderStyle.THICK, borderForHVSParam, sh);

        SXSSFCell cell_9_39 = row_9.createCell(38);
        cell_9_39.setCellStyle(tableHeaderStyle);
        cell_9_39.setCellValue("Pгор.хв");
        SXSSFCell cell_10_39 = row_10.createCell(38);
        cell_10_39.setCellStyle(tableHeaderStyle);
        cell_10_39.setCellValue("В горводопроводе");

        SXSSFCell cell_9_40 = row_9.createCell(39);
        cell_9_40.setCellStyle(tableHeaderStyle);
        cell_9_40.setCellValue("Pгор.хв");
        SXSSFCell cell_10_40 = row_10.createCell(39);
        cell_10_40.setCellStyle(tableHeaderStyle);
        cell_10_40.setCellValue("Время измерения параметра");
        sh.setColumnHidden(39, true);

        SXSSFCell cell_9_41 = row_9.createCell(40);
        cell_9_41.setCellStyle(tableHeaderStyle);
        cell_9_41.setCellValue("Pхв");
        SXSSFCell cell_10_41 = row_10.createCell(40);
        cell_10_41.setCellStyle(tableHeaderStyle);
        cell_10_41.setCellValue("На выходе из ЦТП на I зону");

        SXSSFCell cell_9_42 = row_9.createCell(41);
        cell_9_42.setCellStyle(tableHeaderStyle);
        cell_9_42.setCellValue("Pхв");
        SXSSFCell cell_10_42 = row_10.createCell(41);
        cell_10_42.setCellStyle(tableHeaderStyle);
        cell_10_42.setCellValue("Время измерения параметра");
        sh.setColumnHidden(41, true);

        SXSSFCell cell_9_43 = row_9.createCell(42);
        cell_9_43.setCellStyle(tableHeaderStyle);
        cell_9_43.setCellValue("Pхв`");
        SXSSFCell cell_10_43 = row_10.createCell(42);
        cell_10_43.setCellStyle(tableHeaderStyle);
        cell_10_43.setCellValue("На выходе из ЦТП на II зону");

        SXSSFCell cell_9_44 = row_9.createCell(43);
        cell_9_44.setCellStyle(tableHeaderStyle);
        cell_9_44.setCellValue("Pхв`");
        SXSSFCell cell_10_44 = row_10.createCell(43);
        cell_10_44.setCellStyle(tableHeaderStyle);
        cell_10_44.setCellValue("Время измерения параметра");
        sh.setColumnHidden(43, true);

        SXSSFCell cell_8_45 = row_8.createCell(44);
        cell_8_45.setCellStyle(tableHeaderStyle);
        cell_8_45.setCellValue("Показания счетчиков");
        CellRangeAddress headerMetData = new CellRangeAddress(7, 7, 44, 53);
        sh.addMergedRegion(headerMetData);
        CellRangeAddress borderForMetData = new CellRangeAddress(7, 7, 44, 53);
        RegionUtil.setBorderBottom(BorderStyle.THICK, borderForMetData, sh);
        RegionUtil.setBorderTop(BorderStyle.THICK, borderForMetData, sh);
        RegionUtil.setBorderLeft(BorderStyle.THICK, borderForMetData, sh);
        RegionUtil.setBorderRight(BorderStyle.THICK, borderForMetData, sh);

        SXSSFCell cell_9_45 = row_9.createCell(44);
        cell_9_45.setCellStyle(tableHeaderStyle);
        cell_9_45.setCellValue("Qтс");
        SXSSFCell cell_10_45 = row_10.createCell(44);
        cell_10_45.setCellStyle(tableHeaderStyle);
        cell_10_45.setCellValue("Расход тепла");

        SXSSFCell cell_9_46 = row_9.createCell(45);
        cell_9_46.setCellStyle(tableHeaderStyle);
        cell_9_46.setCellValue("Qтс");
        SXSSFCell cell_10_46 = row_10.createCell(45);
        cell_10_46.setCellStyle(tableHeaderStyle);
        cell_10_46.setCellValue("Время измерения параметра");
        sh.setColumnHidden(45, true);

        SXSSFCell cell_9_47 = row_9.createCell(46);
        cell_9_47.setCellStyle(tableHeaderStyle);
        cell_9_47.setCellValue("Gп");
        SXSSFCell cell_10_47 = row_10.createCell(46);
        cell_10_47.setCellStyle(tableHeaderStyle);
        cell_10_47.setCellValue("Водомер на подпитке");

        SXSSFCell cell_9_48 = row_9.createCell(47);
        cell_9_48.setCellStyle(tableHeaderStyle);
        cell_9_48.setCellValue("Gп");
        SXSSFCell cell_10_48 = row_10.createCell(47);
        cell_10_48.setCellStyle(tableHeaderStyle);
        cell_10_48.setCellValue("Время измерения параметра");
        sh.setColumnHidden(47, true);

        SXSSFCell cell_9_49 = row_9.createCell(48);
        cell_9_49.setCellStyle(tableHeaderStyle);
        cell_9_49.setCellValue("Расх. ЭЭ");
        SXSSFCell cell_10_49 = row_10.createCell(48);
        cell_10_49.setCellStyle(tableHeaderStyle);
        cell_10_49.setCellValue("Общий расход электроэнергии");

        SXSSFCell cell_9_50 = row_9.createCell(49);
        cell_9_50.setCellStyle(tableHeaderStyle);
        cell_9_50.setCellValue("Расх. ЭЭ");
        SXSSFCell cell_10_50 = row_10.createCell(49);
        cell_10_50.setCellStyle(tableHeaderStyle);
        cell_10_50.setCellValue("Время измерения параметра");
        sh.setColumnHidden(49, true);

        SXSSFCell cell_9_51 = row_9.createCell(50);
        cell_9_51.setCellStyle(tableHeaderStyle);
        cell_9_51.setCellValue("Расх. Эхвс");
        SXSSFCell cell_10_51 = row_10.createCell(50);
        cell_10_51.setCellStyle(tableHeaderStyle);
        cell_10_51.setCellValue("Расход электроэнергии на насосы ХВС");

        SXSSFCell cell_9_52 = row_9.createCell(51);
        cell_9_52.setCellStyle(tableHeaderStyle);
        cell_9_52.setCellValue("Расх. Эхвс");
        SXSSFCell cell_10_52 = row_10.createCell(51);
        cell_10_52.setCellStyle(tableHeaderStyle);
        cell_10_52.setCellValue("Время измерения параметра");
        sh.setColumnHidden(51, true);

        SXSSFCell cell_9_53 = row_9.createCell(52);
        cell_9_53.setCellStyle(tableHeaderStyle);
        cell_9_53.setCellValue("V8");
        SXSSFCell cell_10_53 = row_10.createCell(52);
        cell_10_53.setCellStyle(tableHeaderStyle);
        cell_10_53.setCellValue("Расход холодной воды на нужды ГВС");

        SXSSFCell cell_9_54 = row_9.createCell(53);
        cell_9_54.setCellStyle(tableHeaderStyle);
        cell_9_54.setCellValue("V8");
        SXSSFCell cell_10_54 = row_10.createCell(53);
        cell_10_54.setCellStyle(tableHeaderStyle);
        cell_10_54.setCellValue("Время измерения параметра");
        sh.setColumnHidden(53, true);

        SXSSFCell cell_8_55 = row_8.createCell(54);
        cell_8_55.setCellStyle(tableHeaderStyle);
        cell_8_55.setCellValue("Комментарий");
        CellRangeAddress headerComment = new CellRangeAddress(7, 9, 54, 54);
        sh.addMergedRegion(headerComment);
        CellRangeAddress borderForComment = new CellRangeAddress(7, 9, 54, 54);
        RegionUtil.setBorderBottom(BorderStyle.THICK, borderForComment, sh);
        RegionUtil.setBorderTop(BorderStyle.THICK, borderForComment, sh);
        RegionUtil.setBorderLeft(BorderStyle.THICK, borderForComment, sh);
        RegionUtil.setBorderRight(BorderStyle.THICK, borderForComment, sh);

        SXSSFCell cell_8_56 = row_8.createCell(55);
        cell_8_56.setCellStyle(tableHeaderStyle);
        cell_8_56.setCellValue("Имя прикрепленного файла");
        CellRangeAddress headerFileName = new CellRangeAddress(7, 9, 55, 55);
        sh.addMergedRegion(headerFileName);
        CellRangeAddress borderForFileName = new CellRangeAddress(7, 9, 55, 55);
        RegionUtil.setBorderBottom(BorderStyle.THICK, borderForFileName, sh);
        RegionUtil.setBorderTop(BorderStyle.THICK, borderForFileName, sh);
        RegionUtil.setBorderLeft(BorderStyle.THICK, borderForFileName, sh);
        RegionUtil.setBorderRight(BorderStyle.THICK, borderForFileName, sh);

        SXSSFCell cell_8_57 = row_8.createCell(56);
        cell_8_57.setCellStyle(tableHeaderStyle);
        cell_8_57.setCellValue("Прикрепленный файл");
        CellRangeAddress headerFile = new CellRangeAddress(7, 9, 56, 56);
        sh.addMergedRegion(headerFile);
        CellRangeAddress borderForFile = new CellRangeAddress(7, 9, 56, 56);
        RegionUtil.setBorderBottom(BorderStyle.THICK, borderForFile, sh);
        RegionUtil.setBorderTop(BorderStyle.THICK, borderForFile, sh);
        RegionUtil.setBorderLeft(BorderStyle.THICK, borderForFile, sh);
        RegionUtil.setBorderRight(BorderStyle.THICK, borderForFile, sh);
        sh.setColumnHidden(56, true);


        //с шапкой таблицы покончено, начинаем заполнять тело отчета
        List<Integer> objects = getUserObjects(user, dsR);

        HashMap <String, CellStyle> colors = new HashMap<>();
        int begRow = 10;
        int k = 1;
        if (!objects.isEmpty()) {
            for (Integer userObject : objects) {
                ObjectParams params = loadParams(user.getUserId(), userObject, timestamp, dsR);
                if (params.getCrashTime() != null) {
                    crash = "Y";
                }

                SXSSFRow row = sh.createRow(begRow);
                row.setHeight((short) 350);//todo тут высота если решат делать расширение

                SXSSFCell objNumCell = row.createCell(0);
                objNumCell.setCellValue(k);
                objNumCell.setCellStyle(cellNoBoldStyle);

                SXSSFCell objNameCell = row.createCell(1);
                if (params.getTnvColor() != null) {
                    String objName = params.getObjName() + " " + params.getTnvColor();
                    objNameCell.setCellValue(objName);
                } else {
                    objNameCell.setCellValue(params.getObjName());
                }
                objNameCell.setCellStyle(cellNoBoldStyle);

                SXSSFCell objAddr = row.createCell(2);
                objAddr.setCellValue(params.getObjAddress());
                objAddr.setCellStyle(cellNoBoldStyle);

                SXSSFCell crashTime = row.createCell(3);
                crashTime.setCellValue(params.getCrashTime());
                crashTime.setCellStyle(cellNoBoldStyle);

                SXSSFCell tnv = row.createCell(4);
                tnv.setCellValue(params.getTnv());
                tnv.setCellStyle(cellNoBoldStyle);


                SXSSFCell tnvTime = row.createCell(5);
                tnvTime.setCellValue(params.getTnvTime());
                tnvTime.setCellStyle(cellNoBoldStyle);

                SXSSFCell t1 = row.createCell(6);
                t1.setCellValue(params.getT1());
                if (params.getT1Color() != null) {
                    if (colors.containsKey(params.getT1Color())) {
                        t1.setCellStyle(colors.get(params.getT1Color()));
                    } else {
                        CellStyle cellColoredStyle = setCellNoBoldStyle(wb);
                        String rgbS = params.getT1Color();
                        byte [] rgbB = Hex.decodeHex(rgbS);
                        XSSFColor color = new XSSFColor(rgbB, null);
                        cellColoredStyle.setFillForegroundColor(color);
                        cellColoredStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                        colors.put(params.getT1Color(), cellColoredStyle);
                        t1.setCellStyle(cellColoredStyle);
                    }
                } else {
                    t1.setCellStyle(cellNoBoldStyle);
                }

                SXSSFCell t1Time = row.createCell(7);
                t1Time.setCellValue(params.getT1Time());
                t1Time.setCellStyle(cellNoBoldStyle);

                SXSSFCell t2 = row.createCell(8);
                t2.setCellValue(params.getT2());
                if (params.getT2Color() != null) {
                    if (colors.containsKey(params.getT2Color())) {
                        t2.setCellStyle(colors.get(params.getT2Color()));
                    } else {
                        CellStyle cellColoredStyle = setCellNoBoldStyle(wb);
                        String rgbS = params.getT2Color();
                        byte [] rgbB = Hex.decodeHex(rgbS);
                        XSSFColor color = new XSSFColor(rgbB, null);
                        cellColoredStyle.setFillForegroundColor(color);
                        cellColoredStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                        colors.put(params.getT2Color(), cellColoredStyle);
                        t2.setCellStyle(cellColoredStyle);
                    }

                } else {
                    t2.setCellStyle(cellNoBoldStyle);
                }

                SXSSFCell t2Time = row.createCell(9);
                t2Time.setCellValue(params.getT2Time());
                t2Time.setCellStyle(cellNoBoldStyle);

                SXSSFCell p1 = row.createCell(10);
                p1.setCellValue(params.getP1());
                if (params.getP1Color() != null) {
                    if (colors.containsKey(params.getP1Color())) {
                        p1.setCellStyle(colors.get(params.getP1Color()));
                    } else {
                        CellStyle cellColoredStyle = setCellNoBoldStyle(wb);
                        String rgbS = params.getP1Color();
                        byte [] rgbB = Hex.decodeHex(rgbS);
                        XSSFColor color = new XSSFColor(rgbB, null);
                        cellColoredStyle.setFillForegroundColor(color);
                        cellColoredStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                        colors.put(params.getP1Color(), cellColoredStyle);
                        p1.setCellStyle(cellColoredStyle);
                    }
                } else {
                    p1.setCellStyle(cellNoBoldStyle);
                }

                SXSSFCell p1Time = row.createCell(11);
                p1Time.setCellValue(params.getP1Time());
                p1Time.setCellStyle(cellNoBoldStyle);

                SXSSFCell p2 = row.createCell(12);
                p2.setCellValue(params.getP2());
                if (params.getP2Color() != null) {
                    if (colors.containsKey(params.getP2Color())) {
                        p2.setCellStyle(colors.get(params.getP2Color()));
                    } else {
                        CellStyle cellColoredStyle = setCellNoBoldStyle(wb);
                        String rgbS = params.getP2Color();
                        byte [] rgbB = Hex.decodeHex(rgbS);
                        XSSFColor color = new XSSFColor(rgbB, null);
                        cellColoredStyle.setFillForegroundColor(color);
                        cellColoredStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                        colors.put(params.getP2Color(), cellColoredStyle);
                        p2.setCellStyle(cellColoredStyle);
                    }
                } else {
                    p2.setCellStyle(cellNoBoldStyle);
                }

                SXSSFCell p2Time = row.createCell(13);
                p2Time.setCellValue(params.getP2Time());
                p2Time.setCellStyle(cellNoBoldStyle);

                SXSSFCell t3 = row.createCell(14);
                t3.setCellValue(params.getT3());
                if (params.getT3Color() != null) {
                    if (colors.containsKey(params.getT3Color())) {
                        t3.setCellStyle(colors.get(params.getT3Color()));
                    } else {
                        CellStyle cellColoredStyle = setCellNoBoldStyle(wb);
                        String rgbS = params.getT3Color();
                        byte [] rgbB = Hex.decodeHex(rgbS);
                        XSSFColor color = new XSSFColor(rgbB, null);
                        cellColoredStyle.setFillForegroundColor(color);
                        cellColoredStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                        colors.put(params.getT3Color(), cellColoredStyle);
                        t3.setCellStyle(cellColoredStyle);
                    }

                } else {
                    t3.setCellStyle(cellNoBoldStyle);
                }

                SXSSFCell t3Time = row.createCell(15);
                t3Time.setCellValue(params.getT3Time());
                t3Time.setCellStyle(cellNoBoldStyle);

                SXSSFCell t4 = row.createCell(16);
                t4.setCellValue(params.getT4());
                if (params.getT4Color() != null) {
                    if (colors.containsKey(params.getT4Color())) {
                        t4.setCellStyle(colors.get(params.getT4Color()));
                    } else {
                        CellStyle cellColoredStyle = setCellNoBoldStyle(wb);
                        String rgbS = params.getT4Color();
                        byte [] rgbB = Hex.decodeHex(rgbS);
                        XSSFColor color = new XSSFColor(rgbB, null);
                        cellColoredStyle.setFillForegroundColor(color);
                        cellColoredStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                        colors.put(params.getT4Color(), cellColoredStyle);
                        t4.setCellStyle(cellColoredStyle);
                    }
                } else {
                    t4.setCellStyle(cellNoBoldStyle);
                }

                SXSSFCell t4Time = row.createCell(17);
                t4Time.setCellValue(params.getT4Time());
                t4Time.setCellStyle(cellNoBoldStyle);

                SXSSFCell p3 = row.createCell(18);
                p3.setCellValue(params.getP3());
                if (params.getP3Color() != null) {
                    if (colors.containsKey(params.getP3Color())) {
                        p3.setCellStyle(colors.get(params.getP3Color()));
                    } else {
                        CellStyle cellColoredStyle = setCellNoBoldStyle(wb);
                        String rgbS = params.getP3Color();
                        byte [] rgbB = Hex.decodeHex(rgbS);
                        XSSFColor color = new XSSFColor(rgbB, null);
                        cellColoredStyle.setFillForegroundColor(color);
                        cellColoredStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                        colors.put(params.getP3Color(), cellColoredStyle);
                        p3.setCellStyle(cellColoredStyle);
                    }
                } else {
                    p3.setCellStyle(cellNoBoldStyle);
                }

                SXSSFCell p3Time = row.createCell(19);
                p3Time.setCellValue(params.getP3Time());
                p3Time.setCellStyle(cellNoBoldStyle);

                SXSSFCell p4 = row.createCell(20);
                p4.setCellValue(params.getP4());
                if (params.getP4Color() != null) {
                    if (colors.containsKey(params.getP4Color())) {
                        p4.setCellStyle(colors.get(params.getP4Color()));
                    } else {
                        CellStyle cellColoredStyle = setCellNoBoldStyle(wb);
                        String rgbS = params.getP4Color();
                        byte [] rgbB = Hex.decodeHex(rgbS);
                        XSSFColor color = new XSSFColor(rgbB, null);
                        cellColoredStyle.setFillForegroundColor(color);
                        cellColoredStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                        colors.put(params.getP4Color(), cellColoredStyle);
                        p4.setCellStyle(cellColoredStyle);
                    }
                } else {
                    p4.setCellStyle(cellNoBoldStyle);
                }

                SXSSFCell p4Time = row.createCell(21);
                p4Time.setCellValue(params.getP4Time());
                p4Time.setCellStyle(cellNoBoldStyle);

                SXSSFCell t7 = row.createCell(22);
                t7.setCellValue(params.getT7());
                if (params.getT7Color() != null) {
                    if (colors.containsKey(params.getT7Color())) {
                        t7.setCellStyle(colors.get(params.getT7Color()));
                    } else {
                        CellStyle cellColoredStyle = setCellNoBoldStyle(wb);
                        String rgbS = params.getT7Color();
                        byte [] rgbB = Hex.decodeHex(rgbS);
                        XSSFColor color = new XSSFColor(rgbB, null);
                        cellColoredStyle.setFillForegroundColor(color);
                        cellColoredStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                        colors.put(params.getT7Color(), cellColoredStyle);
                        t7.setCellStyle(cellColoredStyle);
                    }
                } else {
                    t7.setCellStyle(cellNoBoldStyle);
                }

                SXSSFCell t7Time = row.createCell(23);
                t7Time.setCellValue(params.getT7Time());
                t7Time.setCellStyle(cellNoBoldStyle);

                SXSSFCell t7_2 = row.createCell(24);
                t7_2.setCellValue(params.getT7_2());
                if (params.getT7_2Color() != null) {
                    if (colors.containsKey(params.getT7_2Color())) {
                        t7_2.setCellStyle(colors.get(params.getT7_2Color()));
                    } else {
                        CellStyle cellColoredStyle = setCellNoBoldStyle(wb);
                        String rgbS = params.getT7_2Color();
                        byte [] rgbB = Hex.decodeHex(rgbS);
                        XSSFColor color = new XSSFColor(rgbB, null);
                        cellColoredStyle.setFillForegroundColor(color);
                        cellColoredStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                        colors.put(params.getT7_2Color(), cellColoredStyle);
                        t7_2.setCellStyle(cellColoredStyle);
                    }
                } else {
                    t7_2.setCellStyle(cellNoBoldStyle);
                }

                SXSSFCell t7_2Time = row.createCell(25);
                t7_2Time.setCellValue(params.getT7_2Time());
                t7_2Time.setCellStyle(cellNoBoldStyle);

                SXSSFCell p7 = row.createCell(26);
                p7.setCellValue(params.getP7());
                if (params.getP7Color() != null) {
                    if (colors.containsKey(params.getP7Color())) {
                        p7.setCellStyle(colors.get(params.getP7Color()));
                    } else {
                        CellStyle cellColoredStyle = setCellNoBoldStyle(wb);
                        String rgbS = params.getP7Color();
                        byte [] rgbB = Hex.decodeHex(rgbS);
                        XSSFColor color = new XSSFColor(rgbB, null);
                        cellColoredStyle.setFillForegroundColor(color);
                        cellColoredStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                        colors.put(params.getP7Color(), cellColoredStyle);
                        p7.setCellStyle(cellColoredStyle);
                    }
                } else {
                    p7.setCellStyle(cellNoBoldStyle);
                }

                SXSSFCell p7Time = row.createCell(27);
                p7Time.setCellValue(params.getP7Time());
                p7Time.setCellStyle(cellNoBoldStyle);

                SXSSFCell p7_2 = row.createCell(28);
                p7_2.setCellValue(params.getP7_2());
                if (params.getP7_2Color() != null) {
                    if (colors.containsKey(params.getP7_2Color())) {
                        p7_2.setCellStyle(colors.get(params.getP7_2Color()));
                    } else {
                        CellStyle cellColoredStyle = setCellNoBoldStyle(wb);
                        String rgbS = params.getP7_2Color();
                        byte [] rgbB = Hex.decodeHex(rgbS);
                        XSSFColor color = new XSSFColor(rgbB, null);
                        cellColoredStyle.setFillForegroundColor(color);
                        cellColoredStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                        colors.put(params.getP7_2Color(), cellColoredStyle);
                        p7_2.setCellStyle(cellColoredStyle);
                    }
                } else {
                    p7_2.setCellStyle(cellNoBoldStyle);
                }

                SXSSFCell p7_2Time = row.createCell(29);
                p7_2Time.setCellValue(params.getP7_2Time());
                p7_2Time.setCellStyle(cellNoBoldStyle);

                SXSSFCell t13 = row.createCell(30);
                t13.setCellValue(params.getT13());
                if (params.getT13Color() != null) {
                    if (colors.containsKey(params.getT13Color())) {
                        t13.setCellStyle(colors.get(params.getT13Color()));
                    } else {
                        CellStyle cellColoredStyle = setCellNoBoldStyle(wb);
                        String rgbS = params.getT13Color();
                        byte [] rgbB = Hex.decodeHex(rgbS);
                        XSSFColor color = new XSSFColor(rgbB, null);
                        cellColoredStyle.setFillForegroundColor(color);
                        cellColoredStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                        colors.put(params.getT13Color(), cellColoredStyle);
                        t13.setCellStyle(cellColoredStyle);
                    }
                } else {
                    t13.setCellStyle(cellNoBoldStyle);
                }

                SXSSFCell t13Time = row.createCell(31);
                t13Time.setCellValue(params.getT13Time());
                t13Time.setCellStyle(cellNoBoldStyle);

                SXSSFCell t13_2 = row.createCell(32);
                t13_2.setCellValue(params.getT13_2());
                if (params.getT13_2Color() != null) {
                    if (colors.containsKey(params.getT13_2Color())) {
                        t13_2.setCellStyle(colors.get(params.getT13_2Color()));
                    } else {
                        CellStyle cellColoredStyle = setCellNoBoldStyle(wb);
                        String rgbS = params.getT13_2Color();
                        byte [] rgbB = Hex.decodeHex(rgbS);
                        XSSFColor color = new XSSFColor(rgbB, null);
                        cellColoredStyle.setFillForegroundColor(color);
                        cellColoredStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                        colors.put(params.getT13_2Color(), cellColoredStyle);
                        t13_2.setCellStyle(cellColoredStyle);
                    }
                } else {
                    t13_2.setCellStyle(cellNoBoldStyle);
                }

                SXSSFCell t13_2Time = row.createCell(33);
                t13_2Time.setCellValue(params.getT13_2Time());
                t13_2Time.setCellStyle(cellNoBoldStyle);

                SXSSFCell p13 = row.createCell(34);
                p13.setCellValue(params.getP13());
                if (params.getP13Color() != null) {
                    if (colors.containsKey(params.getP13Color())) {
                        p13.setCellStyle(colors.get(params.getP13Color()));
                    } else {
                        CellStyle cellColoredStyle = setCellNoBoldStyle(wb);
                        String rgbS = params.getP13Color();
                        byte [] rgbB = Hex.decodeHex(rgbS);
                        XSSFColor color = new XSSFColor(rgbB, null);
                        cellColoredStyle.setFillForegroundColor(color);
                        cellColoredStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                        colors.put(params.getP13Color(), cellColoredStyle);
                        p13.setCellStyle(cellColoredStyle);
                    }
                } else {
                    p13.setCellStyle(cellNoBoldStyle);
                }

                SXSSFCell p13Time = row.createCell(35);
                p13Time.setCellValue(params.getP13Time());
                p13Time.setCellStyle(cellNoBoldStyle);

                SXSSFCell p13_2 = row.createCell(36);
                p13_2.setCellValue(params.getP13_2());
                if (params.getP13_2Color() != null) {
                    if (colors.containsKey(params.getP13_2Color())) {
                        p13_2.setCellStyle(colors.get(params.getP13_2Color()));
                    } else {
                        CellStyle cellColoredStyle = setCellNoBoldStyle(wb);
                        String rgbS = params.getP13_2Color();
                        byte [] rgbB = Hex.decodeHex(rgbS);
                        XSSFColor color = new XSSFColor(rgbB, null);
                        cellColoredStyle.setFillForegroundColor(color);
                        cellColoredStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                        colors.put(params.getP13_2Color(), cellColoredStyle);
                        p13_2.setCellStyle(cellColoredStyle);
                    }
                } else {
                    p13_2.setCellStyle(cellNoBoldStyle);
                }

                SXSSFCell p13_2Time = row.createCell(37);
                p13_2Time.setCellValue(params.getP13_2Time());
                p13_2Time.setCellStyle(cellNoBoldStyle);

                SXSSFCell pg = row.createCell(38);
                pg.setCellValue(params.getPg());
                if (params.getPgColor() != null) {
                    if (colors.containsKey(params.getPgColor())) {
                        pg.setCellStyle(colors.get(params.getPgColor()));
                    } else {
                        CellStyle cellColoredStyle = setCellNoBoldStyle(wb);
                        String rgbS = params.getPgColor();
                        byte [] rgbB = Hex.decodeHex(rgbS);
                        XSSFColor color = new XSSFColor(rgbB, null);
                        cellColoredStyle.setFillForegroundColor(color);
                        cellColoredStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                        colors.put(params.getPgColor(), cellColoredStyle);
                        pg.setCellStyle(cellColoredStyle);
                    }
                } else {
                    pg.setCellStyle(cellNoBoldStyle);
                }

                SXSSFCell pgTime = row.createCell(39);
                pgTime.setCellValue(params.getPgTime());
                pgTime.setCellStyle(cellNoBoldStyle);

                SXSSFCell ph = row.createCell(40);
                ph.setCellValue(params.getPh());
                if (params.getPhColor() != null) {
                    if (colors.containsKey(params.getPhColor())) {
                        ph.setCellStyle(colors.get(params.getPhColor()));
                    } else {
                        CellStyle cellColoredStyle = setCellNoBoldStyle(wb);
                        String rgbS = params.getPhColor();
                        byte [] rgbB = Hex.decodeHex(rgbS);
                        XSSFColor color = new XSSFColor(rgbB, null);
                        cellColoredStyle.setFillForegroundColor(color);
                        cellColoredStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                        colors.put(params.getPhColor(), cellColoredStyle);
                        ph.setCellStyle(cellColoredStyle);
                    }
                } else {
                    ph.setCellStyle(cellNoBoldStyle);
                }

                SXSSFCell phTime = row.createCell(41);
                phTime.setCellValue(params.getPhTime());
                phTime.setCellStyle(cellNoBoldStyle);

                SXSSFCell ph_2 = row.createCell(42);
                ph_2.setCellValue(params.getPh_2());
                if (params.getPh_2Color() != null) {
                    if (colors.containsKey(params.getPh_2Color())) {
                        ph_2.setCellStyle(colors.get(params.getPh_2Color()));
                    } else {
                        CellStyle cellColoredStyle = setCellNoBoldStyle(wb);
                        String rgbS = params.getPh_2Color();
                        byte [] rgbB = Hex.decodeHex(rgbS);
                        XSSFColor color = new XSSFColor(rgbB, null);
                        cellColoredStyle.setFillForegroundColor(color);
                        cellColoredStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                        colors.put(params.getPh_2Color(), cellColoredStyle);
                        ph_2.setCellStyle(cellColoredStyle);
                    }
                } else {
                    ph_2.setCellStyle(cellNoBoldStyle);
                }

                SXSSFCell ph_2Time = row.createCell(43);
                ph_2Time.setCellValue(params.getPh_2Time());
                ph_2Time.setCellStyle(cellNoBoldStyle);

                SXSSFCell qts = row.createCell(44);
                qts.setCellValue(params.getQts());
                if (params.getQtsColor() != null) {
                    if (colors.containsKey(params.getQtsColor())) {
                        qts.setCellStyle(colors.get(params.getQtsColor()));
                    } else {
                        CellStyle cellColoredStyle = setCellNoBoldStyle(wb);
                        String rgbS = params.getQtsColor();
                        byte [] rgbB = Hex.decodeHex(rgbS);
                        XSSFColor color = new XSSFColor(rgbB, null);
                        cellColoredStyle.setFillForegroundColor(color);
                        cellColoredStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                        colors.put(params.getQtsColor(), cellColoredStyle);
                        qts.setCellStyle(cellColoredStyle);
                    }
                } else {
                    qts.setCellStyle(cellNoBoldStyle);
                }

                SXSSFCell qtsTime = row.createCell(45);
                qtsTime.setCellValue(params.getQtsTime());
                qtsTime.setCellStyle(cellNoBoldStyle);

                SXSSFCell gp = row.createCell(46);
                gp.setCellValue(params.getGp());
                if (params.getGpColor() != null) {
                    if (colors.containsKey(params.getGpColor())) {
                        gp.setCellStyle(colors.get(params.getGpColor()));
                    } else {
                        CellStyle cellColoredStyle = setCellNoBoldStyle(wb);
                        String rgbS = params.getGpColor();
                        byte [] rgbB = Hex.decodeHex(rgbS);
                        XSSFColor color = new XSSFColor(rgbB, null);
                        cellColoredStyle.setFillForegroundColor(color);
                        cellColoredStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                        colors.put(params.getGpColor(), cellColoredStyle);
                        gp.setCellStyle(cellColoredStyle);
                    }
                } else {
                    gp.setCellStyle(cellNoBoldStyle);
                }

                SXSSFCell gpTime = row.createCell(47);
                gpTime.setCellValue(params.getGpTime());
                gpTime.setCellStyle(cellNoBoldStyle);

                SXSSFCell gee = row.createCell(48);
                gee.setCellValue(params.getGee());
                if (params.getGeeColor() != null) {
                    if (colors.containsKey(params.getGeeColor())) {
                        gee.setCellStyle(colors.get(params.getGeeColor()));
                    } else {
                        CellStyle cellColoredStyle = setCellNoBoldStyle(wb);
                        String rgbS = params.getGeeColor();
                        byte [] rgbB = Hex.decodeHex(rgbS);
                        XSSFColor color = new XSSFColor(rgbB, null);
                        cellColoredStyle.setFillForegroundColor(color);
                        cellColoredStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                        colors.put(params.getGeeColor(), cellColoredStyle);
                        gee.setCellStyle(cellColoredStyle);
                    }
                } else {
                    gee.setCellStyle(cellNoBoldStyle);
                }

                SXSSFCell geeTime = row.createCell(49);
                geeTime.setCellValue(params.getGeeTime());
                geeTime.setCellStyle(cellNoBoldStyle);

                SXSSFCell geh = row.createCell(50);
                geh.setCellValue(params.getGeh());
                if (params.getGehColor() != null) {
                    if (colors.containsKey(params.getGehColor())) {
                        geh.setCellStyle(colors.get(params.getGehColor()));
                    } else {
                        CellStyle cellColoredStyle = setCellNoBoldStyle(wb);
                        String rgbS = params.getGehColor();
                        byte [] rgbB = Hex.decodeHex(rgbS);
                        XSSFColor color = new XSSFColor(rgbB, null);
                        cellColoredStyle.setFillForegroundColor(color);
                        cellColoredStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                        colors.put(params.getGehColor(), cellColoredStyle);
                        geh.setCellStyle(cellColoredStyle);
                    }
                } else {
                    geh.setCellStyle(cellNoBoldStyle);
                }

                SXSSFCell gehTime = row.createCell(51);
                gehTime.setCellValue(params.getGehTime());
                gehTime.setCellStyle(cellNoBoldStyle);

                SXSSFCell v8 = row.createCell(52);
                v8.setCellValue(params.getV8());
                if (params.getV8Color() != null) {
                    if (colors.containsKey(params.getV8Color())) {
                        v8.setCellStyle(colors.get(params.getV8Color()));
                    } else {
                        CellStyle cellColoredStyle = setCellNoBoldStyle(wb);
                        String rgbS = params.getV8Color();
                        byte [] rgbB = Hex.decodeHex(rgbS);
                        XSSFColor color = new XSSFColor(rgbB, null);
                        cellColoredStyle.setFillForegroundColor(color);
                        cellColoredStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                        colors.put(params.getV8Color(), cellColoredStyle);
                        v8.setCellStyle(cellColoredStyle);
                    }
                } else {
                    v8.setCellStyle(cellNoBoldStyle);
                }

                SXSSFCell v8Time = row.createCell(53);
                v8Time.setCellValue(params.getV8Time());
                v8Time.setCellStyle(cellNoBoldStyle);

                SXSSFCell comment = row.createCell(54);
                comment.setCellValue(params.getComment());
                comment.setCellStyle(cellNoBoldStyle);

                SXSSFCell fileName = row.createCell(55);
                fileName.setCellValue(params.getFileName());
                fileName.setCellStyle(cellNoBoldStyle);

                SXSSFCell file = row.createCell(56);
                file.setCellValue(params.getFile());
                file.setCellStyle(cellNoBoldStyle);

                begRow++;
                k++;
            }
        } else {
            SXSSFRow row = sh.createRow(begRow);
            row.setHeight((short) 350);//todo тут высота если решат делать расширение

            SXSSFCell objNumCell = row.createCell(1);
            objNumCell.setCellValue("Отсутствуют объекты, закрепленные за пользоавтелем");

            CellRangeAddress merge = new CellRangeAddress(begRow, begRow, 1, 2);
            sh.addMergedRegion(merge);
        }

        return new WbForSave(wb, crash);
    }

    //получаем список пользователей
    public static List<User> getUsers(DataSource dsR) {
        List <User> result = new LinkedList<>();
        try (Connection connect = dsR.getConnection();
             PreparedStatement stm = connect.prepareStatement(GET_USERS)) {
            ResultSet res = stm.executeQuery();
            while (res.next()) {
                result.add(new User(res.getString("user_id"), res.getString("user_name"),  "User"));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "error load Users", e);
        }

        return result;
    }

    //получаем список предприятий
    public static List<User> getStruct(DataSource dsR) {
        List <User> result = new LinkedList<>();
        try (Connection connect = dsR.getConnection();
             PreparedStatement stm = connect.prepareStatement(GET_STRUCT)) {
            ResultSet res = stm.executeQuery();
            while (res.next()) {
                result.add(new User(res.getString("struct_id"), res.getString("struct_name"), "Struct"));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "error load Users", e);
        }

        return result;
    }

    // получаем id объектов для конкретного пользователя или предприятия

    public List<Integer> getUserObjects (User user, DataSource dsR) {
        List <Integer> result = new ArrayList<>();
        try (Connection connect = dsR.getConnection();
             PreparedStatement stm = connect.prepareStatement(user.getTag().equals("User") ? GET_OBJECTS : GET_STRUCT_OBJECTS)) {

            if (user.getTag().equals("User")) {
                stm.setString(1, user.getUserId());
            } else {
                stm.setInt(1, Integer.parseInt(user.getUserId()));
            }
            ResultSet res = stm.executeQuery();
            while (res.next()) {
                result.add(res.getInt("obj_id"));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "error load Objects id", e);
        }

        return result;
    }

    // получаем перечень параметров для объекта
    public ObjectParams loadParams(String user, int objId, LocalDateTime timestamp, DataSource ds) {
        ObjectParams result = new ObjectParams();
        try (Connection connect = ds.getConnection();
             PreparedStatement stm = connect.prepareStatement(GET_PARAMS)) {
            stm.setString(1, user);
            stm.setInt(2, objId);
            stm.setTimestamp(3, Timestamp.valueOf(timestamp));
            ResultSet res = stm.executeQuery();
            if (res.next()) {
                result.setObjName(res.getString("obj_name"));
                result.setObjAddress(res.getString("obj_address"));
                result.setCrashTime(res.getString("crash_time"));
                result.setTnv(res.getString("tnv_value"));
                result.setTnvColor(res.getString("tnv_color"));
                result.setTnvTime(res.getString("tnv_timestamp"));
                result.setT1(res.getString("t1_value"));
                result.setT1Color(res.getString("t1_color"));
                result.setT1Time(res.getString("t1_timestamp"));
                result.setT2(res.getString("t2_value"));
                result.setT2Color(res.getString("t2_color"));
                result.setT2Time(res.getString("t2_timestamp"));
                result.setP1(res.getString("p1_value"));
                result.setP1Color(res.getString("p1_color"));
                result.setP1Time(res.getString("p1_timestamp"));
                result.setP2(res.getString("p2_value"));
                result.setP2Color(res.getString("p2_color"));
                result.setP2Time(res.getString("p2_timestamp"));
                result.setT3(res.getString("t3_value"));
                result.setT3Color(res.getString("t3_color"));
                result.setT3Time(res.getString("t3_timestamp"));
                result.setT4(res.getString("t4_value"));
                result.setT4Color(res.getString("t4_color"));
                result.setT4Time(res.getString("t4_timestamp"));
                result.setP3(res.getString("p3_value"));
                result.setP3Color(res.getString("p3_color"));
                result.setP3Time(res.getString("p3_timestamp"));
                result.setP4(res.getString("p4_value"));
                result.setP4Color(res.getString("p4_color"));
                result.setP4Time(res.getString("p4_timestamp"));
                result.setT7(res.getString("t7_value"));
                result.setT7Color(res.getString("t7_color"));
                result.setT7Time(res.getString("t7_timestamp"));
                result.setT7_2(res.getString("t7_2_value"));
                result.setT7_2Color(res.getString("t7_2_color"));
                result.setT7_2Time(res.getString("t7_2_timestamp"));
                result.setP7(res.getString("p7_value"));
                result.setP7Color(res.getString("p7_color"));
                result.setP7Time(res.getString("p7_timestamp"));
                result.setP7_2(res.getString("p7_2_value"));
                result.setP7_2Color(res.getString("p7_2_color"));
                result.setP7_2Time(res.getString("p7_2_timestamp"));
                result.setT13(res.getString("t13_value"));
                result.setT13Color(res.getString("t13_color"));
                result.setT13Time(res.getString("t13_timestamp"));
                result.setT13_2(res.getString("t13_2_value"));
                result.setT13_2Color(res.getString("t13_2_color"));
                result.setT13_2Time(res.getString("t13_2_timestamp"));
                result.setP13(res.getString("p13_value"));
                result.setP13Color(res.getString("p13_color"));
                result.setP13Time(res.getString("p13_timestamp"));
                result.setP13_2(res.getString("p13_2_value"));
                result.setP13_2Color(res.getString("p13_2_color"));
                result.setP13_2Time(res.getString("p13_2_timestamp"));
                result.setPg(res.getString("pg_value"));
                result.setPgColor(res.getString("pg_color"));
                result.setPgTime(res.getString("pg_timestamp"));
                result.setPh(res.getString("ph_value"));
                result.setPhColor(res.getString("ph_color"));
                result.setPhTime(res.getString("ph_timestamp"));
                result.setPh_2(res.getString("ph_2_value"));
                result.setPh_2Color(res.getString("ph_2_color"));
                result.setPh_2Time(res.getString("ph_2_timestamp"));
                result.setQts(res.getString("qts_value"));
                result.setQtsColor(res.getString("qts_color"));
                result.setQtsTime(res.getString("qts_timestamp"));
                result.setGp(res.getString("gp_value"));
                result.setGpColor(res.getString("gp_color"));
                result.setGpTime(res.getString("gp_timestamp"));
                result.setGee(res.getString("gee_value"));
                result.setGeeColor(res.getString("gee_color"));
                result.setGeeTime(res.getString("gee_timestamp"));
                result.setGeh(res.getString("geh_value"));
                result.setGehColor(res.getString("geh_color"));
                result.setGehTime(res.getString("geh_timestamp"));
                result.setV8(res.getString("v8_value"));
                result.setV8Color(res.getString("v8_color"));
                result.setV8Time(res.getString("v8_timestamp"));
                result.setComment(res.getString("comment_"));
                result.setFileName(res.getString("file_name"));
                result.setFile(res.getString("file_link"));

                return result;
            }

        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "error load params", e);
        }
        return result;
    }

    //добавляеь блоб в таблицу
    public void saveReportIntoTable(WbForSave wb, User user, LocalDateTime timestamp, DataSource ds) {
        try (Connection connection = ds.getConnection();
             PreparedStatement stm = connection.prepareStatement(user.getTag().equals("User") ? SAVE_BLOB_USER : SAVE_BLOB_STRUCT)) {

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            try {
                wb.getW().write(bos);
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "saving blob error ", e);
            }

            stm.setTimestamp(1, Timestamp.valueOf(timestamp));
            stm.setBytes(2, bos.toByteArray());
            if (user.getTag().equals("User")) {
                stm.setString(3, user.getUserId());
            } else {
                stm.setInt(3, Integer.parseInt(user.getUserId()));
            }
            stm.setString(4, wb.getCrash());

            stm.executeQuery();
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "add blob error ", e);
        }
    }


    ///////////////////////////////////////////  Определение стилей тут

    //  Стиль заголовка жирный
    private CellStyle setHeaderStyle(SXSSFWorkbook p_wb) {

        CellStyle style = p_wb.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setWrapText(false);

        Font headerFont = p_wb.createFont();
        headerFont.setBold(true);
        headerFont.setFontName("Times New Roman");
        headerFont.setFontHeightInPoints((short) 16);

        style.setFont(headerFont);

        return style;
    }

    //  Стиль заголовка не жирный
    private  CellStyle setHeaderStyleNoBold(SXSSFWorkbook p_wb) {

        CellStyle style = p_wb.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setWrapText(false);

        Font headerFontNoBold = p_wb.createFont();
        headerFontNoBold.setBold(false);
        headerFontNoBold.setFontName("Times New Roman");
        headerFontNoBold.setFontHeightInPoints((short) 16);

        style.setFont(headerFontNoBold);

        return style;
    }

    //стиль для даты создания отчета
    private  CellStyle setCellNow(SXSSFWorkbook wb) {
        CellStyle style = wb.createCellStyle();
        style.setAlignment(HorizontalAlignment.LEFT);


        Font nowFont = wb.createFont();
        nowFont.setBold(false);
        nowFont.setFontName("Times New Roman");
        nowFont.setFontHeightInPoints((short) 12);

        style.setFont(nowFont);

        return style;
    }

    //  Стиль шапки таблицы
    private  CellStyle setTableHeaderStyle(SXSSFWorkbook p_wb) {
        CellStyle style = p_wb.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setWrapText(true);

        style.setBorderTop(BorderStyle.THICK);
        style.setBorderLeft(BorderStyle.THICK);
        style.setBorderRight(BorderStyle.THICK);
        style.setBorderBottom(BorderStyle.THICK);

        Font tableHeaderFont = p_wb.createFont();

        tableHeaderFont.setBold(true);
        tableHeaderFont.setFontName("Times New Roman");
        tableHeaderFont.setFontHeightInPoints((short) 12);

        style.setFont(tableHeaderFont);

        return style;
    }

    private  CellStyle setCellNoBoldStyle(SXSSFWorkbook p_wb) {
        CellStyle style = p_wb.createCellStyle();
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.TOP);
        style.setWrapText(true);

        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);

        Font cellNoBoldFont = p_wb.createFont();

        cellNoBoldFont.setBold(false);
        cellNoBoldFont.setFontName("Times New Roman");
        cellNoBoldFont.setFontHeightInPoints((short) 11);

        style.setFont(cellNoBoldFont);

        return style;
    }
}
