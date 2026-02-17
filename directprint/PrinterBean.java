package com.company.printing;
// By Hatem Moushir 2026
import oracle.forms.handler.IHandler;
import oracle.forms.properties.ID;
import oracle.forms.ui.VBean;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import java.util.ArrayList;
import java.util.List;

public class PrinterBean extends VBean {

    protected static final ID pLoadPrinters = ID.registerProperty("LOAD_PRINTERS");
    protected static final ID pGetPrinters  = ID.registerProperty("GET_PRINTERS");
    protected static final ID pAutoDetect   = ID.registerProperty("AUTO_DETECT");
    protected static final ID pPrinterName  = ID.registerProperty("PRINTER_NAME");

    private List<String> printerList = new ArrayList<>();
    private String selectedPrinter   = "";
    private String detectedType      = "OFFICE";

    public void init(IHandler handler) {
        super.init(handler);
    }

    // ======================
    // GET_CUSTOM_PROPERTY
    // ======================
    @Override
    public Object getProperty(ID id) {

        if (id == pGetPrinters) {
            return String.join(",", printerList);
        }

        if (id == pAutoDetect) {
            return detectedType;
        }

        return super.getProperty(id);
    }

    // ======================
    // SET_CUSTOM_PROPERTY
    // ======================
    @Override
    public boolean setProperty(ID id, Object value) {

        try {

            // تحميل الطابعات
            if (id == pLoadPrinters) {

                printerList.clear();

                PrintService[] services =
                        PrintServiceLookup.lookupPrintServices(null, null);

                for (PrintService service : services) {
                    printerList.add(service.getName());
                }

                return true;
            }

            // تحديد طابعة و Auto Detect
            if (id == pPrinterName && value != null) {

                selectedPrinter = value.toString();
                String upper = selectedPrinter.toUpperCase();

                if (upper.contains("EPSON") ||
                    upper.contains("THERMAL") ||
                    upper.contains("TM-")) {

                    detectedType = "THERMAL";

                } else if (upper.contains("LQ") ||
                           upper.contains("DOT") ||
                           upper.contains("FX")) {

                    detectedType = "DOTMATRIX";

                } else {

                    detectedType = "OFFICE";
                }

                return true;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }
}
