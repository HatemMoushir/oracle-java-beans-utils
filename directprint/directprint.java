
package com.company.printing;
// By Hatem Moushir v2.2

import oracle.forms.handler.IHandler;
import oracle.forms.properties.ID;
import oracle.forms.ui.VBean;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.printing.PDFPrintable;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.PrinterJob;
import java.net.URL;

public class DirectPrintBean extends VBean {

    // ==== Properties ====
    protected static final ID pPrintURL      = ID.registerProperty("PRINT_URL");
    protected static final ID pPrinterName   = ID.registerProperty("PRINTER_NAME");
    protected static final ID pPrinterType   = ID.registerProperty("PRINTER_TYPE");
    protected static final ID pOrientation   = ID.registerProperty("ORIENTATION");
    protected static final ID pUseDialog     = ID.registerProperty("USE_DIALOG");
    protected static final ID pGetError      = ID.registerProperty("GET_ERROR");

    private String m_errorMessage = "OK";
    private String m_printerName  = "DEFAULT";
    private String m_printerType  = "OFFICE";
    private String m_orientation  = "PORTRAIT";
    private boolean m_useDialog   = false;

    public void init(IHandler handler) {
        super.init(handler);
    }

    // ==== GET_CUSTOM_PROPERTY ====
    @Override
    public Object getProperty(ID id) {

        if (id == pGetError) {
            return m_errorMessage;
        }

        return super.getProperty(id);
    }

    // ==== SET_CUSTOM_PROPERTY ====
    @Override
    public boolean setProperty(ID id, Object value) {

        try {

            if (id == pPrinterName && value != null) {
                m_printerName = value.toString();
                return true;
            }

            if (id == pPrinterType && value != null) {
                m_printerType = value.toString().toUpperCase();
                return true;
            }

            if (id == pOrientation && value != null) {
                m_orientation = value.toString().toUpperCase();
                return true;
            }

            if (id == pUseDialog && value != null) {
                m_useDialog = Boolean.parseBoolean(value.toString());
                return true;
            }

            // ==== Start Printing ====
            if (id == pPrintURL && value != null) {

                m_errorMessage = "OK";  // reset

                URL pdfURL = new URL(value.toString());
                PDDocument document = PDDocument.load(pdfURL.openStream());

                PrinterJob job = PrinterJob.getPrinterJob();

                // تحديد الطابعة
                if (!"DEFAULT".equalsIgnoreCase(m_printerName)) {
                    PrintService[] services =
                            PrintServiceLookup.lookupPrintServices(null, null);

                    boolean found = false;

                    for (PrintService service : services) {
                        if (service.getName().equalsIgnoreCase(m_printerName)) {
                            job.setPrintService(service);
                            found = true;
                            break;
                        }
                    }

                    if (!found) {
                        document.close();
                        m_errorMessage = "Printer not found: " + m_printerName;
                        return false;
                    }
                }

                // Dialog اختياري
                if (m_useDialog && !"DEFAULT".equalsIgnoreCase(m_printerName)) {
                    if (!job.printDialog()) {
                        document.close();
                        m_errorMessage = "User cancelled print";
                        return false;
                    }
                }

                PDFPrintable printable =
                        new PDFPrintable(document, PDFPrintable.SCALE_TO_FIT);

                job.setPrintable(printable);

                job.print();

                document.close();

                m_errorMessage = "OK";
                return true;
            }

        } catch (Exception e) {
            m_errorMessage = e.getMessage();
            return false;
        }

        return true;
    }
}
