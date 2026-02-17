package com.com.company.mcdr.printing;
// By Hatem Moushir 2026 v2.1

import oracle.forms.handler.IHandler;
import oracle.forms.properties.ID;
import oracle.forms.ui.VBean;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.printing.PDFPrintable;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.PrinterJob;
import java.net.URL;

public class DirectPrintBean extends VBean {

    protected static final ID pPDFURL       = ID.registerProperty("PDF_URL");
    protected static final ID pOrientation  = ID.registerProperty("PRINT_ORIENTATION"); // PORTRAIT / LANDSCAPE
    protected static final ID pPrinterName  = ID.registerProperty("PRINTER_NAME");      // printer name or "DEFAULT"
    protected static final ID pPrinterType  = ID.registerProperty("PRINTER_TYPE");      // OFFICE / DOTMATRIX / THERMAL
    protected static final ID pUseDialog    = ID.registerProperty("USE_PRINT_DIALOG");   // true / false

    private String m_orientation = "PORTRAIT";
    private String m_printerName = "DEFAULT";
    private String m_printerType = "OFFICE";
    private boolean m_useDialog = false;

    private IHandler m_handler;

    public void init(IHandler handler) {
        m_handler = handler;
        super.init(handler);
    }

    private PrintService findPrintService(String printerName) {
        if (printerName == null) return null;
        PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);
        for (PrintService service : services) {
            if (service.getName().equalsIgnoreCase(printerName)) {
                return service;
            }
        }
        return null;
    }

    @Override
    public boolean setProperty(ID _ID, Object _args) {
        try {
            // ضبط الخصائص
            if (_ID == pOrientation && _args != null) {
                String value = _args.toString().toUpperCase();
                if ("PORTRAIT".equals(value) || "LANDSCAPE".equals(value)) {
                    m_orientation = value;
                    return true;
                }
            }

            if (_ID == pPrinterName && _args != null) {
                m_printerName = _args.toString();
                return true;
            }

            if (_ID == pPrinterType && _args != null) {
                String value = _args.toString().toUpperCase();
                if ("OFFICE".equals(value) || "DOTMATRIX".equals(value) || "THERMAL".equals(value)) {
                    m_printerType = value;
                    return true;
                }
            }

            if (_ID == pUseDialog && _args != null) {
                m_useDialog = Boolean.parseBoolean(_args.toString());
                return true;
            }

            // الطباعة من URL
            if (_ID == pPDFURL && _args != null) {

                URL pdfURL = new URL(_args.toString());
                PDDocument document = PDDocument.load(pdfURL.openStream());

                PrinterJob job = PrinterJob.getPrinterJob();
                PrintService service = null;

                if (!"DEFAULT".equalsIgnoreCase(m_printerName)) {
                    service = findPrintService(m_printerName);
                    if (service == null) {
                        document.close();
                        m_handler.setItemProperty("BLOCK_NAME.PRINT_STATUS",
                                oracle.forms.properties.VM.PROP_TEXT,
                                "Printer not found: " + m_printerName);
                        return false;
                    }
                    job.setPrintService(service);
                }

                if (m_useDialog && !"DEFAULT".equalsIgnoreCase(m_printerName)) {
                    if (!job.printDialog()) {
                        document.close();
                        m_handler.setItemProperty("BLOCK_NAME.PRINT_STATUS",
                                oracle.forms.properties.VM.PROP_TEXT,
                                "User cancelled print");
                        return false;
                    }
                }

                // PDFPrintable مع Scaling ذكي
                PDFPrintable printable = new PDFPrintable(document, PDFPrintable.SCALE_TO_FIT) {
                    @Override
                    public PageFormat getPageFormat(int pageIndex, PrinterJob job) {
                        PDPage page = document.getPage(pageIndex);
                        PDRectangle rect = page.getMediaBox();
                        Paper paper = new Paper();

                        switch (m_printerType) {
                            case "OFFICE":
                                paper.setSize(rect.getWidth(), rect.getHeight());
                                paper.setImageableArea(0, 0, rect.getWidth(), rect.getHeight());
                                break;

                            case "DOTMATRIX":
                                // Continuous feed: الطول = ارتفاع PDF، العرض = PDF width أو fixed حسب الطابعة
                                double widthDM = rect.getWidth();
                                double heightDM = rect.getHeight();
                                paper.setSize(widthDM, heightDM);
                                paper.setImageableArea(0, 0, widthDM, heightDM);
                                break;

                            case "THERMAL":
                                // 80mm width ثابت، الطول ديناميكي حسب محتوى PDF
                                double widthThermal = 226.77; // 80mm
                                double scale = widthThermal / rect.getWidth();
                                double heightThermal = rect.getHeight() * scale;
                                paper.setSize(widthThermal, heightThermal);
                                paper.setImageableArea(0, 0, widthThermal, heightThermal);
                                break;
                        }

                        PageFormat pf = new PageFormat();
                        pf.setPaper(paper);
                        pf.setOrientation("LANDSCAPE".equals(m_orientation) ? PageFormat.LANDSCAPE : PageFormat.PORTRAIT);
                        return pf;
                    }
                };

                job.setPrintable(printable);
                job.print();

                document.close();

                m_handler.setItemProperty("BLOCK_NAME.PRINT_STATUS",
                        oracle.forms.properties.VM.PROP_TEXT, "OK");

                return true;
            }

        } catch (Exception e) {
            e.printStackTrace();
            m_handler.setItemProperty("BLOCK_NAME.PRINT_STATUS",
                    oracle.forms.properties.VM.PROP_TEXT, e.getMessage());
            return false;
        }

        return true;
    }
}
