package passio;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { PassioDataConfig.class, PassioTestConfig.class })
@ActiveProfiles("test")
public class PassioEntityFactoryTest extends AbstractAppengineTest {

	@Autowired
	private PassioEntityFactory passioEntityFactory;

	@Test
	public void thatASavedEntityCanBeLoadedAgain() {
		PassioEntity passioEntity = new PassioEntity("user", "initial_value", new byte[] {0, 1, 2});
		passioEntityFactory.create(passioEntity);

		PassioEntity retrievedEntitiy = passioEntityFactory.load("user");
		assertThat(retrievedEntitiy, is(passioEntity));
	}

	@Test
	public void thatACreatedEntityCannotBeOverwrittenByAnotherCreate() {
		PassioEntity passioEntity = new PassioEntity("user", "initial_value", new byte[] {0, 1, 2});
		passioEntityFactory.create(passioEntity);

		PassioEntity anotherPassioEntity = new PassioEntity("user", "another_value", new byte[] {3, 4 ,5});
		assertThat(passioEntityFactory.create(anotherPassioEntity), is(false));
	}

}
