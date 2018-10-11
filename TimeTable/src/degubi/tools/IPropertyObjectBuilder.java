package degubi.tools;

public interface IPropertyObjectBuilder<T>{
	String writeObject(T object);
	T readObject(String dataStr);
}