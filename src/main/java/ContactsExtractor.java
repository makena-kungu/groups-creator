import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbookFactory;

import java.io.*;
import java.time.format.FormatStyle;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ContactsExtractor {
	private final List<String> contacts = Collections.synchronizedList(new ArrayList<>());

	static List<String> contactList() {
		return new ContactsExtractor().contacts();
	}

	void extractFromTxtFile() {
		var path = "C:\\Users\\USER\\IdeaProjects\\group_creator\\src\\main\\resources\\contacts.txt";
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
				contacts.add(format(str.trim()));
			}
			System.out.println("contacts      = " + Arrays.toString(contacts.toArray(new String[0])));
			System.out.println("contacts size = " + contacts.size());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	void extractFromExcelFile() {
		var path = "C:\\Users\\USER\\IdeaProjects\\group_creator\\src\\main\\resources\\contacts.xlsx";
		try (var workbook = XSSFWorkbookFactory.createWorkbook(OPCPackage.open(new File(path)))) {
			var sht = workbook.getSheetAt(0);
			int col = -1;
			for (Cell cell : sht.getRow(0)) {
				String value = cell.getStringCellValue();
				if (Objects.equals(value, "Phone 1 - Value")) {
					col = cell.getColumnIndex();
					break;
				}
			}

			// iterate sheet rows
			var contacts = new ArrayList<String>();

			int i = 0;
			for (Row row : sht) {
				if (i++ == 0) continue;
				String s;
				Cell cell = row.getCell(col);
				try {
					s = format(String.valueOf((long) cell.getNumericCellValue()));
					System.out.println("s = " + s);
				} catch (Exception e) {
					String[] strings = cell.getStringCellValue().split(":");
					var b = new StringBuilder();
					for (int i1 = 0, last = strings.length - 1; i1 <= last; i1++) {
						String s1 = strings[i1].trim();
						if (!s1.isEmpty()) {
							b.append(format(s1));
							if (i1 != last) {
								b.append(" & ");
							}
						}
					}
					s = b.toString();
				}
				contacts.add(s);
			}

			Object[] array = contacts.toArray();
			System.out.println("contacts = " + Arrays.toString(array));

			System.out.println("col = " + col);
			String sheetName = sht.getSheetName();
			System.out.println("sheet name = " + sheetName);
		} catch (IOException | InvalidFormatException e) {
			throw new RuntimeException(e);
		}
	}

	List<String> contacts() {
		try (ExecutorService executorService = Executors.newFixedThreadPool(2)) {
			executorService.submit(this::extractFromTxtFile);
			executorService.submit(this::extractFromExcelFile);
		}
		return contacts;
	}

	private final PhoneNumberUtil util = PhoneNumberUtil.getInstance();
	private final PhoneNumberUtil.PhoneNumberFormat FORMAT = PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL;

	private String format(String number) {
		try {
			return util.format(util.parse(number, "KE"), FORMAT);
		} catch (NumberParseException e) {
			throw new RuntimeException(e);
		}
	}
}
