import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbookFactory;

import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ContactsExtractor {
    private final List<Contact> contacts = Collections.synchronizedList(new ArrayList<>());

    static List<Contact> contactList() {
        return new ContactsExtractor().contacts();
    }

    void extractFromTxtFile() {
        var path = Main.ROOT_DIR + "contacts.txt";
        try (var i = new FileInputStream(path)) {
            var builder = new StringBuilder();
            try (var reader = new BufferedReader(new InputStreamReader(i))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
            }
            String[] strings = builder.toString().split(",");
            for (String str : strings) {
                contacts.add(new Contact(format(str.trim())));
            }
//            System.out.println("contacts      = " + Arrays.toString(contacts.toArray(new Contact[0])));
//            System.out.println("contacts size = " + contacts.size());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    void extractFromExcelFile() {
        this.contacts.addAll(extractFromExcelFile("contacts.xlsx"));
    }

    static List<Contact> extractFromExcelFile(String fileName) {
        try (var workbook = XSSFWorkbookFactory.createWorkbook(OPCPackage.open(new File(Main.ROOT_DIR + fileName)))) {
            var sht = workbook.getSheetAt(0);
            int valueColumn = -1;
            int nameColumn = -1;
            for (Cell cell : sht.getRow(0)) {
                String value = cell.getStringCellValue();
                if (value.equals("Name")) {
                    nameColumn = cell.getColumnIndex();
                    continue;
                }
                if (Objects.equals(value, "Phone 1 - Value")) {
                    valueColumn = cell.getColumnIndex();
                    break;
                }
            }

            // iterate sheet rows
            var contacts = new ArrayList<Contact>();

            int i = 0;
            for (Row row : sht) {
                if (i++ == 0) continue;
                final LinkedList<String> phones = new LinkedList<>();
                final Cell value = row.getCell(valueColumn);
                try {
                    phones.add(format(String.valueOf((long) value.getNumericCellValue())));
                } catch (Exception e) {
                    final String[] strings = value.getStringCellValue().split(":");
                    List<String> phones1 = Arrays.stream(strings)
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .toList();
                    phones.addAll(phones1);
                }
                final Cell cell = row.getCell(nameColumn);
                final String name;
                if (cell == null || cell.getCellType() == CellType.BLANK || cell.getStringCellValue().isEmpty())
                    name = null;
                else name = cell.getStringCellValue();
                contacts.add(new Contact(name, phones));
            }

//            System.out.println("contacts = " + Arrays.toString(contacts.toArray()));
//            System.out.println("col = " + valueColumn);
            String sheetName = sht.getSheetName();
//            System.out.println("sheet name = " + sheetName);
            return contacts;
        } catch (IOException | InvalidFormatException e) {
            throw new RuntimeException(e);
        }
    }

    List<Contact> contacts() {
        ExecutorService service = Executors.newFixedThreadPool(2);
        service.execute(this::extractFromTxtFile);
        service.execute(this::extractFromExcelFile);
        return contacts;
    }

    private static final PhoneNumberUtil UTIL = PhoneNumberUtil.getInstance();
    private static final PhoneNumberUtil.PhoneNumberFormat FORMAT = PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL;

    private static String format(String number) {
        try {
            return UTIL.format(UTIL.parse(number, "KE"), FORMAT);
        } catch (NumberParseException e) {
            throw new RuntimeException(e);
        }
    }
}
