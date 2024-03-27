import java.util.Arrays;
import java.util.Objects;

final class Contact {
	private final String name;
	private final String contact;
	private String first;
	private String second;


	Contact(String name, String contact) {
		this.name = name;
		this.contact = contact;
		String[] strings = contact.split("&");
		try {
			first = strings[0];
			second = strings[1];
		} catch (Exception e) {
//			throw new RuntimeException(e);
		}
	}

	Contact(String contact) {
		this(null, contact);

	}

	public String name() {
		return name;
	}

	public String contact() {
		return contact;
	}

	public String first() {
		return first;
	}

	public String second() {
		return second;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (obj == null || obj.getClass() != this.getClass()) return false;
		var that = (Contact) obj;
		return Objects.equals(this.name, that.name) &&
				Objects.equals(this.contact, that.contact);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, contact);
	}

	@Override
	public String toString() {
		return "Contact[" +
				"name=" + name + ", " +
				"contact=" + contact + ']';
	}

}