package passio;

public interface PassioEntityFactory {

	PassioEntity load(String name) throws PassioEntityFactoryException;

	boolean create(PassioEntity e) throws PassioEntityFactoryException;
	boolean update(PassioEntity e) throws PassioEntityFactoryException;

}
