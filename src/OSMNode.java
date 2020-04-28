import java.util.LinkedList;
import java.util.List;

public class OSMNode {
	public long id;
	public double lat;
	public double lon;
	//в списке linkedTo хранить id(Long) или ссылки на объекты OSMNode?
	//public List<Long> linkedTo = new LinkedList<Long>();
	public List<OSMNode> linkedTo = new LinkedList<OSMNode>();
}
