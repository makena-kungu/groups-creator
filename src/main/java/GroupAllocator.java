import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class GroupAllocator {
    private static final String FILE_DIR = Main.ROOT_DIR;
    private final List<Contact> contacts;

    {
        contacts = ContactsExtractor.contactList();
        Collections.shuffle(contacts);
    }

    private final List<Contact> gLContacts = ContactsExtractor.extractFromExcelFile("steering committee.xlsx");

    static void execute() {
        GroupAllocator ga = new GroupAllocator();
        final Deque<Contact> membersOnly = new LinkedList<>(ga.contacts);
        final List<Contact> gLContacts1 = ga.gLContacts;
        gLContacts1.sort(Comparator.comparing(Contact::name));
        membersOnly.removeAll(gLContacts1);

        System.out.println("membersOnly.size() = " + membersOnly.size());
        var groups = new HashMap<Contact, List<Contact>>();
        final int memsSize = membersOnly.size() / gLContacts1.size();
        final int rem = membersOnly.size() % gLContacts1.size();

        // generate random indices
        Random random = new Random();
        Set<Integer> extras = new HashSet<>();
        for (int i = 0; i < rem; i++) {
            int i1;
            do {
                i1 = random.nextInt(gLContacts1.size());
            } while (extras.contains(i1));
            extras.add(i1);
        }

        for (int j = 0; j < gLContacts1.size(); j++) {
            Contact s = gLContacts1.get(j);
            final var mems = new ArrayList<Contact>();
            for (int i = 0; i < memsSize; i++) {
                if (membersOnly.peek() == null) break;
                mems.add(membersOnly.pop());
            }
            // add an extra record here
            if (extras.contains(j) && membersOnly.peek() != null) mems.add(membersOnly.pop());
            groups.put(s, mems);
        }
        final var b = new StringBuilder();
        AtomicInteger count = new AtomicInteger();
        new TreeMap<>(groups).forEach((gl, contacts) -> {
            count.addAndGet(contacts.size());
            b.append(gl)
                    .append('\n')
                    .append("Contacts: ").append(contacts.size())
                    .append('\n');
        });
        System.out.println();
        System.out.println();
        System.out.println("Groups:\n" + b);
        System.out.println("count = " + count);
        // todo create a sheet for every group leader.

        final String path = FILE_DIR + "groups.xlsx";

        try (FileOutputStream stream = new FileOutputStream(path)) {
            try (XSSFWorkbook wb = new XSSFWorkbook()) {
                final AtomicInteger p = new AtomicInteger();
                int size1 = groups.size();
                groups.forEach((gl, members) -> {
                    XSSFSheet sheet = createSheetHeader(wb.createSheet(gl.name()));

                    for (int i = 0, size = members.size(); i < size; i++) {
                        XSSFRow row = sheet.createRow(i + 1);
                        Contact member = members.get(i);
                        for (int k = 0; k < headings.length; k++) {
                            XSSFCell cell = row.createCell(k);
                            cell.setCellType(CellType.STRING);
                            var phones = member.phones();
                            final String value;
                            if (k == 0) value = member.name();
                            else value = phones.peek() != null ? phones.pop() : null;
                            cell.setCellValue(value);
                        }
                    }

                    System.out.printf("\rProgress: %.2f", (p.incrementAndGet() / (double) size1) * 100);
                });
                wb.write(stream);
                System.out.println();
                System.out.println("Done!");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static XSSFSheet createSheetHeader(XSSFSheet sheet) {
        XSSFRow row = sheet.createRow(0);
        for (int i = 0, headingsLength = headings.length; i < headingsLength; i++) {
            String heading = headings[i];

            final var wb = sheet.getWorkbook();
            final var style = wb.createCellStyle();
            final var font = wb.createFont();
            font.setFontHeightInPoints((short) 14);
            font.setBold(true);
            font.setFontName("Calibri Light");
            style.setFont(font);
            style.setAlignment(HorizontalAlignment.CENTER);
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            style.setFillBackgroundColor(IndexedColors.WHITE1.getIndex());

            XSSFCell cell = row.createCell(i, CellType.STRING);
            cell.setCellValue(heading);
        }
        return sheet;
    }

    static String[] headings = new String[]{"Name", "Phone 1", "Phone 2"};

}
