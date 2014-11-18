package passio;

public interface PassioEntityFactory {

	PassioEntity load(String name) throws PassioEntityNotFoundException, PassioEntityFactoryException;
	void save(PassioEntity e) throws PassioEntityFactoryException;

}
