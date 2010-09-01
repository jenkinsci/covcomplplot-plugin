package hudson.plugins.covcomplplot;

import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
public class PagingInfoTest {

	@Test
	public void testStartPage() {
		Pagination a = new Pagination(3, 230, 20, "EE");
		assertThat(a.getTag(), containsString("Next"));
	}
}
