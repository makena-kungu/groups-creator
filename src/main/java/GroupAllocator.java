import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class GroupAllocator {
	private static final String FILE_DIR = "C:\\Users\\USER\\IdeaProjects\\group_creator\\src\\main\\resources";
	private final List<Contact> contacts;

	{
		contacts = ContactsExtractor.contactList();
		Collections.shuffle(contacts);
	}

	private final List<Contact> gLContacts = ContactsExtractor.extractFromExcelFile("C:\\Users\\USER\\IdeaProjects\\group_creator\\src\\main\\resources\\steering committee.xlsx");

	static void execute() {
		GroupAllocator ga = new GroupAllocator();
		final Deque<Contact> membersOnly = new LinkedList<>(ga.contacts);
		final List<Contact> gLContacts1 = ga.gLContacts;
		membersOnly.removeAll(gLContacts1);

		System.out.println("membersOnly.size() = " + membersOnly.size());
		var groups = new HashMap<Contact, List<Contact>>();
		var j = 0;
		for (Contact s : gLContacts1) {
			final var mems = new ArrayList<Contact>();
			for (int i = 0, size = j++ % 2 == 0 ? 9 : 10; i < size; i++) {
				if (membersOnly.peek() == null) break;
				mems.add(membersOnly.pop());
			}
			groups.put(s, mems);
		}
		final var b = new StringBuilder();
		AtomicInteger count = new AtomicInteger();
		groups.forEach((gl, contacts) -> {
			count.addAndGet(contacts.size());
			b.append(gl)
					.append('\n')
					.append("Contacts: ").append(contacts.size())
					//				.append(Arrays.toString(contacts.toArray()))
					.append('\n');
		});
		System.out.println();
		System.out.println();
		System.out.println("Groups:\n" + b);
		System.out.println("count = " + count);
		// todo create a sheet for every group leader.

		try (XSSFWorkbook wb = XSSFWorkbookFactory.createWorkbook(OPCPackage.open(FILE_DIR + "groups.xlsx"))) {
			groups.forEach((gl, members) -> {
				XSSFSheet sheet = createSheetHeader(wb.createSheet());
				for (int i = 0, size = members.size(); i < size; i++) {
					XSSFRow row = sheet.createRow(i + 1);
					Contact member = members.get(i);
					for (int k = 0; k < headings.length; k++) {
						XSSFCell cell = row.createCell(k);
						cell.setCellType(CellType.STRING);
						String value = k==0?member.name()
					}
				}
			});
		} catch (IOException | InvalidFormatException e) {
			throw new RuntimeException(e);
		}
	}

	static XSSFSheet createSheetHeader(XSSFSheet sheet) {
		XSSFRow row = sheet.createRow(0);
		for (int i = 0, headingsLength = headings.length; i < headingsLength; i++) {
			String heading = headings[i];
			XSSFCellStyle style = row.getRowStyle();
			XSSFFont font = sheet.getWorkbook().createFont();
			font.setFontHeightInPoints((short) 14);
			font.setBold(true);
			font.setFontName("Calibri Light");
			style.setFont(font);
			style.setAlignment(HorizontalAlignment.CENTER);
			style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			style.setFillBackgroundColor(IndexedColors.WHITE1.getIndex());

			XSSFCell cell = row.createCell(i, CellType.STRING);
			cell.setCellValue(heading);
			return sheet;
		}
		return sheet;
	}

	static String[] headings = new String[]{"Name", "Phone 1", "Phone 2"};

}
