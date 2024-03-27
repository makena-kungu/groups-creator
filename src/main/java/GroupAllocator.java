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
			for (int i = 0,size = j++ % 2 == 0 ? 9 : 10; i < size; i++) {
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
	}
}
